const router = require('express').Router();
const os = require('os');
const db = require('../database/database-manager');
const backup = require('../database/backup-manager');
const sessions = require('../whatsapp/session-manager');
const conn = require('../whatsapp/connection');
const log = require('../core/logger');
const audit = require('../security/audit');

const HELP = [
  'help | system info',
  'bot start|stop|restart|status',
  'whatsapp pairing <number> | status | disconnect',
  'group list | open <n> | close <n> | members <n>',
  'security antilink|antispam|antiflood on|off',
  'settings show | reset',
  'settings set botname|prefix|welcome|goodbye <text>',
  'commands list | create <name> <reply> | delete <name>',
  'files list | files backup',
  'logs | logs clear'
].join('\n');

const fmt = s => `${Math.floor(s / 3600)}h ${Math.floor(s % 3600 / 60)}m`;

async function run(line) {
  const p = line.trim().split(/\s+/);
  const c = (p[0] || '').toLowerCase(), sub = (p[1] || '').toLowerCase(), rest = p.slice(2);
  switch (c) {
    case '': return '';
    case 'help': return HELP;
    case 'system':
      if (sub === 'version') return 'Qureshi Royal Server v0.2';
      return `Qureshi Royal Server v0.2\nNode ${process.version}\nUptime ${fmt(process.uptime())}\nRAM ${Math.round(process.memoryUsage().rss / 1048576)} MB\nHost ${os.hostname()}`;
    case 'bot': {
      const st = conn.state();
      if (sub === 'start') {
        if (!sessions.isLinked()) return 'Not linked yet. Use: whatsapp pairing <number>';
        await conn.start(); return 'Bot starting...';
      }
      if (sub === 'stop') { await conn.stop(true); return 'Bot stopped.'; }
      if (sub === 'restart') {
        if (!sessions.isLinked()) return 'Not linked yet. Use: whatsapp pairing <number>';
        await conn.stop(false); await conn.start(); return 'Bot restarting...';
      }
      if (sub === 'status') return `Bot: ${st.enabled ? 'ON' : 'OFF'} | WhatsApp: ${st.status}`;
      return 'Usage: bot start|stop|restart|status';
    }
    case 'whatsapp': {
      if (sub === 'pairing') {
        if (!rest[0]) return 'Usage: whatsapp pairing 93700000000 (country code, digits only)';
        const code = await conn.requestPairing(rest[0]);
        return `Code: ${code.slice(0, 4)}-${code.slice(4)}\nWhatsApp > Linked devices > Link with phone number > enter the code.`;
      }
      if (sub === 'connect') { if (!sessions.isLinked()) return 'Not linked yet. Use: whatsapp pairing <number>'; await conn.start(); return 'Connecting...'; }
      if (sub === 'status') return `WhatsApp: ${conn.state().status} | linked: ${sessions.isLinked()}`;
      if (sub === 'disconnect') { await conn.logout(); return 'Logged out and session removed.'; }
      return 'Usage: whatsapp pairing|connect|status|disconnect';
    }
    case 'group': {
      if (sub === 'list') {
        const g = await conn.groups();
        return g.length ? g.map((x, i) => `${i + 1}. ${x.name} (${x.size})`).join('\n') : 'No groups.';
      }
      const n = parseInt(rest[0], 10);
      if (!n) return 'Usage: group open|close|members <number from group list>';
      if (sub === 'open') return await conn.setAnnounce(n, false);
      if (sub === 'close') return await conn.setAnnounce(n, true);
      if (sub === 'members') return await conn.members(n);
      return 'Usage: group list|open|close|members';
    }
    case 'security': {
      const key = sub, val = (rest[0] || '').toLowerCase();
      if (!['antilink', 'antispam', 'antiflood'].includes(key) || !['on', 'off'].includes(val))
        return 'Usage: security antilink|antispam|antiflood on|off';
      db.update('settings', s => { s.protection[key] = val === 'on'; });
      return `${key}: ${val}`;
    }
    case 'settings': {
      if (sub === 'show') return JSON.stringify(db.get('settings'), null, 1);
      if (sub === 'reset') { db.set('settings', {}); return 'Settings reset.'; }
      if (sub === 'set') {
        const key = (rest[0] || '').toLowerCase(), val = rest.slice(1).join(' ');
        const map = { botname: 'botName', prefix: 'prefix', welcome: 'welcome', goodbye: 'goodbye' };
        if (!map[key] || !val) return 'Usage: settings set botname|prefix|welcome|goodbye <text>';
        if (key === 'prefix' && val.length > 2) return 'Prefix must be 1-2 characters.';
        db.update('settings', s => { s[map[key]] = val.slice(0, 300); });
        return 'Saved.';
      }
      return 'Usage: settings show|set|reset';
    }
    case 'commands': {
      const cmds = db.get('commands');
      if (sub === 'list') { const k = Object.keys(cmds); return k.length ? k.join('\n') : 'No custom commands.'; }
      if (sub === 'create' || sub === 'edit') {
        const name = (rest[0] || '').toLowerCase().replace(/[^a-z0-9_]/g, ''), reply = rest.slice(1).join(' ');
        if (!name || !reply) return 'Usage: commands create <name> <reply text>';
        cmds[name] = reply.slice(0, 1000); db.set('commands', cmds); return `Saved: ${name}`;
      }
      if (sub === 'delete') { delete cmds[(rest[0] || '').toLowerCase()]; db.set('commands', cmds); return 'Deleted.'; }
      return 'Usage: commands list|create|delete';
    }
    case 'files':
      if (sub === 'backup') return 'Backup created: ' + backup.create();
      if (sub === 'list') { const l = backup.list(); return l.length ? l.join('\n') : 'No backups.'; }
      return 'Usage: files list|backup';
    case 'logs':
      if (sub === 'clear') { log.clear(); return 'Logs cleared.'; }
      return log.tail(25).join('\n') || 'No logs.';
    default: return 'Unknown command. Type: help';
  }
}

router.post('/command', async (req, res) => {
  const line = String((req.body && req.body.line) || '').slice(0, 500);
  audit.record(req.ip, 'cmd: ' + line.split(/\s+/).slice(0, 2).join(' '));
  try { res.json({ output: await run(line) }); }
  catch (e) { res.json({ output: 'Error: ' + e.message }); }
});
module.exports = { router, run };
