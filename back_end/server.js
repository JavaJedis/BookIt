//Server startup
const utils = require("./modules/utils");

//Global definitions
const MODULE_NAME = "SERVER-WORKER";

utils.serverLog(MODULE_NAME, "Starting Backend Server");



// Import necessary modules
require('dotenv').config({ path: '.env.backend' });
const https = require('https');
//const http = require('http');
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
let server;





// Express configurations
const app = express();
const PORT = 443;
app.use(bodyParser.json());


// API endpoints
app.get('/user/type', user_manager.userType);   //Done test
app.post('/user/login', user_manager.userLogin);    //Done test
app.get('/ils/:building_code', ils_manager.listIlsRooms);       //Done test
app.get('/filter', sroom_manager.filterStudyRooms); // Done test
app.get('/studyrooms/:building_code', sroom_manager.listStudyRooms);    //Done test
app.get('/studyrooms/:building_code/:room_no/slots', sroom_manager.getSlots);   //Done test
app.get('/studyrooms/:building_code/:room_no/comments', sroom_manager.getStudyRoomComment);     //Done test 
app.post('/studyrooms/:building_code/:room_no/report', user_manager.userAuth, sroom_manager.reportRoom);    //Done Test
app.get('/user/bookings', user_manager.userBookings);  // Done test
app.delete('/user/bookings/:id', user_manager.userAuth, user_manager.cancelBooking); // Done test
app.put('/user/bookings/:id', user_manager.userAuth, user_manager.confirmBooking);
app.post('/studyroom/book', user_manager.userAuth, sroom_manager.bookStudyRooms);        //Done test
app.post('/studyroom/waitlist', user_manager.userAuth, sroom_manager.waitlistStudyRooms);     // Done test
app.post('/studyrooms/:building_code/:room_no/comments', user_manager.userAuth, cmt_manager.sendStudyRoomComment);  //Done test
app.get('/lecturehalls/:building_code', lhall_manager.listLectureHalls);        //Done test

//Administration Endpoints
app.get('/user/admin', user_manager.listAdmins);        //Done test
app.post('/user/admin', user_manager.userAuth, user_manager.createAdmin);       //Done test
app.delete('/user/admin', user_manager.userAuth, user_manager.removeAdmin);     //Done test
app.get('/user/admin/:email/buildings', user_manager.getAdminBuildings);        //Done test
app.post('/user/admin/:email/buildings', user_manager.userAuth, user_manager.addBuildingAdmin);     //Done test
app.delete('/user/admin/:email/buildings', user_manager.userAuth, user_manager.removeBuildingAdmin);        //Done test
app.post('/studyrooms/building', user_manager.userAuth, sroom_manager.createBuilding)   //Done Test
app.delete('/studyrooms/:building_code', user_manager.userAuth, sroom_manager.delBuilding)  //Done Test
app.post('/studyrooms/:building_code/', user_manager.userAuth, sroom_manager.createRoom)
app.delete('/studyrooms/:building_code/:room_no', user_manager.userAuth, sroom_manager.delRoom) //Done Test



// database handler initialization function call
db_handler.dbh_init();
notif_manager.init();
sroom_manager.initScheduler();


// Express https listener
if (process.env.NODE_ENV === "test") {
    server = app.listen(80, () => {
        utils.serverLog(MODULE_NAME, `Server is running on http://localhost:${PORT}`);
    });
} else {
    server = https.createServer({
        key: fs.readFileSync("/home/dev/bookit_backend/certs/private.pem"),
        cert: fs.readFileSync("/home/dev/bookit_backend/certs/fullchain.crt")
    }, app).listen(PORT, () => {
        utils.serverLog(MODULE_NAME, `Server is running on https://localhost:${PORT}`);
    });
}


async function shutDown() {
    utils.serverLog(MODULE_NAME, "Shutting down backend service...");
    sroom_manager.deinitScheduler();
    notif_manager.deInit();
    await db_handler.dbh_deinit();
    await server.close();
    utils.serverLog(MODULE_NAME, "Backend Closed.");
    //process.exit();
}

// Close connection to database and terminate
process.on("SIGTERM", shutDown);

process.on("SIGINT", shutDown);

module.exports = {
    shutDown,
    app,
    db_handler,
}