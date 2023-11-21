
/** @type {import('jest').Config} */
const config = {
    verbose: true, 
    testEnvironment: "node", 
    preset: "@shelf/jest-mongodb", 
    collectCoverage: true, 
    coverageDirectory: "./coverage", 
    globals: {
        MONGO_MEMEORY_SERVER_INSTANCE: null, 
        SERVER_INSTANCE: null
    }, 
    globalSetup: "./setup.js",
    globalTeardown: "./teardown.js", 
    moduleDirectories: [
        "/home/dev/bookit_backend/node_modules", 
        "/home/dev/bookit_backend/modules"
    ]
}

module.exports = config;