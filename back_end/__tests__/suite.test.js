const { MongoClient } = require("mongodb");
const { MongoMemoryServer } = require("mongodb-memory-server");
const request = require("supertest");
jest.mock('axios');
const axios = require("axios");
let server = require("../server");
const app = "https://bookit.henrydhc.me";
let mongoMemServer;

/* Test data */
let testDataA = {
    _id: "henry@henry.co",
    type: "user",
    adminBuildings: []
};
let testDataB = {
    _id: "jimmy@jimmy.co",
    type: "admin",
    adminBuildings: [
        "FAKE",
        "REAL"
    ]
};
let testDataC = {
    _id: "aman@admin.ca",
    type: "user"
};
let testDataD = {
    _id: "tommy@admin.us",
    type: "superadmin",
    adminBuildings: ["NULL"]
}
let testDataE = {
    _id: "admin@testadmin.com",
    type: "admin"
};

/*
====================START OF USER MANAGER TESTS====================
*/


//Interface bookit.henrydhc.me/user/admin GET
describe("/user/admin GET request", () => {

    /**@type {MongoClient} */
    let memClient;

    beforeAll(async () => {
        mongoMemServer = await MongoMemoryServer.create(
            {
                instance: {
                    port: 25565
                }
            }
        );
        memClient = await MongoClient.connect(mongoMemServer.getUri());
        let targetCollection = memClient.db("users").collection("users");
        await targetCollection.insertMany([testDataA, testDataC]);
    });

    afterAll(async () => {
        await memClient.close();
    });

    test("Success: No Admin", async () => {
        /*
        Input: None
        Expected Status Code: 200
        Expected Behavior: Admin user information fetched from the database
        Expected Output: Empty admin list
        */
        let expected = [];
        let actual = await request(app).get("/user/admin");
        expect(actual.status).toBe(200);
        expect(actual.body).toEqual({
            status: "ok",
            data: expected
        });
    });

    test("Success: Has one admin", async () => {
        /*
        Input: None
        Expected Status Code: 200
        Expected Behavior: Admin user information fetched from the database
        Expected Output: A list with one admin
        */

        /* Prepare Test Data */
        let targetCollection = memClient.db("users").collection("users");
        targetCollection.insertOne(testDataB);

        let expected = ["jimmy@jimmy.co"];
        let res = await request(app).get("/user/admin");
        expect(res.status).toBe(200);
        expect(res.body).toEqual({
            status: "ok",
            data: expected
        });
    });

    test("Success: Has more than one admin", async () => {
        /*
        Input: None
        Expected Status Code: 200
        Expected Behavior: Admin user information fetched from the database
        Expected Output: A list of 2 admins
        */

        /* Prepare Test Data */
        let targetCollection = memClient.db("users").collection("users");
        targetCollection.insertOne(testDataE);

        let expected = ["jimmy@jimmy.co", "admin@testadmin.com"];
        let res = await request(app).get("/user/admin");
        expect(res.status).toBe(200);
        expect(res.body).toEqual(
            {
                status: "ok",
                data: expected
            }
        );
    })

});

