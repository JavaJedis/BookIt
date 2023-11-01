// Import Libraries
const db_handler = require("./db_handler");
const axios = require('axios');
const utils = require("./utils");


//Global Definition
MODULE_NAME = "USER-MANAGER";


/**
 * Get informal learning space room data
 * @param {*} req HTTPS request from the client
 * @param {*} res HTTPS response send to the client
 */
async function userLogin(req, res) {
    const token = req.body.token
    const devToken = req.body.device_token;
    try {
        const response = await axios.get(`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${token}&key=${process.env.GOOGLE_OAUTH_TOKEN}`);
        const userInfo = {
            _id: response.data.email,
            type: 'user',
            booking_ids: [],
            devToken: devToken
        }
        const result = await db_handler.userLogin(userInfo);
        utils.consoleMsg(MODULE_NAME, `${response.data.email} logged in with device token ${devToken}`);
        utils.onSuccess(res, result)
    } catch (error) {
        var err = new Error("Unauthorized")
        err.statusCode = 401
        utils.onFailure(res, err);
    }
}


// User Authentication middleware

async function userType(req, res) {
    const token = req.query.token;
    try {
        const response = await axios.get(`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${token}&key=${process.env.GOOGLE_OAUTH_TOKEN}`);
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
        const response = await axios.get(`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${token}&key=${process.env.GOOGLE_OAUTH_TOKEN}`);
        const userEmail = response.data.email
        const user = await db_handler.checkUser(userEmail)
        const result = await db_handler.getBookings(user.booking_ids);
        utils.onSuccess(res, result);
    } catch (error) {
        var err = new Error("Unauthorized");
        err.statusCode = 401;
        utils.onFailure(res, err);
    }
}

async function confirmBooking(req, res) {
    const confirmData = {
        ID: req.params.id,
        lat: req.body.lat,
        lon: req.body.lon,
        user: req.user
    }
    try {
        const result = await db_handler.confirmBooking(confirmData)
        utils.onSuccess(res, result)
    } catch (error) {
        res.status(error.statusCode);
        res.type("json");
        res.send(JSON.stringify(
            {
                status: "error",
                data: error.message
            }
        ))
    }
}

async function cancelBooking(req, res) {
    const token = req.query.token
    const id = req.params.id.toString(16).padStart(24, '0')
    try {
        const response = await axios.get(`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${token}&key=${process.env.GOOGLE_OAUTH_TOKEN}`);
        const userEmail = response.data.email
        var user = await db_handler.checkUser(userEmail)

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
    var token = req.body.token
    if (!token) {
        token = req.query.token
    }

    try {
        const response = await axios.get(`https://www.googleapis.com/oauth2/v3/tokeninfo?id_token=${token}&key=${process.env.GOOGLE_OAUTH_TOKEN}`);
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


//Admin methods


function createAdmin(req, res) {

    const email = req.body.email;

    if (email == null) {
        utils.onFailure(res, {
            statusCode: 400,
            message: "invalid body data"
        });
    }

    //Here call db_handler to change data

    db_handler.addAdmin(email).then(
        result => {
            if (result) {
                res.status(201);
                res.type('json');
                res.send(JSON.stringify(
                    {
                        status: "ok",
                        data: "admin created"
                    }
                ));
            } else {
                utils.onFailure(res,
                    {
                        statusCode: 500,
                        message: "operation failed"
                    });
            }
        }
    );

}

function removeAdmin(req, res) {

    const email = req.body.email;

    if (email == null) {
        utils.onFailure(res, {
            statusCode: 400,
            message: "invalid body data"
        });
        return;
    }

    db_handler.delAdmin(email).then(
        result => {

            if (result) {
                res.status(200);
                res.type('json');
                res.send(JSON.stringify(
                    {
                        status: "ok",
                        data: "admin removed"
                    }
                ));
            } else {
                utils.onFailure(res,
                    {
                        statusCode: 500,
                        message: "operation failed"
                    });
            }
        }
    );

}

function addBuildingAdmin(req, res) {

    const email = req.params.email;
    const building = req.body.building;

    if (email == null || building == null) {
        utils.onFailure(res, {
            statusCode: 400,
            message: "invalid body data"
        });
        return;
    }

    db_handler.addBuildingAdmin(email, building).then(
        result => {

            if (result) {
                res.status(200);
                res.type('json');
                res.send(JSON.stringify(
                    {
                        status: "ok",
                        data: "building added to the admin"
                    }
                ));
            } else {
                utils.onFailure(res,
                    {
                        statusCode: 500,
                        message: "operation failed"
                    });
            }

        }
    );

}

function removeBuildingAdmin(req, res) {

    const email = req.params.email;
    const building = req.body.building;

    if (email == null || building == null) {
        utils.onFailure(res, {
            statusCode: 400,
            message: "invalid data"
        });
        return;
    }

    db_handler.delBuildingAdmin(email, building).then(
        result => {

            if (result) {
                res.status(200);
                res.type('json');
                res.send(JSON.stringify(
                    {
                        status: "ok",
                        data: "building removed from the admin"
                    }
                ));
            } else {
                utils.onFailure(res,
                    {
                        statusCode: 500,
                        message: "operation failed"
                    });
            }

        }
    );

}

function getAdminBuildings(req, res) {

    const email = req.params.email;

    if (email == null) {
        utils.onFailure(res, {
            statusCode: 400,
            message: "invalid data"
        });
    }

    db_handler.getAdminBuildings(email).then(
        result => {
            if (result != null) {
                res.status(200);
                res.type('json');
                res.send(JSON.stringify(
                    {
                        status: "ok",
                        data: result
                    }
                ));
                return
            }

            utils.onFailure(res,
                {
                    statusCode: 500,
                    message: "operation failed"
                });

            return;
        }
    );





}
// Interface exports

module.exports = {
    userLogin,
    userType,
    userAuth,
    userBookings,
    cancelBooking,
    confirmBooking,
    createAdmin,
    removeAdmin,
    addBuildingAdmin,
    removeBuildingAdmin,
    getAdminBuildings
};