// Import Libraries
const db_handler = require("./db_handler");
const utils = require("./utils");


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
        utils.onFailure(res, error)
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
        const result = await db_handler.bookStudyRooms(bookingData)
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
    const filter_data = {
        start_time: req.query.start_time,
        duration: req.query.duration,
        day: req.query.day,
        lat: req.query.lat,
        lon: req.query.lon
    }

    try {
        const result = await db_handler.filterRoom(filter_data)
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

    

}

// Helper Functions



// Interface exports
module.exports = {
    listStudyRooms,
    //filterStudyRooms,
    bookStudyRooms,
    getSlots,
    getStudyRoomComment
};
