// Entry point inside the app (nodejs-mobile, Node 18).
const nodeCrypto = require('crypto');
if (!globalThis.crypto) globalThis.crypto = nodeCrypto.webcrypto; // needed by Baileys on Node 18
require('./core/server.js');
