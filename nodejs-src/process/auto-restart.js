const log = require('../core/logger');
const conn = require('../whatsapp/connection');
let downSince = null;
function watch() {
  setInterval(() => {
    const s = conn.state();
    if (s.enabled && s.status !== 'connected' && s.status !== 'connecting') {
      downSince = downSince || Date.now();
      if (Date.now() - downSince > 5 * 60000) {
        log.warn('Connection down for 5 minutes, forcing restart');
        downSince = null;
        conn.stop(false).then(() => conn.start()).catch(e => log.error(e.message));
      }
    } else downSince = null;
  }, 30000);
}
module.exports = { watch };
