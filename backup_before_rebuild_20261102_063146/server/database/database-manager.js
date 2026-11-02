const fs = require('fs'), path = require('path');
const DIR = path.join(__dirname, '..', 'data');
fs.mkdirSync(DIR, { recursive: true });
const DEF_SETTINGS = {
  botName: 'Qureshi Royal', prefix: '!', welcome: 'Welcome @user to @group 👑', goodbye: 'Goodbye @user',
  protection: { antilink: false, antispam: false, antiflood: false }, autostart: true
};
function get(name) {
  let v;
  try { v = JSON.parse(fs.readFileSync(path.join(DIR, name + '.json'), 'utf8')); } catch { v = {}; }
  if (name === 'settings') return { ...DEF_SETTINGS, ...v, protection: { ...DEF_SETTINGS.protection, ...(v.protection || {}) } };
  return v;
}
function set(name, val) {
  const f = path.join(DIR, name + '.json');
  fs.writeFileSync(f + '.tmp', JSON.stringify(val, null, 2));
  fs.renameSync(f + '.tmp', f);
}
function update(name, fn) { const v = get(name); fn(v); set(name, v); return v; }
module.exports = { get, set, update, DIR };
