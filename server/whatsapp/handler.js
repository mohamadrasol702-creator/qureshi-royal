const db = require('../database/database-manager');
const log = require('../core/logger');

const LINK = /(https?:\/\/|chat\.whatsapp\.com\/|www\.)\S+/i;
const floodMap = new Map(), lastMsg = new Map(), warns = new Map();

const textOf = m => { const x = m.message || {};
  return x.conversation || (x.extendedTextMessage && x.extendedTextMessage.text) ||
    (x.imageMessage && x.imageMessage.caption) || (x.videoMessage && x.videoMessage.caption) || ''; };
const idOf = p => typeof p === 'string' ? p : p.id;
const num = jid => String(jid).split('@')[0];

async function isAdmin(sock, jid, user) {
  const md = await sock.groupMetadata(jid);
  const p = md.participants.find(x => x.id === user);
  return !!(p && p.admin);
}

function violation(s, jid, user, text) {
  const now = Date.now(), key = jid + user;
  if (s.protection.antilink && LINK.test(text)) return 'link';
  if (s.protection.antiflood) {
    const t = (floodMap.get(key) || []).filter(x => now - x < 5000); t.push(now); floodMap.set(key, t);
    if (t.length > 6) return 'flood';
  }
  if (s.protection.antispam && text) {
    const l = lastMsg.get(key) || { text: '', n: 0 };
    l.n = l.text === text ? l.n + 1 : 1; l.text = text; lastMsg.set(key, l);
    if (l.n >= 3) return 'spam';
  }
  return null;
}

async function onMessage(sock, m) {
  if (!m.message || m.key.fromMe) return;
  const jid = m.key.remoteJid, isGroup = jid.endsWith('@g.us');
  const user = m.key.participant || jid, text = textOf(m).trim();
  const s = db.get('settings');

  if (isGroup) {
    if (require('./commands').isMuted(jid, user) && !(await isAdmin(sock, jid, user))) { await sock.sendMessage(jid, { delete: m.key }).catch(() => {}); return; }
    const why = violation(s, jid, user, text);
    if (why && !(await isAdmin(sock, jid, user))) {
      await sock.sendMessage(jid, { delete: m.key }).catch(() => {});
      const k = jid + user, n = (warns.get(k) || 0) + 1; warns.set(k, n);
      await sock.sendMessage(jid, { text: `⚠️ @${num(user)} warning ${n}/3 (${why})`, mentions: [user] });
      if (n >= 3) { warns.delete(k); await sock.groupParticipantsUpdate(jid, [user], 'remove').catch(() => {}); }
      return;
    }
  }

  if (!text.startsWith(s.prefix)) return;
  const cmd = text.slice(s.prefix.length).split(/\s+/)[0].toLowerCase();
  if (await require('./commands').handle(sock, m, { jid, user, isGroup, prefix: s.prefix, text, botName: s.botName })) return;
  const custom = db.get('commands');
  if (cmd === 'ping') await sock.sendMessage(jid, { text: 'pong 👑' }, { quoted: m });
  else if (cmd === 'menu') {
    const extra = Object.keys(custom).map(c => s.prefix + c).join('\n');
    await sock.sendMessage(jid, { text: `♛ ${s.botName}\n${s.prefix}ping\n${s.prefix}menu\n${extra}`.trim() }, { quoted: m });
  } else if (custom[cmd]) await sock.sendMessage(jid, { text: custom[cmd] }, { quoted: m });
}

function attach(sock) {
  sock.ev.on('messages.upsert', async ({ messages, type }) => {
    if (type !== 'notify') return;
    for (const m of messages) { try { await onMessage(sock, m); } catch (e) { log.error('message: ' + e.message); } }
  });
  sock.ev.on('group-participants.update', async (ev) => {
    try {
      if (ev.action !== 'add' && ev.action !== 'remove') return;
      const s = db.get('settings'), md = await sock.groupMetadata(ev.id);
      for (const p of ev.participants) {
        const jid = idOf(p);
        const tpl = ev.action === 'add' ? s.welcome : s.goodbye;
        await sock.sendMessage(ev.id, { text: tpl.replace('@user', '@' + num(jid)).replace('@group', md.subject), mentions: [jid] });
      }
    } catch (e) { log.error('participants: ' + e.message); }
  });
}
module.exports = { attach };