//Interface bookit.henrydhc.me/user/type GET
describe("/user/type GET request",
    () => {

        let memClient;

        beforeAll(
            async () => {
                memClient = await MongoClient.connect(mongoMemServer.getUri());
                await memClient.db("users").collection("users").insertOne(testDataD);
            }
        );

        afterAll(
            async () => {
                jest.restoreAllMocks();
                await memClient.close();
            }
        )

        test("Success: Normal user type",
            async () => {
                /*
                Input: Normal account token
                Expected Status Code: 200
                Expected Behavior: Account type fetched from the database
                Expected Output: "user"
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "aman@admin.ca"
                        }
                    }
                );
                let expected = {
                    status: "ok",
                    data: "user"
                };
                let actual = await request(app).get("/user/type?token=fake");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Success: Admin user type",
            async () => {
                /*
                Input: Admin user token
                Expected Status Code: 200
                Expected Behavior: Account type fetached from the database
                Expected Output: "admin"
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "jimmy@jimmy.co"
                        }
                    }
                );
                let expected = {
                    status: "ok",
                    data: "admin"
                };
                let actual = await request(app).get("/user/type?token=fake");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Superadmin user type",
            async () => {
                /*
                Input: Superadmin user token
                Expected Status Code: 200
                Expected Behavior: Account type fetached from the database
                Expected Output: "admin"
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "tommy@admin.us"
                        }
                    }
                );
                let expected = {
                    status: "ok",
                    data: "superadmin"
                };
                let actual = await request(app).get("/user/type?token=fake");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("User does not exist",
            async () => {
                /*
                Input: Token of a invalid user
                Expected Status Code: 404
                Expected Behavior: None
                Expected Output: Error Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "fake@fake.f"
                        }
                    }
                );
                let expected = {
                    status: "error",
                    data: "Unauthorized"
                };
                let actual = await request(app).get("/user/type?token=fake");
                expect(actual.status).toBe(401);
                expect(actual.body).toEqual(expected);
            }
        );
    }
);

//Interface bookit.henrydhc.me/user/login POST
describe("/user/login POST request",
    () => {

        let memClient;

        beforeAll(
            async () => {
                memClient = await MongoClient.connect(mongoMemServer.getUri());
            }
        );

        afterAll(
            async () => {
                await jest.restoreAllMocks();
                await memClient.close();
            }
        );

        test("Success: New Account",
            async () => {
                /*
                Input: Token of a valid user
                Expected Status Code: 200
                Expected Behavior: New user added to the database
                Expected Output: Success Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "noob@n.ca"
                        }
                    }
                );

                let expected = {
                    status: "ok",
                    data: "User information saved"
                };
                let requestData = {
                    token: "faketok",
                    devToken: "fakeDevToken"
                };
                let actual = await request(app).post("/user/login")
                    .set("Content-type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Success: Account already exists",
            async () => {
                let expected = {
                    status: "ok",
                    data: "User exists"
                };
                let requestData = {
                    token: "faketok",
                    devToken: "fakeDevToken"
                };
                let actual = await request(app).post("/user/login")
                    .set("Content-type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Invalid token",
            async () => {
                await axios.get.mockImplementation(
                    () => {
                        throw Error();
                    }
                );
                let expected = {
                    status: "error",
                    data: "Unauthorized"
                };
                let requestData = {
                    token: "fake",
                    devToken: "fakedevT"
                };
                let actual = await request(app).post("/user/login")
                    .set("Content-type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(401);
                expect(actual.body).toEqual(expected);
            }
        );

    }
);

//Interface: bookit.henrydhc.me/user/admin POST request
describe("/user/admin POST request",
    () => {

        /**@type {MongoClient} */
        let memClient;

        beforeAll(async () => {
            memClient = await MongoClient.connect(mongoMemServer.getUri());
        });

        afterAll(async () => {
            await memClient.close();
            jest.restoreAllMocks();
        });

        test("Success: Normal user to admin",
            async () => {
                /*
                Input: Normal user email and superadmin token
                Expected Status Code: 201
                Expected Behavior: User type changed to "admin" in database
                Expected Output: Success Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "tommy@admin.us"
                        }
                    }
                );
                let expected = {
                    status: "ok",
                    data: "admin created"
                };
                let requestData = {
                    email: "henry@henry.co",
                    token: "fakeToken"
                };
                let actual = await request(app).post("/user/admin")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(201);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Target user is not normal user but admin user",
            async () => {
                /*
                Input: target admin email and superadmin token
                Expected Status Code: 409
                Expected Behavior: None
                Expected Output: Error Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "tommy@admin.us"
                        }
                    }
                );
                let expected = {
                    status: "error",
                    data: "User is not normal user"
                };
                let requestData = {
                    email: "henry@henry.co",
                    token: "fakeToken"
                };
                let actual = await request(app).post("/user/admin")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(409);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Target user is not normal user but superadmin",
            async () => {
                let expected = {
                    status: "error",
                    data: "User is not normal user"
                };
                let requestData = {
                    email: "tommy@admin.us",
                    token: "fakeToken"
                };
                let actual = await request(app).post("/user/admin")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(409);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Account not found",
            async () => {
                /*
                Input: Invalid target user email and superadmin token
                Expected Status Code: 409
                Expected Behavior: None
                Expected Output: Error Message
                */

                let expected = {
                    status: "error",
                    data: "User does not exist"
                };
                let requestData = {
                    email: "dumb@dumb.ca",
                    token: "fakeToken"
                };
                let actual = await request(app).post("/user/admin")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(409);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Current User is not superadmin but normal user",
            async () => {
                /*
                Input: Target user email and normal user token
                Expected Status Code: 409
                Expected Behavior: None
                Expected Output: Error Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "aman@admin.ca"
                        }
                    }
                );
                let expected = {
                    status: "error",
                    data: "Current user is not superadmin"
                };
                let requestData = {
                    email: "aman@admin.ca",
                    token: "fakeToken"
                };
                let actual = await request(app).post("/user/admin")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(409);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Current user is not superadmin but admin user",
            async () => {
                /*
                Input: Target normal user email and admin user token
                Expected Status Code: 409
                Expected Behavior: None
                Expected Output: Error Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "jimmy@jimmy.co"
                        }
                    }
                );
                let expected = {
                    status: "error",
                    data: "Current user is not superadmin"
                };
                let requestData = {
                    email: "aman@admin.ca",
                    token: "fakeToken"
                };
                let response = await request(app).post("/user/admin")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(response.status).toBe(409);
                expect(response.body).toEqual(expected);
            }
        );

        test("Failure: Invalid token",
            async () => {
                /*
                Input: target user email and bad token
                Expected Status Code: 401
                Expected Behavior: None
                Expected Output: Error Message
                */

                await axios.get.mockImplementation(
                    () => {
                        throw new Error();
                    }
                );
                let expected = {
                    status: "error",
                    data: "Unauthorized"
                };
                let requestData = {
                    email: "aman@admin.ca",
                    token: "fakeToken"
                };
                let response = await request(app).post("/user/admin")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(response.status).toBe(401);
                expect(response.body).toEqual(expected);
            }
        )
    }
);

//Interface bookit.henrydhc.me/user/admin DELETE
describe("/user/admin DELETE request",
    () => {

        /** @type {MongoClient} */
        let testClient;

        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
            }
        );

        afterAll(
            async () => {
                await testClient.close();
                await jest.restoreAllMocks();
            }
        );

        test("Success: Admin to normal user",
            async () => {
                /*
                Input: Target admin user and superadmin token
                Expected Status Code: 
                Expected Behavior: Target admin user converted to normal user
                Expected output: Success Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "tommy@admin.us"
                        }
                    }
                );
                let requestData = {
                    email: "jimmy@jimmy.co",
                    token: "fakeToken"
                };
                let expected = {
                    status: "ok",
                    data: "admin removed"
                };
                let actual = await request(app).delete("/user/admin")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Target user is not admin but normal user",
            async () => {
                /*
                Input: Target normal user and superadmin token
                Expected Status Code: 409
                Expected Behavior: None
                Expected Output: Error Message
                */

                let requestData = {
                    email: "jimmy@jimmy.co",
                    token: "fakeToken"
                };
                let expected = {
                    status: "error",
                    data: "User is not admin"
                };
                let actual = await request(app).delete("/user/admin")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(409);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Target user is not admin but superadmin",
            async () => {
                /*
                Input: Target superadmin user and superadmin token
                Expected Status Code: 409
                Expected Behavior: None
                Expected Output: Error Message
                */

                let requestData = {
                    email: "tommy@admin.us",
                    token: "fakeToken"
                };
                let expected = {
                    status: "error",
                    data: "User is not admin"
                };
                let actual = await request(app).delete("/user/admin")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(409);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: account not found",
            async () => {
                /*
                Input: Invalid target user and superadmin token
                Expected Status Code: 409
                Expected Behavior: None
                Expected Output: Error Message
                */

                let requestData = {
                    email: "dumb@dumb.com",
                    token: "fakeToken"
                };
                let expected = {
                    status: "error",
                    data: "User does not exist"
                };
                let actual = await request(app).delete("/user/admin")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(409);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Current user is not superadmin but normal user",
            async () => {
                /*
                Input: Target admin and normal user token
                Expected Status Code: 409
                Expected Behavior: None
                Expected Output: Error Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "jimmy@jimmy.co"
                        }
                    }
                );
                let requestData = {
                    email: "henry@henry.co",
                    token: "fakeToken"
                };
                let expected = {
                    status: "error",
                    data: "Current user is not superadmin"
                };
                let actual = await request(app).delete("/user/admin")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(409);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Current user is not superadmin but admin",
            async () => {
                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "henry@henry.co"
                        }
                    }
                );
                let requestData = {
                    email: "henry@henry.co",
                    token: "fakeToken"
                };
                let expected = {
                    status: "error",
                    data: "Current user is not superadmin"
                };
                let actual = await request(app).delete("/user/admin")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(409);
                expect(actual.body).toEqual(expected);
            }
        );

    }
);

describe("/user/admin/:email/buildings GET request",
    () => {
        /** @type {MongoClient} */
        let testClient;

        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
            }
        );

        afterAll(
            async () => {
                await testClient.db("users").dropDatabase();
                await testClient.close();
            }
        );

        test("Success: Admin does not have any building",
            async () => {
                /*
                Input: Admin email
                Expected Status Code: 200
                Expected Behavior: Building list fetched from the database
                Expected Output: List of building of the specified admin
                */

                let expected = {
                    status: "ok",
                    data: []
                };
                let actual = await request(app).get("/user/admin/henry@henry.co/buildings");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Success: Admin has one building",
            async () => {
                /*
                Input: Admin email
                Expected Status Code: 200
                Expected Behavior: Building list fetched from the database
                Expected Output: List of 1 building of the specified admin
                */

                /* Prepare Data */
                await testClient.db("users").collection("users").updateOne({ _id: "henry@henry.co" }, { $push: { adminBuildings: "DOM" } });

                let expected = {
                    status: "ok",
                    data: ["DOM"]
                };
                let actual = await request(app).get("/user/admin/henry@henry.co/buildings");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Success: Admin has more than one building",
            async () => {
                /*
                Input: Admin email
                Expected Status Code: 200
                Expected Behavior: Building list of the admin fetched from the database
                Expected Output: List of 2 buildings of the specified admin
                */

                /* Prepare Data */
                await testClient.db("users").collection("users").updateOne({ _id: "henry@henry.co" }, { $push: { adminBuildings: "BOB" } });

                let expected = {
                    status: "ok",
                    data: ["DOM", "BOB"]
                };
                let actual = await request(app).get("/user/admin/henry@henry.co/buildings");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        )



        test("Failure: Non-admin user but normal user",
            async () => {
                /*
                Input: Normal user email
                Expected Status Code: 409
                Expected Behavior: None
                Expected Output: Error Message
                */

                let expected = {
                    status: "error",
                    data: "User is not admin"
                };
                let actual = await request(app).get("/user/admin/jimmy@jimmy.co/buildings");
                expect(actual.status).toBe(409);
                expect(actual.body).toEqual(expected)
            }
        );

        test("Failure: Non-admin user but superadmin",
            async () => {
                /*
                Input: Superadmin user email
                Expected Status Code: 409
                Expected Behavior: None
                Expected Output: Error Message
                */

                let expected = {
                    status: "error",
                    data: "User is not admin"
                };
                let actual = await request(app).get("/user/admin/tommy@admin.us/buildings");
                expect(actual.status).toBe(409);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: account does not exist",
            async () => {
                /*
                Input: Invalid account email
                Expected Status: 404
                Expected Behavior: None
                Expected Output: Error Message
                */

                let expected = {
                    status: "error",
                    data: "User not found"
                };
                let actual = await request(app).get("/user/admin/dumb@noob.com/buildings");
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );
    }
);

describe("/user/admin/:email/buildings POST request",
    () => {

        beforeAll(
            async () => {

            }
        );

        afterAll(
            async () => {

            }
        );

        test("Sample",
            async () => {

            }
        );

    }
);



describe("/user/admin/:email/buildings DELETE request",
    () => {

        beforeAll(
            async () => {

            }
        );

        afterAll(
            async () => {

            }
        );

        test("Sample",
            async () => {

            }
        );


    }
);

/*
====================END OF USER MANAGER TESTS====================
*/

/*
====================START OF ILS MANAGER TESTS====================
*/

//Interface bookit.henrydhc.me/ils/:building_code GET
describe("/ils/:building_code GET request",
    () => {

        /** @type {MongoClient} */
        let testClient;
        /** @type {Db} */
        let targetDb;

        let roomA = {
            _id: 101,
            name: 'The Leon and Thea Koerner University Centre (UCEN) - Study Lounge',
            address: '6331 Crescent Road, Vancouver, BC V6T 1Z2',
            capacity: '45',
            description: "Find this bright 45 seat study lounge on the bottom level of UCEN. Seating faces UCEN's garden space, and the lounge area offers a group of couches, tables and open access to a small kitchen.",
        }

        let buildings = {
            type: 'all_ils_buildings',
            buildings: [{
                building_code: 'UCEN',
                building_name: 'West Mall Swing Space (SWNG) - 1st Floor Concourse ',
                address: '2175 West Mall, Vancouver, BC V6T 1Z4',
                lat: 49.2629965,
                lon: -123.254339
            }]
        }

        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
                targetDb = testClient.db("ils_db");
                await targetDb.collection("building_all").insertOne(buildings);
                await targetDb.collection("UCEN").insertOne(roomA);
            }
        );

        afterAll(
            async () => {
                await testClient.db("ils_db").dropDatabase();
                await testClient.close();
            }
        );

        test("Valid Building Code: The building has one room",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database
                Expected Output: List of a room
                */

                /* Prepare Data Here */

                let expected = {
                    status: "ok",
                    data: [roomA]
                };
                let actual = await request("https://bookit.henrydhc.me").get("/ils/UCEN");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Valid Building Code: Get all buildings",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database
                Expected Output: List of 2 Rooms
                */

                /* Prepare Data Here */
                delete buildings._id
                let expected = {
                    status: "ok",
                    data: [buildings]
                };
                let actual = await request("https://bookit.henrydhc.me").get("/ils/building_all");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Invalid Building Code: Building does not exist",
            async () => {
                /*
                Input: None
                Expected Status Code: 404
                Expected Behavior: Room information fetched from the database
                Expected Output: Error message
                */

                let expected = {
                    status: "error",
                    data: "Not Found"
                };
                let actual = await request("https://bookit.henrydhc.me").get("/ils/DUMB");
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );
    }
);

/*
====================END OF ILS MANAGER TESTS====================
*/

/*
====================START OF SROOM MANAGER TESTS====================
*/

let roomA = {
    _id: "101",
    building_code: "GAME",
    features: [
        "Free PS5s",
        "Unlimited laptops"
    ],
    comments: [
        "Place for gamers!",
        "I love free computers"
    ]
};

let roomB = {
    _id: "202",
    building_code: "GAME",
    features: [
        "Free cards",
        "Gambling",
        "7*24 hours"
    ],
    comments: [
        "I earn my pocket money here"
    ]
};

let roomC = {
    _id: "303",
    building_code: "GAME",
    features: [
        "Nothing"
    ],
    comments: [

    ]
};

let buildings = {
    type: 'all_studyroom_buildings',
    buildings: [{
        building_code: 'WCKL',
        building_name: 'Walter C. Koerner Library',
        building_address: '1958 Main Mall, Vancouver, BC V6T 1Z2',
        open_times: [
            730, 730, 730,
            730, 730, 1000,
            1000
        ],
        close_times: [
            2130, 2130,
            2130, 2130,
            1730, 1730,
            2130
        ],
        lat: 49.26654885,
        lon: -123.25507800348765
    },
    {
        building_code: 'ALSC',
        building_name: 'Abdul Ladha Science Student Centre',
        building_address: '2055 East Mall, Vancouver, BC V6T 1Z4',
        open_times: [
            800, 800, 800, 800,
            800, 0, 0
        ],
        close_times: [
            2000, 2000, 2000,
            2000, 1800, 0,
            0
        ],
        lat: 49.26607735,
        lon: -123.25137314363899
    }]
}

let testRoomA = {
    _id: '101',
    capacity: 5,
    features: ['TV with HDMI Connection', 'Whiteboard', 'Outlets'],
    open_times: [
        800, 800, 800, 800,
        800, 0, 0
    ],
    close_times: [
        2000, 2000, 2000,
        2000, 1800, 0,
        0
    ],
    building_code: 'ALSC',
    building_name: 'Abdul Ladha Science Student Centre',
    building_address: '2055 East Mall, Vancouver, BC V6T 1Z4',
    comments: []
}

let testRoomB = {
    _id: '102',
    capacity: 5,
    features: ['TV with HDMI Connection', 'Whiteboard', 'Outlets'],
    open_times: [
        800, 800, 800, 800,
        800, 0, 0
    ],
    close_times: [
        2000, 2000, 2000,
        2000, 1800, 0,
        0
    ],
    building_code: 'ALSC',
    building_name: 'Abdul Ladha Science Student Centre',
    building_address: '2055 East Mall, Vancouver, BC V6T 1Z4',
    comments: []
}

let testRoomC = {
    _id: '101',
    capacity: 10,
    features: ['Whiteboard', 'Outlets'],
    open_times: [
        730, 730, 730,
        730, 730, 1000,
        1000
    ],
    close_times: [
        2130, 2130,
        2130, 2130,
        1730, 1730,
        2130
    ],
    building_code: 'WCKL',
    building_name: 'Walter C. Koerner Library',
    building_address: '1958 Main Mall, Vancouver, BC V6T 1Z2',
    comments: ['Random comment. Trying to fill the 20 character limit.']
}

let testUserBooking = { "_id": "blah@blah.com", "type": "user", "booking_ids": [] }

let testUserWaitlist = { "_id": "wait@wait.com", "type": "user", "booking_ids": [] }



let testUser = {
    _id: "beef@0xdeadbeef.com",
    type: "user"
};

//Interface /studyrooms/:building_code/:room_no/comments POST
describe("/studyrooms/:building_code POST request",
    () => {

        /** @type {MongoClient} */
        let testClient;

        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
                await testClient.db("study_room_db").collection("GAME").insertOne(roomA);
                await testClient.db("users").collection("users").insertOne(testUser);
            }
        );

        afterAll(
            async () => {
                //await testClient.db("study_room_db").dropDatabase();
                //await testClient.db("users").dropDatabase();
                await testClient.close();
                jest.restoreAllMocks();
            }
        );

        test("Successfully uploads a comment",
            async () => {
                /*
                Input: Comment of a specific room with user data
                Expected Status Code: 201
                Expected Behavior: Comment is being added to the database
                Expected Output: Successful response message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: testUser._id
                        }
                    }
                );
                let expected = {
                    status: "ok",
                    data: "comment posted"
                };
                let requestData = {
                    comment: "This room is crazy",
                    token: "fakeToken"
                };
                let actual = await request(app).post("/studyrooms/GAME/101/comments")
                    .set('Content-Type', 'application/json')
                    .send(requestData);
                expect(actual.status).toBe(201);
                expect(actual.body).toEqual(expected);
                roomA.comments.push("This room is crazy");
            }
        );

        test("Failed to upload a comment: Invalid Building",
            async () => {
                /*
                Input: Comment is a invalid room with user data
                Expected Status Code: 404
                Expected Behavior: Comment is not being added to the database
                Expected Output: Fail response message
                */

                let expected = {
                    status: "error",
                    data: "Not found"
                };
                let requestData = {
                    comment: "This room is dumb",
                    token: "fakeToken"
                };
                let actual = await request(app).post("/studyrooms/DUMB/101/comments")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failed to upload a comment: Invalid Room",
            async () => {
                let expected = {
                    status: "error",
                    data: "Not found"
                };
                let requestData = {
                    comment: "This is a fake room!",
                    token: "fakeToken"
                };
                let actual = await request(app).post("/studyrooms/GAME/999/comments")
                    .set("Content-Type", "application/json")
                    .send(requestData);
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

    }
);


//Interface bookit.henrydhc.me/studyrooms/:building_code GET
describe("/studyrooms/:building_code GET request",
    () => {

        /** @type {MongoClient} */
        let testClient;
        /** @type {Db} */
        let targetDb;

        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
                targetDb = testClient.db("study_room_db");
            }
        );

        afterAll(
            async () => {
                await testClient.close();
            }
        );

        test("Valid Building Code: The building has one room",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database
                Expected Output: List of a room
                */

                let expected = {
                    status: "ok",
                    data: [roomA]
                };
                let actual = await request("https://bookit.henrydhc.me").get("/studyrooms/GAME");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Valid Building Code: The building has more than one room",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database
                Expected Output: List of 2 Rooms
                */

                /* Prepare Data Here */
                await targetDb.collection("GAME").insertOne(roomB);

                let expected = {
                    status: "ok",
                    data: [roomA, roomB]
                };
                let actual = await request("https://bookit.henrydhc.me").get("/studyrooms/GAME");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Invalid Building Code: Building does not exist",
            async () => {
                /*
                Input: None
                Expected Status Code: 404
                Expected Behavior: Room information fetched from the database
                Expected Output: Error message
                */

                let expected = {
                    status: "error",
                    data: "Failed to list study rooms"
                };
                let actual = await request("https://bookit.henrydhc.me").get("/studyrooms/DUMB");
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

    }
);

//Interface: bookit.henrydhc.me/:building_code/:room_no/comments
describe("/studyrooms/:building_code/:room_no/comments GET request",
    () => {

        /** @type {MongoClient} */
        let testClient;
        /** @type {Db} */
        let targetDb;

        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
                targetDb = testClient.db("study_room_db");
                await targetDb.collection("GAME").insertOne(roomC);
            }
        );

        afterAll(
            async () => {
                await testClient.close();
            }
        );

        test("Valid Building and Room Code: One comment",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database
                Expected Output: List of a room's comment. The list size is
                */

                let expected = {
                    status: "ok",
                    data: [
                        "I earn my pocket money here"
                    ]
                };
                let actual = await request("https://bookit.henrydhc.me").get("/studyrooms/GAME/202/comments");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Valid Building and Room Code: More than one comment",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Comment information fetched from the database
                Expected Output: List of 2 comments of a room
                */

                let expected = {
                    status: "ok",
                    data: [
                        "Place for gamers!",
                        "I love free computers",
                        "This room is crazy"
                    ]
                };
                let actual = await request(app).get("/studyrooms/GAME/101/comments");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Valid Building and Room Code: No comment",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Comment information fetched from the database
                Expected Output: An empty list
                */

                let expected = {
                    status: "ok",
                    data: []
                };
                let actual = await request(app).get("/studyrooms/GAME/303/comments");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Invalid Building Code",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database
                Expected Output: List of 2 Rooms
                */

                let expected = {
                    status: "error",
                    data: "not found"
                };
                let actual = await request("https://bookit.henrydhc.me").get("/studyrooms/GAM/101/comments");
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Invalid Room Code",
            async () => {
                /*
                Input: None
                Expected Status Code: 404
                Expected Behavior: Room information fetched from the database
                Expected Output: Error message
                */

                let expected = {
                    status: "error",
                    data: "not found"
                };
                let actual = await request("https://bookit.henrydhc.me").get("/studyrooms/GAME/104/comments");
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

    }
);

//Interface: bookit.henrydhc.me/studyroom/book
describe("/studyroom/book POST request",
    () => {

        /** @type {MongoClient} */
        let testClient;
        /** @type {Db} */
        let targetSRoomDb;
        let targetUserDb;


        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
                targetSRoomDb = testClient.db("study_room_db")
                targetUserDb = testClient.db("users")
                await targetSRoomDb.collection("building_all").insertOne(buildings);
                await targetSRoomDb.collection("WCKL").insertOne(testRoomC)
                await targetSRoomDb.collection("ALSC").insertOne(testRoomA)
                await targetSRoomDb.collection("ALSC").insertOne(testRoomB)
                await targetUserDb.collection("users").insertOne(testUserBooking)
            }
        );

        afterAll(
            async () => {
                await testClient.db("users").dropDatabase();
                await testClient.db("study_room_db").dropDatabase();
                await testClient.close();
            }
        );

        test("Book study room with valid data",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database
                Expected Output: List of a room's comment. The list size is
                */

                const bookingData = {
                    date: "01-12-2023",
                    startTime: "0800",
                    endTime: "0830",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking.email
                }

                let expected = {
                    status: "ok",
                    data: "booked!"
                };
                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));
                let actual = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Book previously booked slot",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Comment information fetched from the database
                Expected Output: List of 2 comments of a room
                */

                const bookingData = {
                    date: "01-12-2023",
                    startTime: "0800",
                    endTime: "0830",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking.email
                }
                let expected = {
                    status: "error",
                    data: "Unavailable Timeslots"
                };

                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));
                let actual = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);
                console.log(actual)
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Invalid Room Details",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Comment information fetched from the database
                Expected Output: An empty list
                */
                const bookingData = {
                    date: "01-12-2023",
                    startTime: "0800",
                    endTime: "0830",
                    buildingCode: "ALS",
                    roomNo: "101",
                    token: testUserBooking.email
                }
                let expected = {
                    status: "error",
                    data: "Room Not Found"
                };

                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));

                let actual = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);

            }
        );

        test("Invalid date",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database
                Expected Output: List of 2 Rooms
                */
                const bookingData = {
                    date: "01-12-2022",
                    startTime: "0830",
                    endTime: "0900",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking.email
                }
                let expected = {
                    status: "error",
                    data: "Invalid Date/Time"
                };

                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));

                let actual = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Booking when building is closed",
            async () => {
                /*
                Input: None
                Expected Status Code: 404
                Expected Behavior: Room information fetched from the database
                Expected Output: Error message
                */
                const bookingData = {
                    date: "01-12-2022",
                    startTime: "0730",
                    endTime: "0800",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking.email
                }
                let expected = {
                    status: "error",
                    data: "Invalid Date/Time"
                };

                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));

                let actual = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

    }
);

