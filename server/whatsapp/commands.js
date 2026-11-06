const db = require('../database/database-manager');
const log = require('../core/logger');

const MAX_WARN = 3;
const ALIAS = {
  'منو':'menu','راهنما':'menu','help':'menu','start':'menu','شروع':'menu',
  'قوانین':'rules','ثبت_قوانین':'setrules','اخطار':'warn','اخطارها':'warns',
  'پاک_اخطار':'resetwarn','اخراج':'kick','حذف':'kick','remove':'kick',
  'افزودن':'add','ارتقا':'promote','تنزل':'demote','باز':'open','بستن':'close',
  'سکوت':'mute','رفع_سکوت':'unmute','ادمینها':'admins','همه':'tagall','اطلاعات':'info'
};
const num = j => String(j).split('@')[0].split(':')[0];
const toJid = n => String(n).replace(/\D/g, '') + '@s.whatsapp.net';
const BOX = '╔═══━━━─── • ───━━━═══╗', DIV = '╠══════════════════════╣', END = '╚═══━━━─── • ───━━━═══╝';
const crown = (brand, title, body) => `${BOX}\n𓆩 ♛ ${brand} ♛ 𓆪\n${DIV}\n👑 ${title}\n${DIV}\n${body}\n${END}`;

function targetOf(m, args) {
  const ci = (m.message && m.message.extendedTextMessage && m.message.extendedTextMessage.contextInfo) || {};
  if (ci.mentionedJid && ci.mentionedJid.length) return ci.mentionedJid[0];
  if (ci.participant) return ci.participant;
  const n = (args[0] || '').replace(/\D/g, '');
  return n.length >= 7 ? toJid(n) : null;
}

function isMuted(jid, user) {
  const a = db.get('muted')[jid] || [];
  return a.some(x => num(x) === num(user));
}

