// Import Libraries
const db_handler = require("./db_handler");
const utils = require("./utils");
const schedule = require('node-schedule');

//Global definition
const MODULE_NAME = "STUDYROOM-MANAGER";

//Global variables
var sroomSchedulerA;
var sroomSchedulerB;

//Scheduler functions

function initScheduler() {
    try {
        sroomSchedulerA = schedule.scheduleJob('sroomSchedulerA', '20 * * * *', removeExpiredBookings);
        sroomSchedulerB = schedule.scheduleJob('sroomSchedulerB', '40 * * * *', removeExpiredBookings);
        utils.consoleMsg(MODULE_NAME, "Study Room Scheduler Service Enabled");
    } catch (err) {
        utils.consoleMsg(MODULE_NAME, "Failed to enable studyroom scheduler service");
        utils.consoleMsg(MODULE_NAME, `ErrMsg:\n${err}`);
    }
}

function deinitScheduler() {
    try {
        schedule.gracefulShutdown().then(
            () => {
                utils.consoleMsg(MODULE_NAME, 'Study Room Scheduler Service Disbaled');
            }
        )
    } catch (err) {
        utils.consoleMsg(MODULE_NAME, 'Failed to disable study room scheduler service');
        utils.consoleMsg(MODULE_NAME, `ErrMsg:\n${err}`);
    }
}

/**
 * Get study room data from database
 * @param {*} req HTTPS request from the client
 * @param {*} res HTTPS response send to the client
 */
async function listStudyRooms(req, res) {
    const buildingCode = req.params.building_code
    try {
        const result = await db_handler.getRooms(buildingCode, 'study_room_db')
        utils.onSuccess(res, result)
    } catch (error) {
        utils.serverLog(MODULE_NAME, error);
        error.message = "Failed to list study rooms";
        utils.onFailure(res, error);
    }
}


function getStudyRoomComment(req, res) {

    const buildingCode = req.params.building_code;
    const roomNum = req.params.room_no;
    db_handler.getRoom(buildingCode, roomNum, "study_room_db").then(
        result => {
            if (result) {
                const comment = JSON.parse(JSON.stringify(result)).comments;
                utils.onSuccess(res, comment);
                return;
            }
            utils.onFailure(res, {
                statusCode: 404,
                message: "not found"
            });
        }

    )

}



async function bookStudyRooms(req, res) {
    const bookingData = {
        date: req.body.date,
        startTime: req.body.startTime,
        endTime: req.body.endTime,
        buildingCode: req.body.buildingCode,
        roomNo: req.body.roomNo,
        email: req.user._id
    }
    try {
        const result = await db_handler.bookStudyRooms(bookingData);
        utils.onSuccess(res, result)
    } catch (err) {
        res.status(404);
        res.type("json");
        res.send(JSON.stringify(
            {
                status: "error",
                data: err.message
            }
        ))
    }
}

async function waitlistStudyRooms(req, res) {
    const waitlistData = {
        date: req.body.date,
        startTime: req.body.startTime,
        endTime: req.body.endTime,
        buildingCode: req.body.buildingCode,
        roomNo: req.body.roomNo,
        email: req.user._id
    }
    try {
        const result = await db_handler.waitlistStudyRooms(waitlistData);
        utils.onSuccess(res, result)
    } catch (err) {
        if (!err.statusCode) {
            res.status(404);
        } else {
            res.status(err.statusCode);
        }
        res.type("json");
        res.send(JSON.stringify(
            {
                status: "error",
                data: err.message
            }
        ))
    }
}

async function getSlots(req, res) {
    const slotsData = {
        date: req.query.date,
        buildingCode: req.params.building_code,
        roomNo: req.params.room_no,
    }
    try {
        const result = await db_handler.getSlots(slotsData)
        utils.onSuccess(res, result)
    } catch (error) {
        utils.onFailure(res, error)
    }
}

async function filterStudyRooms(req, res) {
    const filterData = {
        startTime: req.query.startTime,
        duration: req.query.duration,
        day: req.query.day,
        lat: req.query.lat,
        lon: req.query.lon
    }

    try {
        const result = await db_handler.filterRooms(filterData)
        utils.onSuccess(res, result)
    } catch (error) {
        res.status(404);
        res.type("json");
        res.send(JSON.stringify(
            {
                status: "error",
                data: error.message
            }
        ))
    }
}

