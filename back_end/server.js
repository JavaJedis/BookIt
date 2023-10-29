//Server startup
const utils = require("/home/dev/bookit_backend/modules/utils");

//Global definitions
const MODULE_NAME = "SERVER-WORKER";

utils.serverLog(MODULE_NAME, "Starting Backend Server");



// Import necessary modules
const https = require('https');
const fs = require('fs');
const db_handler = require("/home/dev/bookit_backend/modules/db_handler");
const user_manager = require("./modules/user_manager");
const ils_manager = require("./modules/ils_manager");
const sroom_manager = require("./modules/sroom_manager");
const lhall_manager = require("./modules/lecture_room_manager");
const express = require('express');
const bodyParser = require('body-parser');
const cmt_manager = require("./modules/comment_manager");



// Express configurations
const app = express();
const PORT = 443;
app.use(bodyParser.json());


// API endpoints
app.get('/user/type', user_manager.userType);
app.post('/user/login', user_manager.userLogin);
app.get('/ils/:building_code', ils_manager.listIlsRooms);
// app.get('/filter', sroom_manager.filterStudyRooms);
app.get('/studyrooms/:building_code', sroom_manager.listStudyRooms);
app.get('/studyrooms/:building_code/:room_no/slots', sroom_manager.getSlots);
app.get('/studyrooms/:building_code/:room_no/comments', sroom_manager.getStudyRoomComment);
app.get('/user/bookings', user_manager.userBookings);
app.delete('/user/bookings/:id', user_manager.cancelBooking);
app.post('/studyroom/book', user_manager.userAuth, sroom_manager.bookStudyRooms);
app.post('/studyroom/:building_code/:room_no/comments', /*user_manager.userAuth, */cmt_manager.sendStudyRoomComment);
app.get('/lecturehalls/:building_code', lhall_manager.listLectureHalls);







// database handler initialization function call
db_handler.dbh_init();


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
    db_handler.dbh_deinit().then(() => {
        utils.serverLog(MODULE_NAME, "Backend Closed.");
        process.exit();
    }
    );
});

process.on("SIGINT", function () {
    utils.serverLog(MODULE_NAME, "Shutting down backend service...");
    db_handler.dbh_deinit().then(() => {
        utils.serverLog(MODULE_NAME, "Backend Closed.");
        process.exit();
    }
    );
});

