const router = require('express').Router();
const conn = require('../whatsapp/connection');
const sessions = require('../whatsapp/session-manager');
router.get('/bot/status', (req, res) => res.json({ ...conn.state(), linked: sessions.isLinked() }));
router.post('/bot/:action', async (req, res) => {
  try {
    const a = req.params.action;
    if (a === 'start') await conn.start();
    else if (a === 'stop') await conn.stop(true);
    else if (a === 'restart') { await conn.stop(false); await conn.start(); }
    else return res.status(404).json({ error: 'unknown action' });
    res.json({ ok: true });
  } catch (e) { res.status(500).json({ error: e.message }); }
});
module.exports = router;
