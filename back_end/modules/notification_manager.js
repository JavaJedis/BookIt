//Import necessary modules
const admin = require('firebase-admin');
const utils = require('./utils');
const db_handler = require("./db_handler");
const schedule = require('node-schedule');

const serviceAccount = {
    type: 'service_account',
    project_id: process.env.FIREBASE_PROJECT_ID,
    private_key_id: process.env.FIREBASE_KEY_ID,
    private_key: process.env.FIREBASE_KEY.replace(/\\n/g, '\n'),
    client_email: process.env.FIREBASE_CLIENT_EMAIL,
    client_id: process.env.FIREBASE_CLIENT_ID,
    auth_uri: "https://accounts.google.com/o/oauth2/auth",
    token_uri: "https://oauth2.googleapis.com/token",
    auth_provider_x509_cert_url: "https://www.googleapis.com/oauth2/v1/certs",
    client_x509_cert_url: process.env.FIREBASE_CERT_URL,
    universe_domain: "googleapis.com"
};



//Global variables
var app;


//Global Definitions
const MODULE_NAME = 'NOTIFICATION-MANAGER';



/**
 * Initialize notification manager
 */
function init() {
    //Init app
    app = admin.initializeApp(
        {
            credential: admin.credential.cert(serviceAccount),
        }
    );
    //Init schedulers
    try {
        schedule.scheduleJob('ReminderA', '36 * * * *', searchAndSendReminders);
        schedule.scheduleJob('ReminderB', '27 * * * *', searchAndSendReminders);
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
async function sendNotification(title, body, devToken) {

    const messagingService = admin.messaging(app);
    try {
        const req = {
            notification: {
                title,
                body
            },
            token: devToken
        }
        await messagingService.send(
            req
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
    var minStr;
    var hourStr;

    //Dealing with time and date string
    if (bookingMin > 30) {
        bookingMin = 0;
        minStr = '00';
        bookingHour = bookingHour + 1;
        hourStr = `${bookingHour}`;
    } else {
        bookingMin = 30;
        minStr = '30';
        hourStr = `${bookingHour}`;
    }

    var date;
    var month;
    if (currentDate.getDate() < 10) {
        date = `${0}${currentDate.getDate()}`
    } else {
        date = `${currentDate.getDate()}`
    }

    if (currentDate.getMonth() < 9) {
        month = `${0}${currentDate.getMonth() + 1}`
    } else {
        month = `${currentDate.getMonth() + 1}`
    }

    utils.consoleMsg(MODULE_NAME, `Send reminders for bookings @ [${bookingHour}, ${bookingMin}]`);

    //Get date and time for the next reminder





    const currentDateStr = `${date}-${month}-${currentDate.getFullYear()}`;
    const nextReminderTime = utils.militaryTimeToDecimal([bookingHour, bookingMin]);
    const booking = await db_handler.findBookingByDate(currentDateStr);
    if (booking == null) {
        return;
    }
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
            return;
        }
        for (const devToken of Object.entries(user.tokens)) {
            if (devToken[1] == null)
                continue;
            let success = await sendNotification("Booking Reminder",
                `You have a booking on room ${key} at ${hourStr}:${minStr} today.`
                , devToken[1]);
            if (success) {
                updateList.push(devToken[1]);
            }

        }

        //Update token list to remove useless tokens
        await db_handler.updateUserTokens(id, updateList);
    }
}

/**
 * Send bulk notification to specified emails
 * @param {Array} emails 
 * @param {string} msg 
 */
async function sendBulkNotifications(emails, title, msg) {

    /*
    1. Get user profiles for devtokens
    2. Send msg to all devToken
    */

    for (const email of emails) {
        let userDoc;
        try {
            userDoc = await db_handler.checkUser(email)
        } catch (err) {
            utils.consoleMsg(MODULE_NAME, `Could not find user ${email}'s profile`);
            utils.consoleMsg(MODULE_NAME, `ErrMsg: ${err}`);
            continue;
        }

        const devTokens = userDoc.tokens;
        for (const devToken of devTokens) {
            if (devToken == null) {
                continue;
            }
            await sendNotification(title, msg, devToken);
        }
    }

}

module.exports = {
    init,
    deInit,
    sendNotification,
    sendBulkNotifications
}

