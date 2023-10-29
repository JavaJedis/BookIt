const db_handler = require("./db_handler")
const utils = require("./utils");


async function listLectureHalls(req, res) {
    const buildingCode = req.params.building_code
    try {
        const result = await db_handler.getRooms(buildingCode, 'lecture_room_db')
        utils.onSuccess(res, result)
    } catch (error) {
        utils.onFailure(res, error)
    }
}


module.exports = {
    listLectureHalls
};