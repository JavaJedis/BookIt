const { MongoClient } = require("mongodb");
const { MongoMemoryServer } = require("mongodb-memory-server");
const request = require("supertest");
jest.mock('axios');
const axios = require("axios");
let server = require("../server");
const app = "http://localhost:80";
let mongoMemServer;


var formatter = new Intl.DateTimeFormat('en-US', { timeZone: 'America/Vancouver' });
var currentDate = formatter.format(new Date());
const today = new Date(currentDate);
let day = String(today.getDate()).padStart(2, '0');
let month = String(today.getMonth() + 1).padStart(2, '0'); // Months are zero-based
let year = today.getFullYear();

const formattedDateTod = `${day}-${month}-${year}`;

const tomorrow = new Date(today);
tomorrow.setDate(today.getDate() + 1);

day = String(tomorrow.getDate()).padStart(2, '0');
month = String(tomorrow.getMonth() + 1).padStart(2, '0'); // Months are zero-based
year = tomorrow.getFullYear();


const formattedDateTom = `${day}-${month}-${year}`;

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
    let instace;

    beforeAll(async () => {
        const instance = {
            port: 25565
        }
        mongoMemServer = await MongoMemoryServer.create(
            {
                instance
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
                let expected = {
                    status: "ok",
                    data: "user"
                };
                let data =
                    { email: "aman@admin.ca" }
                await axios.get.mockResolvedValue(
                    {
                        data,
                    }
                );
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
//Interface /user/admin/:email/buildings GET
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

//Interface /user/admin/:email/buildings POST
describe("/user/admin/:email/buildings POST request",
    () => {

        let userA = {
            _id: "henrydhc@dumb.ca",
            type: "user",
            adminBuildings: []
        };

        let userB = {
            _id: "nancy@n.cc",
            type: "admin",
            adminBuildings: []
        };

        let userC = {
            _id: "superadmin@super.super",
            type: "superadmin",
            adminBuildings: []
        };

        let testClient;

        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
                await testClient.db("users").collection("users").insertMany([userA, userB, userC]);
            }
        );

        afterAll(
            async () => {
                await testClient.db("users").dropDatabase();
                await jest.restoreAllMocks();
                await testClient.close();
            }
        );


        test("Success: Building Added to admin",
            async () => {
                /*
                Input: 
                    - Admin Email
                    - Superuser token
                    - Unique Building Code
                Expected Status Code: 201 Created
                Expected Behavior: Building added to admin's profile
                Expected Output: Success Message
                */
                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "superadmin@super.super"
                        }
                    }
                );
                let requestData = {
                    token: "fakeToken",
                    building: "SMART"
                };
                let expected = {
                    status: "ok",
                    data: "building added to the admin"
                };
                let actual = await request(app).post("/user/admin/nancy@n.cc/buildings")
                    .set("Content-Type", "application/json").send(requestData);
                expect(actual.status).toBe(201);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Add building to normal user",
            async () => {
                /*
                Input:
                    - User Email
                    - Superuser token
                    - Unique Building Code
                Expected Status Code: 400 Bad Request
                Expected Behavior: None
                Expected Output: Failure Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "superadmin@super.super"
                        }
                    }
                );

                let requestData = {
                    token: "fakeToken",
                    building: "NEVER_AVAILABLE"
                };
                let expected = {
                    status: "error",
                    data: "target account is not admin"
                };
                let actual = await request(app).post("/user/admin/henrydhc@dumb.ca/buildings")
                    .set("Content-Type", "application/json").send(requestData);
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Add building to superadmin",
            async () => {
                /*
                Input:
                    - Superadmin Email
                    - Superadmin token
                    - Unique Building Code
                Expected Status Code: 400 Bad Request
                Expected Behavior: None
                Expected Output: Error Message
                */
                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "superadmin@super.super"
                        }
                    }
                );

                let requestData = {
                    token: "fakeToken",
                    building: "NEVER_AVAILABLE"
                };
                let expected = {
                    status: "error",
                    data: "target account is not admin"
                };
                let actual = await request(app).post("/user/admin/superadmin@super.super/buildings")
                    .set("Content-Type", "application/json").send(requestData);
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Target user does not exist",
            async () => {
                /*
                Input:
                    - Invalid Email
                    - Superadmin token
                    - Unique Building Code
                Expected Status Code: 404
                Expected Behavior: None
                Expected Output: Error Message
                */
                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "superadmin@super.super"
                        }
                    }
                );

                let requestData = {
                    token: "fakeToken",
                    building: "NEVER_AVAILABLE"
                };
                let expected = {
                    status: "error",
                    data: "user not found"
                };
                let actual = await request(app).post("/user/admin/runoob@runoob.com/buildings")
                    .set("Content-Type", "application/json").send(requestData);
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Duplicated Building",
            async () => {
                /*
                Input:
                    - Admin Email
                    - Superadmin token
                    - Duplicated Building Code
                Expected Status Code: 404
                Expected Behavior: None
                Expected Output: Error Message
                */
                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "superadmin@super.super"
                        }
                    }
                );

                let requestData = {
                    token: "fakeToken",
                    building: "SMART"
                };
                let expected = {
                    status: "error",
                    data: "building already exist"
                };
                let actual = await request(app).post("/user/admin/nancy@n.cc/buildings")
                    .set("Content-Type", "application/json").send(requestData);
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);
            }
        );

    }
);


