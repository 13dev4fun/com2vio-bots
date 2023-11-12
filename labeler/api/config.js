const yaml = require('node-yaml-config');

const config = yaml.load(__dirname + '/config.yml');

module.exports = config;