const router = require('express').Router();
const log = require('../core/logger');
router.get('/logs', (req, res) => res.json({ lines: log.tail(50) }));
router.delete('/logs', (req, res) => { log.clear(); res.json({ ok: true }); });
module.exports = router;