//Interface: bookit.henrydhc.me/studyroom/waitlist
describe("/studyroom/waitlist POST request",
    () => {

        /** @type {MongoClient} */
        let testClient;
        /** @type {Db} */
        let targetSRoomDb;
        let targetUserDb;


        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
                targetSRoomDb = testClient.db("study_room_db")
                targetUserDb = testClient.db("users")
                await targetSRoomDb.collection("building_all").insertOne(buildings);
                await targetSRoomDb.collection("WCKL").insertOne(testRoomC)
                await targetSRoomDb.collection("ALSC").insertMany([testRoomA, testRoomB])
                await targetUserDb.collection("users").insertMany([testUserBooking, testUserWaitlist])
            }
        );

        afterAll(
            async () => {
                await testClient.close();
            }
        );

        test("Waitlist study room with valid data",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database
                Expected Output: List of a room's comment. The list size is
                */

                const bookingData = {
                    date: "01-12-2023",
                    startTime: "0800",
                    endTime: "0830",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking.email
                }

                let expected = {
                    status: "ok",
                    data: "Successfully added to the waitlist"
                };
                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));
                let response = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);
                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserWaitlist._id
                        }
                    }
                ));
                let actual = await request(app)
                    .post("/studyroom/waitlist")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);

                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Waitlist an unbooked slot",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Comment information fetched from the database
                Expected Output: List of 2 comments of a room
                */

                const bookingData = {
                    date: "01-12-2023",
                    startTime: "0830",
                    endTime: "0900",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking.email
                }
                let expected = {
                    status: "error",
                    data: "Booking is unavailable for waitlisting"
                };

                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));

                let actual = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Waitlist a slot that is previously booked by same user",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Comment information fetched from the database
                Expected Output: An empty list
                */
                const bookingData = {
                    date: "01-12-2023",
                    startTime: "0800",
                    endTime: "0830",
                    buildingCode: "ALS",
                    roomNo: "101",
                    token: testUserBooking.email
                }
                let expected = {
                    status: "error",
                    data: "Room Not Found"
                };

                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));

                let actual = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);

            }
        );

        test("Invalid date",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database
                Expected Output: List of 2 Rooms
                */
                const bookingData = {
                    date: "01-12-2022",
                    startTime: "0830",
                    endTime: "0900",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking.email
                }
                let expected = {
                    status: "error",
                    data: "Invalid Date/Time"
                };

                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));

                let actual = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Booking when building is closed",
            async () => {
                /*
                Input: None
                Expected Status Code: 404
                Expected Behavior: Room information fetched from the database
                Expected Output: Error message
                */
                const bookingData = {
                    date: "01-12-2022",
                    startTime: "0730",
                    endTime: "0800",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking.email
                }
                let expected = {
                    status: "error",
                    data: "Invalid Date/Time"
                };

                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));

                let actual = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

    }
);

