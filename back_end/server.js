//Server startup
const utils = require("./modules/utils");

//Global definitions
const MODULE_NAME = "SERVER-WORKER";

utils.serverLog(MODULE_NAME, "Starting Backend Server");



// Import necessary modules
require('dotenv').config({ path: '.env.backend' });
const https = require('https');
const fs = require('fs');
const db_handler = require("./modules/db_handler");
const user_manager = require("./modules/user_manager");
const ils_manager = require("./modules/ils_manager");
const sroom_manager = require("./modules/sroom_manager");
const lhall_manager = require("./modules/lecture_room_manager");
const express = require('express');
const bodyParser = require('body-parser');
const cmt_manager = require("./modules/comment_manager");
const notif_manager = require('./modules/notification_manager');





// Express configurations
const app = express();
const PORT = 443;
app.use(bodyParser.json());


// API endpoints
app.get('/user/type', user_manager.userType);
app.post('/user/login', user_manager.userLogin);
app.get('/ils/:building_code', ils_manager.listIlsRooms);
app.get('/filter', sroom_manager.filterStudyRooms);
app.get('/studyrooms/:building_code', sroom_manager.listStudyRooms);
app.get('/studyrooms/:building_code/:room_no/slots', sroom_manager.getSlots);
app.get('/studyrooms/:building_code/:room_no/comments', sroom_manager.getStudyRoomComment);
app.post('/studyrooms/:building_code/:room_no/report', user_manager.userAuth, sroom_manager.reportRoom);
app.get('/user/bookings', user_manager.userBookings);
app.delete('/user/bookings/:id', user_manager.cancelBooking);
app.put('/user/bookings/:id', user_manager.userAuth, user_manager.confirmBooking);
app.post('/studyroom/book', user_manager.userAuth, sroom_manager.bookStudyRooms);
app.post('/studyroom/waitlist', user_manager.userAuth, sroom_manager.waitlistStudyRooms);
app.post('/studyrooms/:building_code/:room_no/comments', user_manager.userAuth, cmt_manager.sendStudyRoomComment);
app.get('/lecturehalls/:building_code', lhall_manager.listLectureHalls);

//Administration Endpoints
app.get('/user/admin', user_manager.listAdmins);
app.post('/user/admin', user_manager.userAuth, user_manager.createAdmin);
app.delete('/user/admin', user_manager.userAuth, user_manager.removeAdmin);
app.get('/user/admin/:email/buildings', user_manager.getAdminBuildings);
app.post('/user/admin/:email/buildings', user_manager.userAuth, user_manager.addBuildingAdmin);
app.delete('/user/admin/:email/buildings', user_manager.userAuth, user_manager.removeBuildingAdmin);
app.post('/studyrooms/building', user_manager.userAuth, sroom_manager.createBuilding)
app.delete('/studyrooms/:building_code', user_manager.userAuth, sroom_manager.delBuilding)
app.post('/studyrooms/:building_code/', user_manager.userAuth, sroom_manager.createRoom)
app.delete('/studyrooms/:building_code/:room_no', user_manager.userAuth, sroom_manager.delRoom)



// database handler initialization function call
db_handler.dbh_init();
notif_manager.init();
sroom_manager.initScheduler();


// Express https listener
https.createServer({
    key: fs.readFileSync("/home/dev/bookit_backend/certs/private.pem"),
    cert: fs.readFileSync("/home/dev/bookit_backend/certs/fullchain.crt")
}, app).listen(PORT, () => {
    utils.serverLog(MODULE_NAME, `Server is running on https://localhost:${PORT}`);
});


// Close connection to database and terminate
process.on("SIGTERM", function () {
    utils.serverLog(MODULE_NAME, "Shutting down backend service...");
    sroom_manager.deinitScheduler();
    notif_manager.deInit();
    db_handler.dbh_deinit().then(() => {
        utils.serverLog(MODULE_NAME, "Backend Closed.");
        process.exit(0);
    }
    );
});

process.on("SIGINT", function () {
    utils.serverLog(MODULE_NAME, "Shutting down backend service...");
    sroom_manager.deinitScheduler();
    notif_manager.deInit();
    db_handler.dbh_deinit().then(() => {
        utils.serverLog(MODULE_NAME, "Backend Closed.");
        process.exit(0);
    }
    );
});

