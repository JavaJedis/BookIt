//Import necessary modules
const { cp } = require('fs');
const { MongoClient } = require('mongodb');
const mongo = require("mongodb");
const utils = require("./utils");
const { start } = require('repl');

//Global Definitions
const DB_URL = 'mongodb://localhost:27017/';
const MODULE_NAME = "DBHandler";
var client;



/**
 * Initialize Database handler
 */
function dbh_init() {

    utils.consoleMsg(MODULE_NAME, "Initializing Database Handler");
    client = new MongoClient(DB_URL);
    try {
        client.connect().then(() => {
            utils.consoleMsg(MODULE_NAME, "Initialization successful");
        });
    } catch (err) {
        utils.consoleMsg(MODULE_NAME, "Initialziation Failed. See error message below.");
        console.log(err);
    }
}

/**
 * Deinitialize Database handler
 */
async function dbh_deinit() {
    await client.close();
    utils.consoleMsg(MODULE_NAME, 'Connection closed');
}


// get rooms request database handling function
async function getRooms(building_code, dbName) {
    const collection = client.db(dbName).collection(building_code);
    const data = await collection.find({}).toArray()
    if (data.length !== 0) {
        if (building_code === 'building_all') {
            delete data[0]._id
        }
        return data
    }
    var err = new Error("Not Found")
    err.statusCode = 404
    throw err
}

/**
 * Get a specific romm's data
 * @param {string} building_code 
 * @param {string} room_num 
 * @param {string} dbName 
 */
async function getRoom(buildingCode, roomNum, dbName) {

    const collection = client.db(dbName).collection(buildingCode);
    const result = await collection.findOne({ _id: roomNum });
    return result;
}

// Gets the timeslots data for a given date of a particular room
async function getSlots(slotsData) {
    const collection = client.db('study_room_db').collection(slotsData.buildingCode);
    const roomData = await collection.findOne({ _id: slotsData.roomNo })

    if (!roomData) {
        var err = new Error("Room Not Found")
        err.statusCode = 404
        throw err
    }

    const bookingCollection = client.db('study_room_db').collection('bookings')
    const [validDate, day] = dateTimeValidator(slotsData.date, "2359")

    if (!validDate) {
        var err = new Error("Invalid Date")
        err.statusCode = 404
        throw err
    }

    const bookingData = await bookingCollection.findOne({ _id: slotsData.date })
    const roomCode = slotsData.buildingCode + " " + slotsData.roomNo

    if (!bookingData || !(bookingData[roomCode])) {
        var data = createSlots(roomData.open_times[day], roomData.close_times[day])
    } else {
        var data = bookingData[roomCode]
        for (let i = 0; i < 48; i++) {
            if (data[i] != "2" && data[i] != "0") {
                data[i] = "1"
            }
        }
    }

    return data.join('')
}

