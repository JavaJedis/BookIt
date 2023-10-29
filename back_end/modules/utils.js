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


//Function Exports
module.exports = {
    consoleMsg,
    onSuccess,
    onFailure, 
    logInit, 
    logDeinit, 
    serverLog
}
