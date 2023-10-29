var admin = require("firebase-admin");
var serviceAccount = require("../firebase_key.json");
const utils = require("./utils");
var fb_app;

//Global Definitions
const MODULE_NAME = "FCM HANDLER";


/**
 * Initialize firebase 
 */
function fbInit() {
    fb_app = admin,admin.initializeApp(
        {
            credential: admin.credential.cert(serviceAccount)
        }
    );
}

function fbSendMsgToOneDevice(devRegToken, data) {
    admin.messaging(fb_app).send(
        {
            data: data, 
            token: devRegToken
        }
    ).then(
        (res) => {
            utils.consoleMsg(MODULE_NAME, `Cloud Message ${res.split("/")[3]} sent to ${devRegToken}`);
            return true;
        }
    ).catch(
        (err) => {
            utils.consoleMsg(MODULE_NAME, "Failed to send cloud message, see error msg below.");
            utils.consoleMsg(MODULE_NAME, `Error msg:\n ${err}`);
            return false;
        }
    )
}