// book room request database handling function
async function bookStudyRooms(bookingData) {
    const startTime = parseInt(bookingData.startTime)
    const endTime = parseInt(bookingData.endTime)
    const date = bookingData.date
    const [validDate, day] = dateTimeValidator(date, bookingData.startTime)
    var validTimes = timeValidator(startTime, endTime)

    if ((!validDate) || (!validTimes)) {
        var err = new Error("Invalid Date/Time")
        err.statusCode = 404
        throw err
    }

    const buildingCollection = client.db('study_room_db').collection(bookingData.buildingCode);
    const room = await buildingCollection.findOne({ _id: bookingData.roomNo })
    if (!room) {
        var err = new Error("Room Not Found")
        err.statusCode = 404
        throw err
    }

    const roomOpenTime = room.open_times[day]
    const roomCloseTime = room.close_times[day]


    if ((roomOpenTime > startTime) || (roomCloseTime < endTime)) {
        var err = new Error("Invalid Timeslots")
        err.statusCode = 404
        throw err
    }


    const userCollection = client.db('users').collection('users')
    const bookingCollection = client.db('study_room_db').collection('bookings')
    const userBookingCollection = client.db('users').collection('bookings')

    const bookingOriginal = await bookingCollection.findOne({ _id: date })

    var bookingsDate = JSON.parse(JSON.stringify(bookingOriginal))

    const roomCode = bookingData.buildingCode + " " + bookingData.roomNo
    if (!bookingOriginal) {
        bookingsDate = {
            _id: date,
        }
    }

    if (!(bookingsDate[roomCode])) {
        var timeSlotsCode = createSlots(roomOpenTime, roomCloseTime)
        bookingsDate[roomCode] = timeSlotsCode
    }

    const startIndex = Math.ceil(startTime / 50)
    const endIndex = Math.ceil(endTime / 50)

    for (let i = startIndex; i < endIndex; i++) {
        if (bookingsDate[roomCode][i] != "0") {
            var err = new Error("Unavailable Timeslots")
            err.statusCode = 403
            throw err
        }
    }

    for (let i = startIndex; i < endIndex; i++) {
        bookingsDate[roomCode][i] = bookingData.email
    }

    try {
        if (!bookingOriginal) {
            await bookingCollection.insertOne(bookingsDate)
        } else {
            await bookingCollection.updateOne(bookingOriginal, { $set: bookingsDate })
        }
    } catch (error) {
        var err = new Error("Unavailable Timeslots")
        err.statusCode = 403
        throw err
    }

    var userbookingdata = {
        roomCode: roomCode,
        startIndex: startIndex,
        endIndex: endIndex,
        date: date,
        waitlist: []
    }

    const result = await userBookingCollection.insertOne(userbookingdata)

    while (1) {
        try {
            const userOriginal = await userCollection.findOne({ _id: bookingData.email })
            var user = JSON.parse(JSON.stringify(userOriginal))
            user.booking_ids.push(result.insertedId)
            await userCollection.updateOne(userOriginal, { $set: user })
            break
        } catch (err) {
            continue
        }
    }
    return "booked!"
}



// async function filterRooms(filter_data) {


// }

async function userLogin(userInfo) {
    const users = client.db('users').collection('users');
    const user = await users.findOne({ _id: userInfo._id })
    if (user) {
        data = 'User exists'
        return data
    }
    await users.insertOne(userInfo);
    data = 'User information saved'
    return data
}


async function checkUser(userEmail) {
    const users = client.db('users').collection('users');
    const user = await users.findOne({ _id: userEmail })
    if (user) {
        return user
    } else {
        var err = new Error("Unauthorized")
        err.statusCode = 401
        throw err
    }
}

async function getBookings(IDs) {
    const collection = client.db('users').collection('bookings');

    const documents = await collection.find({ _id: { $in: IDs } }).toArray();

    for (let i = 0; i < documents.length; i++) {
        documents[i].startTime = documents[i].startIndex * 50
        documents[i].endTime = (documents[i].endIndex) * 50
        if (documents[i].startIndex % 2 == 1) {
            documents[i].startTime -= 20
        }
        if ((documents[i].endIndex) % 2 == 1) {
            documents[i].endTime -= 20
        }
        documents[i].starTime = timeConvertor(documents[i].startTime)
        documents[i].endTime = timeConvertor(documents[i].endTime)
        delete documents[i].waitlist
        delete documents[i].startIndex
        delete documents[i].endIndex
    }
    return documents
}

