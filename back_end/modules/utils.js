/*
Utility functions module for BookIt 
written by Java Jedis
*/

//Import necessary modules
const fs = require("fs");

//Global variables
var logFileHandle;

//Definitions
const LOG_FILE_PATH = './bookitBE.log';

//Start of function definitions

/**
 * Initialize logger
 */
function logInit() {
    logFileHandle = fs.openSync(LOG_FILE_PATH, "as");
}

/**
 * Deinitialize logger
 */

function logDeinit() {
    fs.closeSync(logFileHandle);
}

/**
 * Print a console message for a module
 * @param {string} module 
 * @param {string} msg 
 */
function consoleMsg(module, msg) {
    const dateStr = new Date(new Date() - 3600 * 1000 * 7).toISOString();
    console.log(`[${dateStr}][${module}] ${msg}`);
}

/**
 * Print message to log file
 * @param {string} module 
 * @param {string} msg 
 */
function serverLog(module, logMsg) {
    //fs.writeSync(logFileHandle, `[${module}] ${logMsg}\n`);
    //fs.fsyncSync(logFileHandle);
    consoleMsg(module, logMsg);
}

// Helper function that handles successful requests. Takes 'express' response and the response data as parameters

function onSuccess(res, result) {
    res.status(200);
    res.type('json');
    res.send(JSON.stringify(
        {
            status: "ok",
            data: result
        }
    ));
}

// Helper function that handles failed requests. Takes 'express' response and the error causing failure as parameters

function onFailure(res, err) {
    if (!(err.statusCode)) {
        err.statusCode = 403
    }
    res.status(err.statusCode);
    res.type("json");
    res.send(JSON.stringify(
        {
            status: "error",
            data: err.message
        }
    ))
}

//Time representation handling functions

/**
 * Convert military time to decimal representation
 * @param {Array} time Military time in an integer array of size 2.
 * @returns Decimal representation of the time.
 */
function militaryTimeToDecimal(time) {

    if (time == null || !(time instanceof Array) || time.length !== 2) {
        throw Error("Invalid Arguments");
    }


    const hour = time[0];
    const min = time[1];

    if (hour == null || min == null) {
        throw Error("Invalid hour or min");
    }

    if (!Number.isInteger(hour) || !Number.isInteger(min)) {
        throw Error("Invalid hour or min");
    }

    if (min % 30 !== 0) {
        throw Error("Invalid hour or min");
    }

    return hour * 2 + min / 30;

}


//Function Exports
module.exports = {
    consoleMsg,
    onSuccess,
    onFailure,
    logInit,
    logDeinit,
    serverLog,
    militaryTimeToDecimal
}
