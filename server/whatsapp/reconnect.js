let attempt = 0;
function nextDelay() { attempt++; return Math.min(60000, 2000 * 2 ** Math.min(attempt, 5)); }
function reset() { attempt = 0; }
module.exports = { nextDelay, reset };