//Interface /user/admin/:email/buildings DELETE
describe("/user/admin/:email/buildings DELETE request",
    () => {

        let userA = {
            _id: "ken@k.k",
            type: "user",
            adminBuildings: []
        };

        let userB = {
            _id: "d@admin.d",
            type: "admin",
            adminBuildings: ["NONE", "JUNK"]
        };

        let userC = {
            _id: "superman@super.s",
            type: "superadmin",
            adminBuildings: []
        }

        let testClient;

        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
                await testClient.db("users").collection("users").insertMany([userA, userB, userC]);
            }
        );

        afterAll(
            async () => {
                await testClient.db("users").dropDatabase();
                await testClient.close();
                await jest.restoreAllMocks();
            }
        );

        test("Success: Delete building from a admin",
            async () => {
                /*
                Input:
                    - admin email
                    - superadmin token
                    - valid building code
                Expected Status Code: 200 OK
                Expected Behavior: Building removed from admin account's profile
                Expected Output: Success Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "superman@super.s"
                        }
                    }
                );

                let requestData = {
                    building: "JUNK",
                    token: "fakeToken"
                };
                let expected = {
                    status: "ok",
                    data: "building removed from the admin"
                };
                let actual = await request(app).delete("/user/admin/d@admin.d/buildings")
                    .set("Content-Type", "application/json").send(requestData);
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Delete building from a superadmin",
            async () => {
                /*
                Input:
                    - superadmin email
                    - superadmin token
                    - valid building code
                Expected Status Code: 400 Bad Request
                Expected Behavior: None
                Expected Output: Error Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "superman@super.s"
                        }
                    }
                );

                let requestData = {
                    building: "NON",
                    token: "fakeToken"
                };
                let expected = {
                    status: "error",
                    data: "target account is not admin"
                };
                let actual = await request(app).delete("/user/admin/ken@k.k/buildings")
                    .set("Content-Type", "application/json").send(requestData);
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Delete building from a normal user",
            async () => {
                /*
                Input:
                    - user email
                    - superadmin token
                    - valid building code
                Expected Status Code: 400 Bad Request
                Expected Behavior: None
                Expected Output: Error Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "superman@super.s"
                        }
                    }
                );

                let requestData = {
                    building: "NON",
                    token: "fakeToken"
                };
                let expected = {
                    status: "error",
                    data: "target account is not admin"
                };
                let actual = await request(app).delete("/user/admin/superman@super.s/buildings")
                    .set("Content-Type", "application/json").send(requestData);
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Delete building from a non-existing user",
            async () => {
                /*
                Input:
                    - invalid user email
                    - superadmin token
                    - valid building code
                Expected Status Code: 404 Not Found
                Expected Behavior: None
                Expected Output: Error Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "superman@super.s"
                        }
                    }
                );

                let requestData = {
                    building: "NON",
                    token: "fakeToken"
                };
                let expected = {
                    status: "error",
                    data: "user not found"
                };
                let actual = await request(app).delete("/user/admin/deadbeef@steak.ca/buildings")
                    .set("Content-Type", "application/json").send(requestData);
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: building does not exist",
            async () => {
                /*
                Input:
                    - admin email
                    - superadmin token
                    - invalid building code
                Expeted Status Code: 404 Not found
                Expected Behavior: None
                Expetced OutputL Error Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "superman@super.s"
                        }
                    }
                );

                let requestData = {
                    building: "JUNK",
                    token: "fakeToken"
                };
                let expected = {
                    status: "error",
                    data: "building not found"
                };
                let actual = await request(app).delete("/user/admin/d@admin.d/buildings")
                    .set("Content-Type", "application/json").send(requestData);
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);

            }
        );


    }
);


//Interface /user/bookings GET
describe("/user/bookings GET Request",
    () => {

        let testClient;

        let userA = {
            _id: "usera@a.c",
            booking_ids: [12345, 67890]
        };

        let userB = {
            _id: "userb@b.a",
            booking_ids: [114514]
        }

        let userC = {
            _id: "userc@dumb.b",
            booking_ids: []
        };

        //Here we prepare booking data

        let bookingA = {
            _id: 12345,
            startIndex: 10,
            endIndex: 12,
        };

        let bookingB = {
            _id: 67890,
            startIndex: 12,
            endIndex: 13,
        };

        let bookingC = {
            _id: 114514,
            startIndex: 14,
            endIndex: 15,
        };

        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
                await testClient.db("users").collection("users").insertMany([userA, userB, userC]);
                await testClient.db("users").collection("bookings").insertMany([bookingA, bookingB, bookingC]);
            }
        );

        afterAll(
            async () => {
                await testClient.db("users").dropDatabase();
                await testClient.close();
                await jest.restoreAllMocks();
            }
        );

        test("Success: User has no booking",
            async () => {
                /*
                Input: Valid user token
                Expected Status Code: 200 OK
                Expected Behavior: Booking ids fetched from the database
                Expected Output: An empty list
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "userc@dumb.b"
                        }
                    }
                );

                let expected = {
                    status: "ok",
                    data: []
                };

                let actual = await request(app).get("/user/bookings?token=dumbToken");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Success: User has one booking",
            async () => {
                /*
                Input: Valid user token
                Expected Status Code: 200 OK
                Expected Behavior: Booking ids fetched from the database
                Expected Output: An list of one booking
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "userb@b.a",
                        }
                    }
                );

                let expected = {
                    status: "ok",
                    data: [{
                        _id: 114514,
                        startTime: "0700",
                        endTime: "0730",
                    }]
                };

                let actual = await request(app).get("/user/bookings?token=fakeToken");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Success: User has more than one booking",
            async () => {
                /*
                Input: Valid user token
                Expected Status Code: 200 OK
                Expected Behavior: Booking data fetched from the database
                Expected Output: An list of two bookings
                */
                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "usera@a.c",
                        }
                    }
                );

                let expected = {
                    status: "ok",
                    data: [{
                        _id: 12345,
                        startTime: "0500",
                        endTime: "0600",
                    }, {
                        _id: 67890,
                        startTime: "0600",
                        endTime: "0630",
                    }]
                };

                let actual = await request(app).get("/user/bookings?token=fakeToken");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);

            }
        );
    }
);