async function handle(sock, m, c) {
  const { jid, user, isGroup, prefix, text, botName } = c;
  const parts = text.slice(prefix.length).trim().split(/\s+/);
  const raw = (parts.shift() || '').toLowerCase();
  const cmd = ALIAS[raw] || raw;
  const args = parts, argText = args.join(' ');
  const KNOWN = ['menu','rules','setrules','warn','warns','resetwarn','kick','add','promote','demote','open','close','mute','unmute','admins','tagall','info'];
  if (!KNOWN.includes(cmd)) return false;

  const say = (title, body, mentions) =>
    sock.sendMessage(jid, { text: crown(botName, title, body), mentions: mentions || [] }, { quoted: m });
  const plain = t => sock.sendMessage(jid, { text: t }, { quoted: m });

  try {
    if (cmd === 'menu') {
      const p = prefix, extra = Object.keys(db.get('commands')).map(k => p + k).join(' | ');
      await say('منوی فرماندهی', [
        `⚔️ ${p}kick @عضو | ${p}add شماره`,
        `🛡️ ${p}promote @عضو | ${p}demote @عضو`,
        `🔒 ${p}close | ${p}open | ${p}mute | ${p}unmute`,
        `⚠️ ${p}warn @عضو | ${p}warns | ${p}resetwarn`,
        `📜 ${p}rules | ${p}setrules متن`,
        `📣 ${p}tagall | ${p}admins | ${p}info`,
        `🏓 ${p}ping`, extra ? `✨ ${extra}` : ''
      ].filter(Boolean).join('\n'));
      return true;
    }
    if (!isGroup) { await plain('این فرمان فقط در گروه کار می‌کند.'); return true; }

    const md = await sock.groupMetadata(jid);
    const adminOf = u => { const p = md.participants.find(x => num(x.id) === num(u)); return !!(p && p.admin); };
    const senderAdmin = adminOf(user), botAdmin = adminOf(sock.user.id);
    const needAdmin = async () => senderAdmin || (await say('دسترسی رد شد', '🔐 این فرمان فقط برای ادمین‌هاست.'), false);
    const needBot = async () => botAdmin || (await say('دسترسی ناکافی', '⚙️ ربات باید ادمین گروه باشد.'), false);
    const target = targetOf(m, args);
    const tag = t => '@' + num(t);

    if (cmd === 'rules') { await say('قوانین گروه', db.get('rules')[jid] || 'قوانین هنوز تنظیم نشده است.'); return true; }
    if (cmd === 'info') {
      const ad = md.participants.filter(p => p.admin).length;
      await say('گزارش گروه', `🏷️ نام: ${md.subject}\n👥 اعضا: ${md.participants.length}\n👑 ادمین‌ها: ${ad}`); return true;
    }
    if (cmd === 'admins') {
      const l = md.participants.filter(p => p.admin).map(p => p.id);
      await say('شورای ادمین‌ها', l.map(x => '👑 ' + tag(x)).join('\n') || 'ادمینی نیست.', l); return true;
    }
    if (cmd === 'warns') {
      const who = target || user, n = ((db.get('warns')[jid] || {})[who]) || 0;
      await say('پروندهٔ اخطار', `👤 ${tag(who)}\n⚠️ ${n}/${MAX_WARN}`, [who]); return true;
    }

    if (!(await needAdmin())) return true;

    if (cmd === 'setrules') {
      if (!argText) { await plain(`استفاده: ${prefix}setrules متن قوانین`); return true; }
      db.update('rules', r => { r[jid] = argText.slice(0, 1500); });
      await say('قوانین ثبت شد', '📜 قوانین جدید ذخیره شد.'); return true;
    }
    if (cmd === 'tagall') {
      const all = md.participants.map(p => p.id);
      await sock.sendMessage(jid, { text: crown(botName, 'فراخوان همگانی', `📣 ${argText || 'توجه اعضای گروه'}\n\n` + all.map(u => '𓆩 ' + tag(u) + ' 𓆪').join('\n')), mentions: all }, { quoted: m });
      return true;
    }
    if (cmd === 'open' || cmd === 'close') {
      if (!(await needBot())) return true;
      await sock.groupSettingUpdate(jid, cmd === 'close' ? 'announcement' : 'not_announcement');
      await say('تنظیمات گروه', cmd === 'close' ? '🔒 فقط ادمین‌ها می‌توانند پیام بفرستند.' : '🔓 همهٔ اعضا می‌توانند پیام بفرستند.'); return true;
    }
    if (cmd === 'add') {
      if (!(await needBot())) return true;
      const n = (args[0] || '').replace(/\D/g, '');
      if (n.length < 7) { await plain(`استفاده: ${prefix}add شماره با کد کشور`); return true; }
      await sock.groupParticipantsUpdate(jid, [toJid(n)], 'add');
      await say('درخواست افزودن', `➕ ${n} ارسال شد.`); return true;
    }

    if (!target) { await plain(`عضو را منشن کن یا روی پیامش ریپلای بزن: ${prefix}${cmd} @عضو`); return true; }

    if (cmd === 'resetwarn') {
      db.update('warns', w => { if (w[jid]) delete w[jid][target]; });
      await say('پرونده پاک شد', `♻️ اخطارهای ${tag(target)} پاک شد.`, [target]); return true;
    }
    if (cmd === 'mute' || cmd === 'unmute') {
      db.update('muted', mu => {
        const a = (mu[jid] || []).filter(x => num(x) !== num(target));
        if (cmd === 'mute') a.push(target);
        mu[jid] = a;
      });
      await say('محدودیت عضو', `🔇 ${tag(target)} ${cmd === 'mute' ? 'ساکت شد (پیام‌هایش پاک می‌شود)' : 'آزاد شد'}.\nربات باید ادمین باشد.`, [target]); return true;
    }

    if (!(await needBot())) return true;
    if (num(target) === num(sock.user.id)) { await plain('❌ روی خود ربات ممکن نیست.'); return true; }

    if (cmd === 'warn') {
      let n = 0;
      db.update('warns', w => { w[jid] = w[jid] || {}; n = (w[jid][target] || 0) + 1; w[jid][target] = n; });
      const why = argText.replace(/@\d+/g, '').trim() || 'تخلف از قوانین';
      if (n >= MAX_WARN) {
        await say('حکم اخراج', `⚠️ ${tag(target)} به ${MAX_WARN} اخطار رسید.\n📜 دلیل: ${why}`, [target]);
        await sock.groupParticipantsUpdate(jid, [target], 'remove').catch(() => {});
        db.update('warns', w => { if (w[jid]) delete w[jid][target]; });
      } else await say('اخطار رسمی', `⚠️ عضو: ${tag(target)}\n📜 دلیل: ${why}\n📊 اخطار ${n}/${MAX_WARN}`, [target]);
      return true;
    }
    if (cmd === 'kick') {
      await sock.groupParticipantsUpdate(jid, [target], 'remove');
      await say('حکم اجرا شد', `⚔️ ${tag(target)} از گروه اخراج شد.`, [target]); return true;
    }
    if (cmd === 'promote' || cmd === 'demote') {
      await sock.groupParticipantsUpdate(jid, [target], cmd);
      await say('مدیریت اعضا', `👑 ${tag(target)} ${cmd === 'promote' ? 'ادمین شد' : 'از ادمینی برکنار شد'}.`, [target]); return true;
    }
  } catch (e) {
    log.error('commands: ' + e.message);
    await plain('⚠️ خطا: ' + e.message).catch(() => {});
  }
  return true;
}
module.exports = { handle, isMuted };
