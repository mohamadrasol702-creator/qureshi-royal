const router = require('express').Router();
const conn = require('../whatsapp/connection');
router.get('/', (req, res) => res.json({ ok: true, uptime: Math.round(process.uptime()), whatsapp: conn.state().status }));
module.exports = router;