/**
 * Report a study room
 * @param {*} req 
 * @param {*} res 
 */
function reportRoom(req, res) {

    //Verify if the request is valid
    const buildingCode = req.params.building_code;
    const roomNumber = req.params.room_no;
    const reportMessage = req.body.msg;

    if (buildingCode == null || roomNumber == null || reportMessage == null) {
        utils.onFailure(res, {
            statusCode: 400,
            message: "bad request"
        });
    }

    var reportData = {
        building_code: buildingCode,
        room_no: roomNumber,
        report_msg: reportMessage
    };

    try {
        db_handler.submitReport(reportData);
        res.status(201);
        res.type('json');
        console.log("yes");
        res.send(JSON.stringify(
            {
                status: "ok",
                data: "report submitted"
            }
        ));
    } catch (err) {
        utils.onFailure(res, {
            statusCode: 400,
            message: "bad request"
        });
    }

    /*
    We probably need to add more mechanisms to 
    send notifications to the admin
    */



}

async function createBuilding(req, res) {
    if (req.user.type != 'superadmin') {
        utils.onFailure(res, {
            statusCode: 401,
            message: "Unauthorized"
        });
        return
    }
    var buildingData = {
        building_code: req.body.building_code,
        building_name: req.body.building_name,
        building_address: req.body.building_address,
        open_times: req.body.open_times,
        close_times: req.body.close_times,
    }

    try {
        result = await db_handler.addBuilding(buildingData)
        utils.onSuccess(res, result)
    } catch (err) {
        utils.onFailure(res, err);
    }
}

async function delBuilding(req, res) {
    if (req.user.type != 'superadmin') {
        utils.onFailure(res, {
            statusCode: 401,
            message: "Unauthorized"
        });
        return
    }
    BuildingCode = req.params.building_code

    try {
        result = await db_handler.delBuilding(BuildingCode)
        utils.onSuccess(res, result)
    } catch (err) {
        utils.onFailure(res, err);
    }
}

async function createRoom(req, res) {
    if (req.user.type != 'admin' || !(req.user.adminBuildings.includes(req.params.building_code))) {
        utils.onFailure(res, {
            statusCode: 401,
            message: "Unauthorized"
        });
        return
    }
    var roomData = {
        building_code: req.params.building_code,
        _id: req.body.room_no,
        features: req.body.features,
        capacity: req.body.capacity
    }

    try {
        result = await db_handler.addRoom(roomData)
        utils.onSuccess(res, result)
    } catch (err) {
        utils.onFailure(res, err);
    }
}


async function delRoom(req, res) {
    if (req.user.type != 'admin' || !(req.user.adminBuildings.includes(req.params.building_code))) {
        utils.onFailure(res, {
            statusCode: 401,
            message: "Unauthorized"
        });
        return
    }
    roomData = {
        buildingCode: req.params.building_code,
        roomNo: req.params.room_no
    }

    try {
        result = await db_handler.delRoom(roomData)
        utils.onSuccess(res, result)
    } catch (err) {
        utils.onFailure(res, err);
    }
}


//Scheduler related functions

/**
 * Remove expired bookings
 */
async function removeExpiredBookings() {

    //Get current time
    const dateObj = new Date();
    var date = dateObj.getDate();
    var month = dateObj.getMonth() + 1;
    var year = dateObj.getFullYear();
    
    //Process data
    if (date < 10) {
        date = `0${date}`;
    } else {
        date = `${date}`;
    }

    if (month < 10) {
        month = `0${month}`;
    } else {
        month = `${month}`;
    }

    await db_handler.removeInvalidBookings(date, month, year);
}



// Helper Functions



// Interface exports
module.exports = {
    listStudyRooms,
    filterStudyRooms,
    bookStudyRooms,
    getSlots,
    getStudyRoomComment,
    reportRoom,
    initScheduler,
    deinitScheduler,
    createBuilding,
    delBuilding,
    createRoom,
    delRoom,
    waitlistStudyRooms
};
