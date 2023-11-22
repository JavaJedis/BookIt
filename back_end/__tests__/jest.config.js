
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
        "./node_modules",
        "./modules"
    ]
}

module.exports = config;