//Interface /

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
                let actual = await request(app).get("/ils/UCEN");
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
                let actual = await request(app).get("/ils/building_all");
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
                let actual = await request(app).get("/ils/DUMB");
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
            730, 730, 730,
            730
        ],
        close_times: [
            2130, 2130,
            2130, 2130,
            2130, 2130,
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
            800, 800, 800
        ],
        close_times: [
            2000, 2000, 2000,
            2000, 2000, 2000,
            2000
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
        800, 800, 800
    ],
    close_times: [
        2000, 2000, 2000,
        2000, 2000, 2000,
        2000
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
        800, 800, 800
    ],
    close_times: [
        2000, 2000, 2000,
        2000, 2000, 2000,
        2000
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
        730, 730, 730,
        730
    ],
    close_times: [
        2130, 2130,
        2130, 2130,
        2130, 2130,
        2130
    ],
    building_code: 'WCKL',
    building_name: 'Walter C. Koerner Library',
    building_address: '1958 Main Mall, Vancouver, BC V6T 1Z2',
    comments: ['Random comment. Trying to fill the 20 character limit.']
}

let testUserBooking = { "_id": "blah@blah.com", "type": "user", "booking_ids": [] }

let testUserWaitlist = { "_id": "wait@wait.com", "type": "user", "booking_ids": [], tokens: ["12345"] }



let testUser = {
    _id: "beef@0xdeadbeef.com",
    type: "user"
};

//Interface /studyrooms/:building_code/:room_no/comments POST
describe("/studyrooms/:building_code/:room_no/comments POST request",
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
                let actual = await request(app).get("/studyrooms/GAME");
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
                let actual = await request(app).get("/studyrooms/GAME");
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
                let actual = await request(app).get("/studyrooms/DUMB");
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
                let actual = await request(app).get("/studyrooms/GAME/202/comments");
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
                let actual = await request(app).get("/studyrooms/GAM/101/comments");
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
                let actual = await request(app).get("/studyrooms/GAME/104/comments");
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );

    }
);

//Interface: bookit.henrydhc.me/filter
describe("/filter GET request",
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
                await testClient.db("study_room_db").dropDatabase();
                await testClient.db("users").dropDatabase();
                await testClient.close();
            }
        );
        test("filter study room with valid data and location close to ALSC", async () => {
            /*
                Input: Valid timeslot with location query near ALSC
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database sorted by distance
                Expected Output: Room information starting from ALSC
            */
            const filterData = {
                startTime: "0800",
                duration: "0.5",
                day: formattedDateTom,
                // lat lon of ALSC
                lat: 49.26607735,
                lon: -123.25137314363899
            }
            let expected = {
                status: "ok",
                // order matters here, testRoomC is in WCKL, which is further away 
                data: [testRoomA, testRoomB, testRoomC]
            }
            let response = await request(app).get("/filter").query(filterData)
            expect(response.status).toBe(200)
            expect(response.body).toEqual(expected)
        });

        test("filter study room with valid data and location close to WCKL", async () => {
            /*
                Input: Valid timeslot with location query near WCKL
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database sorted by distance
                Expected Output: Room information starting from WCKL
            */
            const filterData = {
                startTime: "0800",
                duration: "0.5",
                day: formattedDateTom,
                // lat lon of WCKL
                lat: 49.26654885,
                lon: -123.25507800348765
            }
            let expected = {
                status: "ok",
                // order matters here, testRoomC is in WCKL, which is closer in this case 
                data: [testRoomC, testRoomA, testRoomB]
            }
            let response = await request(app).get("/filter").query(filterData)
            expect(response.status).toBe(200)
            expect(response.body).toEqual(expected)
        });

        test("Remove testRoomA slot by booking it", async () => {
            /*
                Input: Valid timeslot with location query near WCKL
                Expected Status Code: 200
                Expected Behavior: Room information fetched from the database sorted by distance excluding the booked slot
                Expected Output: Room information starting from WCKL
            */
            const filterData = {
                startTime: "0800",
                duration: "0.5",
                day: formattedDateTom,
                // lat lon of WCKL
                lat: 49.26654885,
                lon: -123.25507800348765
            }
            const bookingData = {
                date: formattedDateTom,
                startTime: "0800",
                endTime: "0830",
                buildingCode: "ALSC",
                roomNo: "101",
                token: testUserBooking.email
            }
            let expected = {
                status: "ok",
                // removed roomA
                data: [testRoomC, testRoomB]
            }
            axios.get.mockImplementation(() => Promise.resolve(
                {
                    data: {
                        email: testUserBooking._id
                    }
                }
            ));
            await request(app)
                .post("/studyroom/book")
                .set('Content-Type', 'application/json')
                .send(bookingData);
            let response = await request(app).get("/filter").query(filterData)
            expect(response.status).toBe(200)
            expect(response.body).toEqual(expected)
        });
        test("Invalid date", async () => {
            /*
                Input: Invalid Date
                Expected Status Code: 400
                Expected Behavior: Room information fetched from the database sorted by distance excluding the booked slot
                Expected Output: Room information starting from WCKL
            */
            const filterData = {
                startTime: "0800",
                duration: "0.5",
                day: "08-12-2022",
                // lat lon of WCKL
                lat: 49.26654885,
                lon: -123.25507800348765
            }

            let expected = {
                status: "error",
                // removed roomA
                data: "Invalid Date"
            }
            let response = await request(app).get("/filter").query(filterData)
            expect(response.status).toBe(404)
            expect(response.body).toEqual(expected)
        });
    });

