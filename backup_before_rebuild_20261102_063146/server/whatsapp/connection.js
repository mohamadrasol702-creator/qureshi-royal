const log = require('../core/logger');
const sessions = require('./session-manager');
const { nextDelay, reset } = require('./reconnect');
const handler = require('./handler');

const S = { status: 'stopped', sock: null, enabled: false, pairing: null, groups: [], timer: null, gen: 0 };
let L = null;

async function loadLib() {
  if (L) return L;
  const mod = await import('@whiskeysockets/baileys');
  const ns = (mod.default && typeof mod.default === 'object') ? mod.default : mod;
  L = {
    make: typeof ns.default === 'function' ? ns.default : ns.makeWASocket,
    useMultiFileAuthState: ns.useMultiFileAuthState, DisconnectReason: ns.DisconnectReason,
    fetchLatestBaileysVersion: ns.fetchLatestBaileysVersion, Browsers: ns.Browsers
  };
  return L;
}

function state() { return { status: S.status, enabled: S.enabled }; }

async function start() {
  if (S.sock) return;
  clearTimeout(S.timer);
  S.enabled = true; S.status = 'connecting';
  const lib = await loadLib();
  const { state: auth, saveCreds } = await lib.useMultiFileAuthState(sessions.dir());
  const v = await lib.fetchLatestBaileysVersion().catch(() => ({}));
  const gen = ++S.gen;
  const sock = lib.make({
    auth, version: v.version, logger: require('pino')({ level: 'silent' }),
    browser: lib.Browsers.ubuntu('Qureshi Royal'), printQRInTerminal: false, markOnlineOnConnect: false
  });
  S.sock = sock;
  sock.ev.on('creds.update', saveCreds);
  sock.ev.on('connection.update', async (u) => {
    if (gen !== S.gen) return;
    const { connection, lastDisconnect, qr } = u;
    if (qr && S.pairing && !S.pairing.code && !auth.creds.registered) {
      try { S.pairing.code = await sock.requestPairingCode(S.pairing.phone); S.pairing.resolve(S.pairing.code); }
      catch (e) { S.pairing.reject(e); }
    }
    if (connection === 'open') { S.status = 'connected'; S.pairing = null; reset(); log.info('WhatsApp connected'); }
    if (connection === 'close') {
      const code = lastDisconnect && lastDisconnect.error && lastDisconnect.error.output && lastDisconnect.error.output.statusCode;
      S.sock = null; S.status = 'disconnected';
      if (code === lib.DisconnectReason.loggedOut) {
        log.warn('Logged out from phone; session cleared'); sessions.clear(); S.enabled = false; return;
      }
      if (S.enabled) {
        const d = nextDelay();
        log.warn(`Connection closed (${code}). Reconnecting in ${d} ms`);
        S.timer = setTimeout(() => start().catch(e => log.error(e.message)), d);
      }
    }
  });
  handler.attach(sock);
}

async function stop(disable) {
  clearTimeout(S.timer);
  if (disable) S.enabled = false;
  S.gen++;
  const s = S.sock; S.sock = null;
  if (s) { try { s.end(undefined); } catch {} }
  S.status = 'stopped';
}

function requestPairing(phone) {
  return new Promise(async (resolve, reject) => {
    const clean = String(phone).replace(/\D/g, '');
    if (clean.length < 8) return reject(new Error('Invalid number (country code + digits only)'));
    if (sessions.isLinked()) return reject(new Error('Already linked. Use: whatsapp disconnect first'));
    const timer = setTimeout(() => reject(new Error('Timeout, try again')), 30000);
    S.pairing = { phone: clean, code: null, resolve: c => { clearTimeout(timer); resolve(c); }, reject: e => { clearTimeout(timer); reject(e); } };
    try { await stop(false); await start(); } catch (e) { clearTimeout(timer); reject(e); }
  });
}

async function logout() {
  if (S.sock) { try { await S.sock.logout(); } catch {} }
  await stop(true); sessions.clear();
}

function need() { if (!S.sock || S.status !== 'connected') throw new Error('WhatsApp is not connected'); }
async function groups() {
  need();
  const g = await S.sock.groupFetchAllParticipating();
  S.groups = Object.values(g).map(x => ({ id: x.id, name: x.subject, size: x.participants.length }));
  return S.groups;
}
function pick(n) { const g = S.groups[n - 1]; if (!g) throw new Error('Run "group list" first and use a valid number'); return g; }
async function setAnnounce(n, closed) {
  need(); const g = pick(n);
  await S.sock.groupSettingUpdate(g.id, closed ? 'announcement' : 'not_announcement');
  return `${g.name}: ${closed ? 'closed (admins only)' : 'open (everyone)'}`;
}
async function members(n) {
  need(); const g = pick(n);
  const md = await S.sock.groupMetadata(g.id);
  const lines = md.participants.slice(0, 40).map(p => `${p.admin ? '★ ' : ''}${String(p.id).split('@')[0]}`);
  return `${g.name} - ${md.participants.length} members\n` + lines.join('\n');
}

module.exports = { state, start, stop, requestPairing, logout, groups, setAnnounce, members };
