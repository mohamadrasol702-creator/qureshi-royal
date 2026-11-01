const log = require('../core/logger');
let crashes = [];
function crash(kind, e) {
  log.error(`${kind}: ${(e && e.stack) || e}`);
  const now = Date.now();
  crashes = crashes.filter(t => now - t < 60000); crashes.push(now);
  if (crashes.length >= 10) { log.error('Too many errors, exiting so pm2 restarts us'); process.exit(1); }
}
function install() {
  process.on('uncaughtException', e => crash('uncaughtException', e));
  process.on('unhandledRejection', e => crash('unhandledRejection', e));
}
module.exports = { install };