//Interface: bookit.henrydhc.me/studyrooms/:building_code/:room_no/slots
describe("/studyrooms/:building_code/:room_no/slots GET request",
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
                await testClient.db("study_room_db").dropDatabase();
                await testClient.db("users").dropDatabase();
                await testClient.close();
            }
        );
        test("Get slots of room", async () => {
            /*
                Input: Valid query
                Expected Status Code: 200
                Expected Behavior: Slots information fetched from the database
                Expected Output: List of slots with closed hours encoded as 2
            */
            const query = {
                date: "08-12-2023"
            }
            let expected = {
                status: "ok",
                data: "222222222222222200000000000000000000000022222222"
            }
            let response = await request(app).get("/studyrooms/ALSC/101/slots").query(query)
            expect(response.status).toBe(200)
            expect(response.body).toEqual(expected)
        });
        test("Book slot and fetch slots", async () => {
            /*
                Input: Valid query
                Expected Status Code: 200
                Expected Behavior: Slots information fetched from the database
                Expected Output: List of slots with closed hours encoded as 2 and booked slot encoded as 1
            */
            const bookingData = {
                date: "08-12-2023",
                startTime: "0800",
                endTime: "0830",
                buildingCode: "ALSC",
                roomNo: "101",
                token: testUserBooking.email
            }

            axios.get.mockImplementation(() => Promise.resolve(
                {
                    data: {
                        email: testUserBooking._id
                    }
                }
            ));
            await request(app)
                .post("/studyroom/book")
                .set('Content-Type', 'application/json')
                .send(bookingData);
            const query = {
                date: "08-12-2023"
            }
            let expected = {
                status: "ok",
                data: "222222222222222210000000000000000000000022222222"
            }
            let response = await request(app).get("/studyrooms/ALSC/101/slots").query(query)
            expect(response.status).toBe(200)
            expect(response.body).toEqual(expected)
        });
        test("Invalid Room", async () => {
            /*
                Input: Invalid room
                Expected Status Code: 404
                Expected Behavior: No action. Error message is sent
                Expected Output: Room Not Found error message
            */
            const query = {
                date: "08-12-2023"
            }
            let expected = {
                status: "error",
                data: "Room Not Found"
            }
            let response = await request(app).get("/studyrooms/ALSC/108/slots").query(query)
            expect(response.status).toBe(404)
            expect(response.body).toEqual(expected)
        });
        test("Invalid date", async () => {
            /*
                Input: Invalid room
                Expected Status Code: 200
                Expected Behavior: Slots information fetched from the database
                Expected Output: List of slots with closed hours encoded as 2
            */
            const query = {
                date: "08-12-2022"
            }
            let expected = {
                status: "error",
                data: "Invalid Date"
            }
            let response = await request(app).get("/studyrooms/ALSC/101/slots").query(query)
            expect(response.status).toBe(404)
            expect(response.body).toEqual(expected)
        });
    });

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
                Input: Booking details with valid data
                Expected Status Code: 200
                Expected Behavior: Booking details are added to booking and users database collections. 
                Expected Output: Success message
                */

                const bookingData = {
                    date: formattedDateTom,
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
                let actualBooking = await targetUserDb.collection("users").findOne({ _id: testUserBooking._id })
                let actualBookingId = actualBooking.booking_ids[0]
                let actualBookingDetails = await targetUserDb.collection("bookings").findOne({ _id: actualBookingId })
                let roomCode = bookingData.buildingCode + " " + bookingData.roomNo

                expect(actualBookingDetails.date).toEqual(bookingData.date)
                expect(actualBookingDetails.roomCode).toEqual(roomCode)
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Book previously booked slot",
            async () => {
                /*
                Input: Booking details of a previously booked slot
                Expected Status Code: 400
                Expected Behavior: No action. Error message is sent
                Expected Output: Error message
                */

                const bookingData = {
                    date: formattedDateTom,
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
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Empty time slot",
            async () => {
                /*
                Input: Booking details with empty starting slot
                Expected Status Code: 400
                Expected Behavior: No action. Error message is sent
                Expected Output: Error message
                */

                const bookingData = {
                    date: formattedDateTom,
                    startTime: "",
                    endTime: "0830",
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
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Empty building Code Details",
            async () => {
                /*
                Input: Booking details with empty building code
                Expected Status Code: 400
                Expected Behavior: No action. Error message is sent
                Expected Output: Error message
                */
                const bookingData = {
                    date: formattedDateTom,
                    startTime: "0800",
                    endTime: "0830",
                    buildingCode: "",
                    roomNo: "101",
                    token: testUserBooking.email
                }
                let expected = {
                    status: "error",
                    data: "Collection names cannot be empty"
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
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);

            }
        );

        test("Invalid Room Details",
            async () => {
                /*
                Input: Booking details with invalid building code
                Expected Status Code: 400
                Expected Behavior: No action. Error message is sent
                Expected Output: Error message
                */
                const bookingData = {
                    date: formattedDateTom,
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
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);

            }
        );

        test("Invalid date",
            async () => {
                /*
                Input: None
                Expected Status Code: 400
                Expected Behavior: No action. Error message is sent
                Expected Output: Error message
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
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Booking when building is closed",
            async () => {
                /*
                Input: None
                Expected Status Code: 400
                Expected Behavior: No action. Error message is sent
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
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Booking with invalid time",
            async () => {

            }
        );

    }
);

//Interface: bookit.henrydhc.me/user/bookings/:id
describe("/user/bookings/:id DELETE request",
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
                await testClient.db("users").dropDatabase();
                await testClient.db("study_room_db").dropDatabase();
                await testClient.close();
            }
        );

        test("Cancel study room with valid data",
            async () => {
                /*
                Input: Valid booking details for a slot that was previously booked
                Expected Status Code: 200
                Expected Behavior: User's booking is removed from the database
                Expected Output: Success message
                */

                const bookingData = {
                    date: formattedDateTom,
                    startTime: "0800",
                    endTime: "0830",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking.email
                }

                let expected = {
                    status: "ok",
                    data: "Removed"
                };
                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));
                let booking = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);

                let BookingIds = await request(app).get("/user/bookings?token=fakeToken");
                let actual = await request(app).delete("/user/bookings/" + BookingIds.body.data[0]._id + "?token=fakeToken");
                BookingIds = await request(app).get("/user/bookings?token=fakeToken");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
                expect(BookingIds.body.data.length).toBe(0);
            }
        );
        test("Cancel study room with invalid booking id",
            async () => {
                /*
                Input: Invalid Booking ID
                Expected Status Code: 404
                Expected Behavior: No action. Error message is sent
                Expected Output: Error message
                */
                let expected = {
                    status: "error",
                    data: "Booking not Found"
                };

                let actual = await request(app).delete("/user/bookings/1122?token=fakeToken");
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );
        test("Cancel study room with waitlisted room ",
            async () => {
                /*
                Input: Valid booking details for a slot that was previously booked
                Expected Status Code: 200
                Expected Behavior: User's booking is removed from the database. Notification manager is triggered
                Expected Output: Success message
                */

                const bookingData = {
                    date: formattedDateTom,
                    startTime: "0800",
                    endTime: "0830",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking.email
                }

                let expected = {
                    status: "ok",
                    data: "Removed"
                };
                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));
                let booking = await request(app)
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
                await request(app)
                    .post("/studyroom/waitlist")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);

                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));
                let BookingIds = await request(app).get("/user/bookings?token=fakeToken");
                let actual = await request(app).delete("/user/bookings/" + BookingIds.body.data[0]._id + "?token=fakeToken");
                BookingIds = await request(app).get("/user/bookings?token=fakeToken");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
                expect(BookingIds.body.data.length).toBe(0);
            }
        );

    });