async function cancelBooking(ID, user) {

    const userCollection = client.db('users').collection('users');
    const userBookingCollection = client.db('users').collection('bookings');
    const bookingCollection = client.db('study_room_db').collection('bookings');

    for (let i = 0; i < user.booking_ids.length; i++) {
        if (user.booking_ids[i].toHexString() === ID) {
            ID = user.booking_ids[i]
        }
    }

    const booking = await userBookingCollection.findOne({ _id: ID })

    const bookingsDateOriginal = await bookingCollection.findOne({ _id: booking.date })

    var bookingsDate = JSON.parse(JSON.stringify(bookingsDateOriginal))

    let roomCode = booking.roomCode

    for (let i = booking.startIndex; i < booking.endIndex; i++) {
        bookingsDate[roomCode][i] = "0"
    }

    try {
        await bookingCollection.updateOne(bookingsDateOriginal, { $set: bookingsDate })
    } catch (error) {
        var err = new Error("Could not complete request")
        err.statusCode = 404
        throw err
    }
    await userBookingCollection.deleteOne({ _id: ID });
    var flag = true
    // while (flag) {
    //     try {
    console.log("here")
    const userOriginal = await userCollection.findOne({ _id: user.email })
    console.log("here")
    var user1 = JSON.parse(JSON.stringify(userOriginal))
    console.log(user1)
    let filteredList = user1.booking_ids.filter(item => item.toHexString() !== ID.toHexString)
    console.log(filteredList)
    user1.booking_ids = filteredList
    await userCollection.updateOne(userOriginal, { $set: user1 })
    flag = false
    //     } catch (err) {
    //         continue
    //     }
    // }
    console.log("done")
    return "Removed"
}


function commentUploader(comment) {
    const building = comment.building;
    const room = comment.room;
    const comment_new = comment.comment;
    const db = client.db('study_room_db');
    console.log(comment_new);
    if (!db.listCollections({ name: building }).hasNext()) {
        return false;
    }
    const collection = db.collection(building);
    if (!collection.find({ _id: room }).hasNext()) {
        return false;
    }

    collection.updateOne({ _id: room }, { $push: { comments: comment_new } }).then(
        (err) => {
            if (err) {
                return false;
            }
        }
    );
    return true;

}


// Helper Functions

// Checks if the booking date is after now and less than 2 weeks from now 
// Additionally returns the day of the week of date

function dateTimeValidator(date, startTime) {
    var formatter = new Intl.DateTimeFormat('en-US', { timeZone: 'America/Vancouver' });
    var inputDateParts = date.split('-');
    var inputDate = new Date(inputDateParts[2], inputDateParts[1] - 1, inputDateParts[0]);

    var currentDate = formatter.format(new Date());
    var currentDateTime = new Date(currentDate);

    // Convert inputTimeString to hours and minutes
    var inputHours = parseInt(startTime.slice(0, 2));
    var inputMinutes = parseInt(startTime.slice(2));

    // Set the hours and minutes of the inputDate
    inputDate.setHours(inputHours);
    inputDate.setMinutes(inputMinutes);


    // Check if the input date is within 14 days from now, considering the day of the week
    var daysDifference = (inputDate - currentDateTime) / (1000 * 60 * 60 * 24);
    var isValid = daysDifference >= 0 && daysDifference <= 14;

    var dayOfWeek = inputDate.getDay() - 1;

    if (dayOfWeek < 0) {
        dayOfWeek = 6
    }

    return [isValid, dayOfWeek];
}

function timeValidator(startTime, endTime) {
    if ((endTime < startTime) || (((endTime - startTime) / 300) > 1)
        || (!(2400 >= endTime >= 0)) || (!(2400 >= startTime >= 0))
        || ((endTime % 100 != 0) && (endTime % 100 != 30))
        || ((startTime % 100 != 0) && (startTime % 100 != 30))) {
        return false
    } else {
        return true
    }
}

function createSlots(roomOpenTime, roomCloseTime) {
    var timeSlotsCode = []
    const openIndex = Math.ceil(roomOpenTime / 50)
    const closeIndex = Math.ceil(roomCloseTime / 50) - 1
    for (let i = 0; i < 48; i++) {
        if ((i < openIndex) || (i > closeIndex)) {
            timeSlotsCode.push("2")
        } else {
            timeSlotsCode.push("0")
        }
    }
    return timeSlotsCode
}

function timeConvertor(time) {
    return time.toString().padStart(4, '0');
}

// Interface exports

module.exports = {
    dbh_init,
    dbh_deinit,
    getRooms,
    userLogin,
    checkUser,
    bookStudyRooms,

    getSlots,
    getBookings,
    commentUploader,
    cancelBooking,
    getRoom
};
