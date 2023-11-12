const mysql = require('mysql');
const config = require('./config');

function getDBPool() {
    return mysql.createPool({
        connectionLimit : 10, // important
        host: config.mysql.host,
        port: config.mysql.port,
        database: config.mysql.database,
        user: config.mysql.user,
        password: config.mysql.password,
        charset: config.mysql.charset,
        connectTimeout: 10 * 60 * 1000,
        acquireTimeout: 10 * 60 * 1000,
        timeout: 10 * 60 * 1000
    });
}

function getDBConnection() {
    return mysql.createConnection({
        host: config.mysql.host,
        port: config.mysql.port,
        database: config.mysql.database,
        user: config.mysql.user,
        password: config.mysql.password,
        charset: config.mysql.charset,
        connectTimeout: 10 * 60 * 1000
    });
}

module.exports = {
    getDBPool,
    getDBConnection
}
