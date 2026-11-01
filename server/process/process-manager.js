// Process supervision is done by pm2 (see ecosystem.config.js).
module.exports = {
  info: () => ({ pid: process.pid, uptime: Math.round(process.uptime()), rssMB: Math.round(process.memoryUsage().rss / 1048576) })
};
