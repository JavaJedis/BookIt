// Import Libraries
const db_handler = require("./db_handler");
const axios = require('axios');
const utils = require("./utils");
const { ObjectId } = require('mongodb');


//Global Definition
MODULE_NAME = "USER-MANAGER";


/**
 * Get informal learning space room data
 * @param {*} req HTTPS request from the client
 * @param {*} res HTTPS response send to the client
 */
async function userLogin(req, res) {
    const token = req.body.token
    try {
        const response = await axios.get(`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${token}&key=AIzaSyDDmfi9t5Zd8-PwBMDOOyywBTOd8qPagbo`);
        const userInfo = {
            _id: response.data.email,
            type: 'user',
            booking_ids: []
        }
        const result = await db_handler.userLogin(userInfo)
        utils.onSuccess(res, result)
    } catch (error) {
        var err = new Error("Unauthorized")
        err.statusCode = 401
        utils.onFailure(res, err)
    }
}


// User Authentication middleware

async function userType(req, res) {
    const token = req.query.token;
    try {
        const response = await axios.get(`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${token}&key=AIzaSyDDmfi9t5Zd8-PwBMDOOyywBTOd8qPagbo`);
        const userEmail = response.data.email
        const user = await db_handler.checkUser(userEmail)
        const result = user.type
        utils.onSuccess(res, result)
    } catch (error) {
        var err = new Error("Unauthorized")
        err.statusCode = 401
        utils.onFailure(res, err)
    }
}

async function userBookings(req, res) {
    const token = req.query.token;
    try {
        // const response = await axios.get(`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${token}&key=AIzaSyDDmfi9t5Zd8-PwBMDOOyywBTOd8qPagbo`);
        // const userEmail = response.data.email
        // const user = await db_handler.checkUser(userEmail)
        const user = await db_handler.checkUser(token)
        const result = await db_handler.getBookings(user.booking_ids);
        utils.onSuccess(res, result);
    } catch (error) {
        var err = new Error("Unauthorized");
        err.statusCode = 401;
        utils.onFailure(res, err);
    }
}

async function cancelBooking(req, res) {
    const token = req.query.token
    const id = req.params.id.toString(16).padStart(24, '0')
    try {
        // const response = await axios.get(`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${token}&key=AIzaSyDDmfi9t5Zd8-PwBMDOOyywBTOd8qPagbo`);
        // const userEmail = response.data.email
        var user = await db_handler.checkUser(token)

        if ((user.booking_ids.filter(item => item.toHexString() === id).length) !== 1) {
            var err = new Error("Booking not found")
            err.statusCode = 404
            throw err
        }

        const result = await db_handler.cancelBooking(id, user)
        utils.onSuccess(res, result)

    } catch (error) {
        var err = new Error("Unauthorized")
        err.statusCode = 401
        if (error.statusCode === 404) {
            err = error
        }
        utils.onFailure(res, err)
    }
}

async function userAuth(req, res, next) {
    const token = req.body.token

    try {
        const response = await axios.get(`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${token}&key=AIzaSyDDmfi9t5Zd8-PwBMDOOyywBTOd8qPagbo`);
        const userEmail = response.data.email;
        const user = await db_handler.checkUser(userEmail);
        req.user = user;
        next();
    } catch (error) {
        var err = new Error("Unauthorized");
        err.statusCode = 401;
        utils.onFailure(res, err);
    }
}

// Interface exports

module.exports = {
    userLogin,
    userType,
    userAuth,
    userBookings,
    cancelBooking
};