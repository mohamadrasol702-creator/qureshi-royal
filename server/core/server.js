const express = require('express');
const cfg = require('../config');
const log = require('./logger');
require('../process/crash-recovery').install();
const firewall = require('../security/firewall');
const { requireAuth } = require('../api/auth');
const manager = require('./server-manager');

const app = express();
app.disable('x-powered-by');
app.use(firewall.rateLimit);
app.use(express.json({ limit: '20kb' }));
app.use('/health', require('./health-check'));
app.use('/api', requireAuth,
  require('../api/command').router,
  require('../api/bot-control'),
  require('../api/settings'),
  require('../api/logs'));
app.use((err, req, res, next) => res.status(400).json({ error: 'bad request' }));

const server = app.listen(cfg.port, cfg.host, () => {
  log.info(`Qureshi Royal server listening on ${cfg.host}:${cfg.port}`);
  manager.init(server);
});
