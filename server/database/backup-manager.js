const fs = require('fs'), path = require('path');
const db = require('./database-manager');
const { encrypt } = require('./encryption');
const DIR = path.join(__dirname, '..', 'backups');
fs.mkdirSync(DIR, { recursive: true });
function create() {
  const bundle = {};
  for (const f of fs.readdirSync(db.DIR)) if (f.endsWith('.json')) bundle[f] = fs.readFileSync(path.join(db.DIR, f), 'utf8');
  const name = `backup-${new Date().toISOString().replace(/[:.]/g, '-')}.bak`;
  fs.writeFileSync(path.join(DIR, name), encrypt(JSON.stringify(bundle)));
  const all = list();
  for (const old of all.slice(0, Math.max(0, all.length - 14))) fs.unlinkSync(path.join(DIR, old));
  return name;
}
function list() { return fs.readdirSync(DIR).filter(f => f.endsWith('.bak')).sort(); }
module.exports = { create, list };
