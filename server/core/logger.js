const fs = require('fs'), path = require('path');
const dir = path.join(__dirname, '..', 'logs');
fs.mkdirSync(dir, { recursive: true });
const file = path.join(dir, 'server.log');
const ring = [];
function log(level, msg) {
  const line = `${new Date().toISOString()} [${level}] ${msg}`;
  console.log(line);
  ring.push(line);
  if (ring.length > 500) ring.shift();
  fs.appendFile(file, line + '\n', () => {});
}
module.exports = {
  info: m => log('INFO', m), warn: m => log('WARN', m), error: m => log('ERROR', m),
  tail: (n = 30) => ring.slice(-n),
  clear: () => { ring.length = 0; fs.writeFile(file, '', () => {}); }
};
