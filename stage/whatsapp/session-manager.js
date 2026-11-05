const fs = require('fs'), path = require('path');
const DIR = path.join(__dirname, '..', 'data', 'session');
function dir() { fs.mkdirSync(DIR, { recursive: true }); return DIR; }
function isLinked() {
  try { return JSON.parse(fs.readFileSync(path.join(DIR, 'creds.json'), 'utf8')).registered === true; } catch { return false; }
}
function clear() { fs.rmSync(DIR, { recursive: true, force: true }); }
module.exports = { dir, isLinked, clear };