//Interface bookit.henrydhc.me/lecturehalls/:building_code GET
describe("/lecturehalls/:building_code GET request",
    () => {

        /** @type {MongoClient} */
        let memClient;
        /** @type {Db} */
        let targetDb;

        let roomA = {
            _id: 101,
            'room code': '100',
            'building code': 'UCEN',
            'building name': 'Wesbrook',
            address: '6174 University Boulevard, Vancouver, BC V6T 1Z3',
            hours: 'Mon to Fri: 7:30AM - 5:00PM, Sat/Sun/Holidays: Closed',
            capacity: '325',
            classroom_image_url: 'https://learningspaces.ubc.ca/sites/learningspaces.ubc.ca/files/styles/informal_list/public/classroom-images/WESB%20100%20%282%20of%204%29.JPG?itok=DLojjPI0',
            unavailable_times: {
                Mon: ['00:00 - 7:30', '9:00 - 24:00'],
                Tue: ['00:00 - 7:30', '8:00 - 24:00'],
                Wed: ['00:00 - 7:30', '9:00 - 24:00'],
                Thu: ['00:00 - 7:30', '8:00 - 24:00'],
                Fri: ['00:00 - 7:30', '9:00 - 16:00', '17:00 - 24:00'],
                Sat: ['00:00 - 24:00'],
                Sun: ['00:00 - 24:00']
            }
        }


        let buildings = {
            type: 'all_ils_buildings',
            buildings: [{
                building_code: 'UCEN',
                building_name: 'West Mall Swing Space (SWNG) - 1st Floor Concourse ',
                address: '2175 West Mall, Vancouver, BC V6T 1Z4',
                lat: 49.2629965,
                lon: -123.254339
            }]
        }

        beforeAll(
            async () => {
                memClient = await MongoClient.connect(mongoMemServer.getUri());
                targetDb = memClient.db("lecture_room_db");
                await targetDb.collection("building_all").insertOne(buildings);
                await targetDb.collection("UCEN").insertOne(roomA);
            }
        );

        afterAll(
            async () => {
                await memClient.close();
                await server.shutDown();
                await mongoMemServer.stop();
            }
        );

        test("Valid Building Code: The building has one room",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database
                Expected Output: List of a room
                */

                /* Prepare Data Here */

                let expected = {
                    status: "ok",
                    data: [roomA]
                };
                let actual = await request("https://bookit.henrydhc.me").get("/lecturehalls/UCEN");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Valid Building Code: Get all buildings",
            async () => {
                /*
                Input: None
                Expected Status Code: 200
                Expected Behavior: Building information fetched from the database
                Expected Output: List of 1 Building
                */

                /* Prepare Data Here */
                delete buildings._id
                let expected = {
                    status: "ok",
                    data: [buildings]
                };
                let actual = await request("https://bookit.henrydhc.me").get("/lecturehalls/building_all");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Invalid Building Code: Building does not exist",
            async () => {
                /*
                Input: None
                Expected Status Code: 404
                Expected Behavior: Room information fetched from the database
                Expected Output: Error message
                */
                let expected = {
                    status: "error",
                    data: "Not Found"
                };
                let actual = await request("https://bookit.henrydhc.me").get("/lecturehalls/DUMB");
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );
    }
);