//Interface: bookit.henrydhc.me/user/bookings/:id 
describe("/user/bookings/:id PUT request (confirm booking)",
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
                await testClient.db("users").dropDatabase();
                await testClient.db("study_room_db").dropDatabase();
                await testClient.close();
            }
        );

        test("Confirm Booking with valid data",
            async () => {
                /*
                Input: Valid confirm details for a slot that was previously booked
                Expected Status Code: 200
                Expected Behavior: User's booking is confirmed in the database
                Expected Output: Success message
                */

                const bookingData = {
                    date: formattedDateTod,
                    startTime: "1900",
                    endTime: "1930",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking.email
                }

                let expected = {
                    status: "ok",
                    data: "confirmed"
                };
                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));
                let booking = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);

                let BookingIds = await request(app).get("/user/bookings?token=fakeToken");
                let actual = await request(app).put("/user/bookings/" + BookingIds.body.data[0]._id).send({
                    token: "blah",
                    // lat lon of ALSC
                    lat: 49.26607735,
                    lon: -123.25137314363899
                });
                BookingIds = await request(app).get("/user/bookings?token=fakeToken");
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
                expect(BookingIds.body.data[0].confirmed).toBe(true);
            }
        );

        test("Confirm Booking with valid data but far away location",
            async () => {
                /*
                Input: Valid confirm details for a slot that was previously booked but location is far
                Expected Status Code: 400
                Expected Behavior: No action. Error message is sent
                Expected Output: error message
                */

                const bookingData = {
                    date: formattedDateTod,
                    startTime: "1930",
                    endTime: "2000",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking.email
                }

                let expected = {
                    status: "error",
                    data: "Looks like you are far away"
                };
                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));
                let booking = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);

                let BookingIds = await request(app).get("/user/bookings?token=fakeToken");
                let actual = await request(app).put("/user/bookings/" + BookingIds.body.data[1]._id).send({
                    token: "blah",
                    // location of WCKL which is far away
                    lat: 49.26654885,
                    lon: -123.25507800348765
                });
                BookingIds = await request(app).get("/user/bookings?token=fakeToken");
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);
                expect(BookingIds.body.data[1].confirmed).toBe(false);
            }
        );
        test("Confirm Booking with valid data",
            async () => {
                /*
                Input: Valid confirm details but time is not within 10 minutes of booking
                Expected Status Code: 400
                Expected Behavior: No action. Error message is sent
                Expected Output: error message
                */

                const bookingData = {
                    date: formattedDateTom,
                    startTime: "1930",
                    endTime: "2000",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking.email
                }

                let expected = {
                    status: "error",
                    data: "Cannot confirm yet"
                };
                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));
                let booking = await request(app)
                    .post("/studyroom/book")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);

                let BookingIds = await request(app).get("/user/bookings?token=fakeToken");
                let actual = await request(app).put("/user/bookings/" + BookingIds.body.data[2]._id).send({
                    token: "blah",
                    // lat lon of ALSC
                    lat: 49.26607735,
                    lon: -123.25137314363899
                });
                BookingIds = await request(app).get("/user/bookings?token=fakeToken");
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);
                expect(BookingIds.body.data[2].confirmed).toBe(false);
            }
        );

        test("Confirm Booking with invalid id",
            async () => {
                /*
                Input: Invalid booking id
                Expected Status Code: 400
                Expected Behavior: No action. Error message is sent
                Expected Output: error message
                */
                let expected = {
                    status: "error",
                    data: "Booking not Found"
                };
                axios.get.mockImplementation(() => Promise.resolve(
                    {
                        data: {
                            email: testUserBooking._id
                        }
                    }
                ));
                let actual = await request(app).put("/user/bookings/1232314").send({
                    token: "blah",
                    // lat lon of ALSC
                    lat: 49.26607735,
                    lon: -123.25137314363899
                });
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );
    });
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
                await testClient.db("users").dropDatabase();
                await testClient.db("study_room_db").dropDatabase();
                await testClient.close();
            }
        );

        test("Waitlist study room with valid data",
            async () => {
                /*
                Input: Valid booking details for a slot that was previously booked
                Expected Status Code: 200
                Expected Behavior: user's email is added to the waitlist to the slot in the booking collection
                Expected Output: List of a room's comment. The list size is
                */

                const bookingData = {
                    date: formattedDateTom,
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
                await request(app)
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
                let expectedBooking = await targetUserDb.collection("users").findOne({ _id: testUserBooking._id })
                let expectedBookingId = expectedBooking.booking_ids[0]
                let expectedBookingDetails = await targetUserDb.collection("bookings").findOne({ _id: expectedBookingId })
                let waitlist = expectedBookingDetails.waitlist
                expect(waitlist[0]).toEqual(testUserWaitlist._id)
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Waitlist an unbooked slot",
            async () => {
                /*
                Input: Valid booking details for a slot that was not previously booked
                Expected Status Code: 403
                Expected Behavior: No action. Error message is sent
                Expected Output: Error message
                */

                const bookingData = {
                    date: formattedDateTom,
                    startTime: "0830",
                    endTime: "0900",
                    buildingCode: "ALSC",
                    roomNo: "101",
                    token: testUserBooking._id
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
                    .post("/studyroom/waitlist")
                    .set('Content-Type', 'application/json')
                    .send(bookingData);
                expect(actual.status).toBe(403);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Waitlist a slot that is previously booked by same user",
            async () => {
                /*
                Input: bookingData with a slot that is previously booked by the same user
                Expected Status Code: 400
                Expected Behavior: No action. Error message is sent
                Expected Output: Error message
                */
                const bookingData = {
                    date: formattedDateTom,
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
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);

            }
        );

        test("Invalid date",
            async () => {
                /*
                Input: Date that is not in the future
                Expected Status Code: 400
                Expected Behavior: No action. Error message is sent
                Expected Output: Error message
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
                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Booking when building is closed",
            async () => {
                /*
                Input: Booking data with timeslot when building is closed
                Expected Status Code: 400
                Expected Behavior: No action. Error message is sent
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
                expect(actual.status).toBe(400);
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
                await jest.restoreAllMocks();
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
                let actual = await request(app).get("/lecturehalls/UCEN");
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
                let actual = await request(app).get("/lecturehalls/building_all");
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
                let actual = await request(app).get("/lecturehalls/DUMB");
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );
    }
);

// Interface bookit.henrydhc.me/studyrooms/building POST request
describe("/studyrooms/building POST",
    () => {

        let testClient;

        let userA = {
            _id: "god@god.gg",
            type: "superadmin"
        };

        let userB = {
            _id: "prayer@prayer.gg",
            type: "user"
        };

        let userC = {
            _id: "priest@priest.gg",
            type: "admin"
        };

        let buildingA = {
            token: "dumb",
            building_code: "POPY",
            building_name: "PopeYes",
            buuilding_address: "Unknown",
            open_times: "None",
            close_times: "None"
        };

        let buildingB = {
            token: "null",
            building_code: "KFC",
            building_name: "Kenturky Fried Chicken",
            buuilding_address: "Unknown",
            open_times: "None",
            close_times: "None"
        }


        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
                await testClient.db("users").collection("users").insertMany([userA, userB, userC]);
                await testClient.db("study_room_db").dropDatabase();
            }
        );

        afterAll(
            async () => {
                await jest.restoreAllMocks();
                await testClient.close();
            }
        );

        test("Failure: Server Error",
            async () => {
                /*
                Input: building data and superuser token
                Expected Status Code: 403
                Expected Behavior: None
                Expected Output: Error Message
                */
                await axios.get.mockImplementation(
                    (a, b) => {
                        if (b == null) {
                            return Promise.resolve(
                                {
                                    data: {
                                        email: "god@god.gg"
                                    }
                                }
                            );
                        } else {
                            return Promise.resolve(
                                {
                                    data: {
                                        results: [
                                            {
                                                geometry: {
                                                    lat: 0,
                                                    lng: 0
                                                }
                                            }
                                        ]
                                    }
                                }
                            );
                        }
                    }
                );

                let expected = {
                    status: "error",
                    data: "Server error, please retry"
                };

                let actual = await request(app).post("/studyrooms/building")
                    .set("Content-Type", "application/json").send(buildingA);
                expect(actual.status).toBe(403);
                expect(actual.body).toEqual(expected);
                await testClient.db("study_room_db").collection("building_all").insertOne(
                    {
                        type: "all_studyroom_buildings",
                        buildings: []
                    }
                );
            }
        );

        test("Success: building created by superadmin",
            async () => {
                /*
                Input: Valid building data and superuser token
                Expected Status Code: 200
                Expected Behavior: Building data added to the database
                Expected Output: Success Message
                */
                await axios.get.mockImplementation(
                    (a, b) => {
                        if (b == null) {
                            return Promise.resolve(
                                {
                                    data: {
                                        email: "god@god.gg"
                                    }
                                }
                            );
                        } else {
                            return Promise.resolve(
                                {
                                    data: {
                                        results: [
                                            {
                                                geometry: {
                                                    lat: 0,
                                                    lng: 0
                                                }
                                            }
                                        ]
                                    }
                                }
                            );
                        }
                    }
                );

                let expected = {
                    status: "ok",
                    data: "Successfully added"
                };

                let actual = await request(app).post("/studyrooms/building")
                    .set("Content-Type", "application/json").send(buildingA);
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Admin User",
            async () => {
                /*
                Input: Valid building data and admin token
                Expected Status Code: 401
                Expected Behavior: None
                Expected Output: Error Message
                */
                await axios.get.mockImplementation(
                    (a, b) => {
                        if (b == null) {
                            return Promise.resolve(
                                {
                                    data: {
                                        email: "priest@priest.gg"
                                    }
                                }
                            );
                        } else {
                            return Promise.resolve(
                                {
                                    data: {
                                        results: [
                                            {
                                                geometry: {
                                                    lat: 0,
                                                    lng: 0
                                                }
                                            }
                                        ]
                                    }
                                }
                            );
                        }
                    }
                );

                let expected = {
                    status: "error",
                    data: "Unauthorized"
                };

                let actual = await request(app).post("/studyrooms/building")
                    .set("Content-Type", "application/json").send(buildingB);
                expect(actual.status).toBe(401);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Normal User",
            async () => {
                await axios.get.mockImplementation(
                    (a, b) => {
                        if (b == null) {
                            return Promise.resolve(
                                {
                                    data: {
                                        email: "prayer@prayer.gg"
                                    }
                                }
                            );
                        } else {
                            return Promise.resolve(
                                {
                                    data: {
                                        results: [
                                            {
                                                geometry: {
                                                    lat: 0,
                                                    lng: 0
                                                }
                                            }
                                        ]
                                    }
                                }
                            );
                        }
                    }
                );

                let expected = {
                    status: "error",
                    data: "Unauthorized"
                };

                let actual = await request(app).post("/studyrooms/building")
                    .set("Content-Type", "application/json").send(buildingB);
                expect(actual.status).toBe(401);
                expect(actual.body).toEqual(expected);
            }
        );


    }
);

//Interface bookit.henrydhc.me/studyrooms/:building_code DELETE request
describe("/studyrooms/:building_code DELETE",
    () => {

        let testClient;

        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
            }
        );

        afterAll(
            async () => {
                await testClient.db("users").dropDatabase();
                await testClient.db("study_room_db").dropDatabase();
                await testClient.close();
                await jest.restoreAllMocks();
            }
        );

        test("Failrue: Non-superadmin user",
            async () => {
                /*
                Input: valid building code and user token
                Expected Status Code: 401
                Expected Behavior: None
                Expected Output: Error Message
                */
                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "prayer@prayer.gg"
                        }
                    }
                );

                let expected = {
                    status: "error",
                    data: "Unauthorized"
                };

                let requestData = {
                    token: "fakeToken"
                };

                let actual = await request(app).delete("/studyrooms/POPY")
                    .set("Content-Type", "application/json").send(requestData);
                expect(actual.status).toBe(401);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Success: Building deleted",
            async () => {
                /*
                Input: valid building code and superadmin token
                Expected Status Code: 200
                Expected Behavior: Building removed from the database
                Expected Output: Success Message
                */
                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "god@god.gg"
                        }
                    }
                );

                let expected = {
                    status: "ok",
                    data: "Successfully removed"
                };

                let requestData = {
                    token: "fakeToken"
                };

                let actual = await request(app).delete("/studyrooms/POPY")
                    .set("Content-Type", "application/json").send(requestData);
                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Building does not exist",
            async () => {
                /*
                Input: invalid building code and superadmin token
                Expected Status Code: 404
                Expected Behavior: None
                Expected Output: Failure Message
                */
                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "god@god.gg"
                        }
                    }
                );

                let expected = {
                    status: "error",
                    data: "No building found"
                };

                let requestData = {
                    token: "fakeToken"
                };

                let actual = await request(app).delete("/studyrooms/POPY")
                    .set("Content-Type", "application/json").send(requestData);
                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);
            }
        );


    }
);

