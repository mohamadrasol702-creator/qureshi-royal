const crypto = require('crypto');
const cfg = require('../config');
const sha = s => crypto.createHash('sha256').update(String(s)).digest();
function verifyToken(t) { return crypto.timingSafeEqual(sha(t || ''), sha(cfg.token)); }
module.exports = { verifyToken };
