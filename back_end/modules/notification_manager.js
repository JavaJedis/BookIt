//Import necessary modules
const admin = require('firebase-admin');
const utils = require('./utils');
const db_handler = require('./db_handler');
const schedule = require('node-schedule');



//Global variables
var app;
var reminderJobA;
var reminderJobB;

//Global Definitions
const FIREBASE_KEY_PATH = './firebase/firebase_key.json';
const MODULE_NAME = 'NOTIFICATION-MANAGER';

/**
 * Initialize notification manager
 */
function init() {
    //Init app
    app = admin.initializeApp(
        {
            credential: admin.credential.applicationDefault()
        }
    );
    //Init schedulers
    try {
        reminderJobA = schedule.scheduleJob('ReminderA', '50 * * * *', searchAndSendReminders);
        reminderJobB = schedule.scheduleJob('ReminderB', '20 * * * *', searchAndSendReminders);
    } catch (err) {
        utils.consoleMsg(MODULE_NAME, "Failed to initialize scheduler.");
        utils.consoleMsg(MODULE_NAME, `ErrMsg:\n${err}`);
        return false;
    }
    utils.consoleMsg(MODULE_NAME, "Notification Service Enabled.");
    return true;
}


function deInit() {
    app.delete().then(
        () => {
            utils.consoleMsg(MODULE_NAME, 'Notification Service Disabled');
            schedule.gracefulShutdown().then();
        }
    ).catch(
        err => {
            utils.consoleMsg(MODULE_NAME, 'Failed to disable notification service');
            utils.consoleMsg(MODULE_NAME, `ErrMsg:\n${err}`);
        }
    )
}

/**
 * Send notification to a specific device
 * @param {*} data Notification Data
 * @param {string} devToken Device Token
 * @returns true on success, false on failure
 */
async function sendNotification(data, devToken) {

    const messagingService = admin.messaging(app);
    try {
        await messagingService.send(
            {
                data: data, 
                token: devToken
            }
        );
        utils.consoleMsg(MODULE_NAME, `Notification sent to ${devToken}`);
        return true;
    } catch (err) {
        utils.consoleMsg(MODULE_NAME, `Failed to send notification to ${devToken}, see error message below.`);
        utils.consoleMsg(MODULE_NAME, err);
        return false;
    }

}

//Scheduling functions

/**
 * Search in databases and 
 * @param {*} time 
 */
async function searchAndSendReminders() {

    /*
    1. Convert time into indexes
    2. Search bookings
    3. Search user devices
    3. Send notifications
    4. Updated devtoken record
    */
    const currentDate = new Date();
    var bookingMin = currentDate.getMinutes();
    var bookingHour = currentDate.getHours();

    if (bookingMin > 30) {
        bookingMin = 0;
        bookingHour = bookingHour + 1;
    } else {
        bookingMin = 30;
    }

    //Get date and time for the next reminder
    const currentDateStr = `${currentDate.getDate()}-${currentDate.getMonth() + 1}-${currentDate.getFullYear()}`;
    const nextReminderTime = utils.militaryTimeToDecimal([0, 0]);
    const booking = await db_handler.findBookingByDate(currentDateStr);

    
    for (const [key, value] of Object.entries(booking)) {
        let updateList = [];
        let id;
        if (key == '_id') {
            id = value;
            continue;
        }

        if (value[nextReminderTime].length < 10)
            continue;

        const user = await db_handler.checkUser(value[nextReminderTime]);
        if (user == null || user.tokens == null) {
            continue;
        } 
        for (const [tokenIndex, devToken] of Object.entries(user.tokens)) {
            if (devToken == null)
                continue;
            const result = await sendNotification(
                {
                    type: 'booking-reminder', 
                    room: key, 
                    date: currentDateStr, 
                    hour: Number.toString(bookingHour), 
                    min: Number.toString(bookingMin)
                }, devToken
            );
            if (result) {
                updateList.push(devToken);
            }
        }

        //Update token list to remove useless tokens
        db_handler.updateUserTokens(id, updateList);

    }
}

/**
 * Send bulk notification to specified emails
 * @param {Array} emails 
 * @param {string} msg 
 */
async function sendBulkNotifications(emails, msg) {

    /*
    1. Get user profiles for devtokens
    2. Send msg to all devToken
    */

    for (const email of emails) {
        let userDoc;

        try { 
            userDoc = await db_handler.checkUser(email);
            if (userDoc == null) {
                utils.consoleMsg(MODULE_NAME, `Could not find user ${email}'s profile`);
                continue;
            }
        } catch (err) {
            utils.consoleMsg(MODULE_NAME, `Could not find user ${email}'s profile`);
            continue;
        }

        const devTokens = userDoc.tokens;
        for (devToken in devTokens) {
            if (devToken == null) {
                continue;
            }
            await sendNotification(
                {
                    type: 'Waitlist', 
                    msg: msg
                }, devToken
            );
        }
    }

}


module.exports = {
    init, 
    sendNotification, 
    sendBulkNotifications, 
    deInit
};

