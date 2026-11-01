const log = require('./logger');
const db = require('../database/database-manager');
const backup = require('../database/backup-manager');
const sessions = require('../whatsapp/session-manager');
const conn = require('../whatsapp/connection');
const autoRestart = require('../process/auto-restart');

function init(httpServer) {
  if (db.get('settings').autostart && sessions.isLinked()) {
    conn.start().catch(e => log.error('autostart failed: ' + e.message));
  }
  autoRestart.watch();
  setInterval(() => { try { backup.create(); } catch (e) { log.error('backup: ' + e.message); } }, 24 * 3600 * 1000);
  const shutdown = async () => {
    log.info('Shutting down...');
    await conn.stop(false).catch(() => {});
    httpServer.close(() => process.exit(0));
    setTimeout(() => process.exit(0), 3000);
  };
  process.on('SIGTERM', shutdown);
  process.on('SIGINT', shutdown);
}
module.exports = { init };
