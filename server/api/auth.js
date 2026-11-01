const ac = require('../security/access-control');
const fw = require('../security/firewall');
const audit = require('../security/audit');
function requireAuth(req, res, next) {
  const h = req.headers.authorization || '';
  const token = h.startsWith('Bearer ') ? h.slice(7) : '';
  if (!ac.verifyToken(token)) { fw.authFailed(req.ip); audit.record(req.ip, 'AUTH FAILED'); return res.status(401).json({ error: 'unauthorized' }); }
  fw.authOk(req.ip);
  next();
}
module.exports = { requireAuth };
