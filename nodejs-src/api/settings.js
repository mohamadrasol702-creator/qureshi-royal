const router = require('express').Router();
const db = require('../database/database-manager');
router.get('/settings', (req, res) => res.json(db.get('settings')));
router.put('/settings', (req, res) => {
  const b = req.body || {};
  db.update('settings', s => {
    for (const k of ['botName', 'prefix', 'welcome', 'goodbye']) if (typeof b[k] === 'string') s[k] = b[k].slice(0, 300);
    if (b.protection) for (const k of ['antilink', 'antispam', 'antiflood']) if (typeof b.protection[k] === 'boolean') s.protection[k] = b.protection[k];
  });
  res.json(db.get('settings'));
});
module.exports = router;
