const fs = require('fs'), path = require('path');
const file = path.join(__dirname, '..', 'logs', 'audit.log');
function record(ip, action) { fs.appendFile(file, `${new Date().toISOString()} ${ip} ${action}\n`, () => {}); }
module.exports = { record };
