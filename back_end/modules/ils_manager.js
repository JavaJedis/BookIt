// Import Libraries
const db_handler = require("./db_handler");
const utils = require("./utils");


/**
 * Get informal learning space room data
 * @param {*} req HTTPS request from the client
 * @param {*} res HTTPS response send to the client
 */
async function listIlsRooms(req, res) {
    const buildingCode = req.params.building_code
    try {
        const result = await db_handler.getRooms(buildingCode, 'ils_db')
        utils.onSuccess(res, result)
    } catch (error) {
        utils.onFailure(res, error)
    }
}

// Interface exports

module.exports = {
    listIlsRooms
};