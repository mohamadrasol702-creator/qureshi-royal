const hits = new Map(), fails = new Map();
const WINDOW = 60000, MAX = 60, LOCK = 10 * 60000, MAX_FAILS = 10;
function rateLimit(req, res, next) {
  const now = Date.now(), ip = req.ip;
  const f = fails.get(ip);
  if (f && f.count >= MAX_FAILS && now - f.last < LOCK) return res.status(429).json({ error: 'locked' });
  const h = (hits.get(ip) || []).filter(t => now - t < WINDOW);
  h.push(now); hits.set(ip, h);
  if (h.length > MAX) return res.status(429).json({ error: 'too many requests' });
  next();
}
function authFailed(ip) { const f = fails.get(ip) || { count: 0 }; f.count++; f.last = Date.now(); fails.set(ip, f); }
function authOk(ip) { fails.delete(ip); }
setInterval(() => { const now = Date.now(); for (const [k, v] of hits) if (!v.some(t => now - t < WINDOW)) hits.delete(k); }, 300000);
module.exports = { rateLimit, authFailed, authOk };
