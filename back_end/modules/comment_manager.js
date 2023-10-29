//Import necessary libraries
const { commentUploader } = require('./db_handler');
const utils = require('./utils');


//Global Definitions
MODULE_NAME = 'COMMENT-MANAGER';
MAX_BUILDING_CODE_LEN = 4;
MAX_ROOM_NUM = 4;
MAX_COMMENT_LEN = 600;


//Show necessary info when required
utils.consoleMsg(MODULE_NAME, '==========COMMENT MANAGER==========');
utils.consoleMsg(MODULE_NAME, `Setting building code limit to ${MAX_BUILDING_CODE_LEN} characters.`);
utils.consoleMsg(MODULE_NAME, `Setting room number limit to ${MAX_ROOM_NUM} characters.`);
utils.consoleMsg(MODULE_NAME, `Setting comment limit to ${MAX_COMMENT_LEN} characters.`);
utils.consoleMsg(MODULE_NAME, '===================================');


//Function definitions

/**
 * Make a comment for a specific study room.
 * @param {*} req Comment POST request
 * @param {*} res Result response
 */
function sendStudyRoomComment(req, res) {

    /*
    Possible response codes:
        - 404 Not Found -> Cannot find building and rooms
        - 201 Created -> Comment sent
        - 429 Too many requests
    */
    /*if (!studyRoomCommentReqCheck(req)) {
        utils.onFailure(res, {
            statusCode: 404, 
            message: "Not Found"
        });
    }*/

    console.log(req.body);
    const data = {
        building: req.params.building_code, 
        room: req.params.room_no, 
        comment: req.body.comment
    }
    console.log(data)
    if (commentUploader(data)) {
        res.status(201);
        res.type('json');
        res.send(JSON.stringify(
            {
                status: "ok",
                data: "comment posted"
            }
        ));
        return;
    }
    utils.onFailure(res, {
        statusCode: 404, 
        message: "Not Found"
    });
}

/**
 * Check whether the comment sending 
 * request for study room is valid
 * @param {Request} req POST request for sending
 *                studyroom comment
 * @returns 
 */
function studyRoomCommentReqCheck(req) {

    /*
    Check whether the body and its members are null
    or empty.
    */
    if (req.body == null) {
        console.log("fuck");
        return false;
    }
    var reqBody = req.body;
    var usrToken = reqBody.token;
    var buildingCode = req.params.building_code;
    var roomNum = req.params.room_num;
    var cmtData = reqBody.comment;
    /*
    if (usrToken == null || usrToken == "") {
        return false;
    }
    */

    if (buildingCode == null || buildingCode == ""
        || buildingCode.length > MAX_BUILDING_CODE_LEN) {
        return false;
    }

    if (roomNum == null || roomNum == ""
        || roomNum.length > MAX_ROOM_NUM) {
        return false;
    }

    if (cmtData == null || cmtData == ""
        || cmtData.length > MAX_COMMENT_LEN) {
        return false;
    }

    return true;

}


//Export functions
module.exports = {
    sendStudyRoomComment
};
