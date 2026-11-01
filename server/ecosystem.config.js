module.exports = { apps: [{ name: 'royal', script: 'core/server.js',
  node_args: '--env-file=.env', max_memory_restart: '400M', autorestart: true }] };