//Interface bookit.henrydhc.me/studyrooms/:building_code/:roomcode DELETE
describe("/studyrooms/:building_code/:roomcode DELETE",
    () => {

        let testClient;

        let userA = {
            _id: "admin@a.com",
            type: "admin",
            adminBuildings: ["NOOB"]
        };

        let userB = {
            _id: "user@b.com",
            type: "user",
            adminBuildings: []
        };

        let room = {
            _id: "100"
        }

        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
                await testClient.db("users").collection("users").insertMany([userA, userB]);
                await testClient.db("study_room_db").collection("NOOB").insertOne(room);
            }
        );

        afterAll(
            async () => {
                await testClient.db("users").dropDatabase();
                await testClient.db("study_room_db").dropDatabase();
                await testClient.close();
                await jest.restoreAllMocks();
            }
        );

        test("Failure: Non-admin user",
            async () => {
                /*
                Input: valid room data and user token
                Expected Status Code: 401 Unauthorized
                Expected Behavior: None
                Expected Output: Error Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "user@b.com"
                        }
                    }
                );

                let expected = {
                    status: "error",
                    data: "Unauthorized"
                };

                let requestData = {
                    token: "fakeToken"
                };

                let actual = await request(app).delete("/studyrooms/NOOB/100")
                    .set("Content-Type", "application/json").send(requestData);

                expect(actual.status).toBe(401);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Success: Room removed",
            async () => {
                /*
                Input: valid room data and admin token
                Expected Status Code: 200
                Expected Behavior: Room deleted from the database
                Expected Output: Success Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "admin@a.com"
                        }
                    }
                );

                let expected = {
                    status: "ok",
                    data: "Successfully removed"
                };

                let requestData = {
                    token: "fakeToken"
                };

                let actual = await request(app).delete("/studyrooms/NOOB/100")
                    .set("Content-Type", "application/json").send(requestData);

                expect(actual.status).toBe(200);
                expect(actual.body).toEqual(expected);
            }
        );

        test("Failure: Room does not exist",
            async () => {
                /*
                Input: invalid room data and admin token
                Expected Status Code: 400
                Expected Behavior: None
                Expected Output: Error Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "admin@a.com"
                        }
                    }
                );

                let expected = {
                    status: "error",
                    data: "Room number does not exist"
                };

                let requestData = {
                    token: "fakeToken"
                };

                let actual = await request(app).delete("/studyrooms/NOOB/100")
                    .set("Content-Type", "application/json").send(requestData);

                expect(actual.status).toBe(400);
                expect(actual.body).toEqual(expected);
            }
        );
    }
);

//Interface bookit.henrydhc.me/studyrooms/:building_code/:room_no/report PUT
describe("/studyrooms/:building_code/:room_no POST",
    () => {

        let testClient;

        let user = {
            _id: "user@user.ca"
        };

        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
                await testClient.db("users").collection("users").insertOne(user);
            }
        );

        afterAll(
            async () => {
                await testClient.db("users").dropDatabase();
                await testClient.db("study_room_db").dropDatabase();
                await testClient.close();
                await jest.restoreAllMocks();
            }
        );

        test("Success: submitted",
            async () => {
                /*
                Input: valid user token and valid report data
                Expected Status Code: 201
                Expected Behavior: Report added to the database
                Expected Output: Success Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "user@user.ca"
                        }
                    }
                );

                let data = {
                    msg: "This is a dumb room"
                };

                let expected = {
                    status: "ok",
                    data: "report submitted"
                };

                let actual = await request(app).post("/studyrooms/NOOB/100/report")
                    .set("Content-Type", "application/json").send(data);

                expect(actual.status).toBe(201);
                expect(actual.body).toEqual(expected);

            }
        );

        test("Failure: invalid params",
            async () => {
                /*
                Input: valid user token and invalid report data
                Expected status: 404
                Expected Behavior: None
                Expected Output: Error Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "user@user.ca"
                        }
                    }
                );

                let data = {
                    msg: "This is another dumb room"
                };


                let actual = await request(app).post("/studyrooms/100//report")
                    .set("Content-Type", "application/json").send(data);

                expect(actual.status).toBe(404);
            }
        );
    }
);

//Interface bookit.henrydhc.me/studyrooms/:building_code/:room_no/report POST
describe("/studyrooms/:building_code/:room_no/report POST",
    () => {

        let testClient;

        let user = {
            _id: "user@u.c",
            type: "user"
        };

        let room = {
            _id: "100",
            comments: []
        };

        beforeAll(
            async () => {
                testClient = await MongoClient.connect(mongoMemServer.getUri());
                await testClient.db("users").collection("users").insertOne(user);
            }
        );

        afterAll(
            async () => {
                await testClient.db("users").dropDatabase();
                await testClient.db("study_room_db").dropDatabase();
                await testClient.close();
                await mongoMemServer.stop();
                await server.shutDown()
            }
        );

        test("Failure: Room does not exist",
            async () => {
                /*
                Input: valid user token and invalid room data and comment
                Expected Status Code: 404
                Expected Behavior: None
                Expected Output: Error Message
                */

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "user@u.c"
                        }
                    }
                );

                let requestData = {
                    token: "fakeToken",
                    comment: "Yes!"
                };

                let expected = {
                    status: "error",
                    data: "Not found"
                };

                let actual = await request(app).post("/studyrooms/DUMB/100/comments")
                    .set("Content-Type", "application/json").send(requestData);

                expect(actual.status).toBe(404);
                expect(actual.body).toEqual(expected);

            }
        )

        test("Success: valid report",
            async () => {
                /*
                Input: valid user token and room data and comment
                Expected Status Code: 201
                Expected Behavior: Comment added to the room data
                Expected Output: Success Message
                */

                //Prepare data
                await testClient.db("study_room_db").collection("DUMB").insertOne(room);

                await axios.get.mockResolvedValue(
                    {
                        data: {
                            email: "user@u.c"
                        }
                    }
                );

                let requestData = {
                    token: "fakeToken",
                    comment: "Yes!"
                };

                let expected = {
                    status: "ok",
                    data: "comment posted"
                };

                let actual = await request(app).post("/studyrooms/DUMB/100/comments")
                    .set("Content-Type", "application/json").send(requestData);

                expect(actual.status).toBe(201);
                expect(actual.body).toEqual(expected);

            }
        );


    }
);