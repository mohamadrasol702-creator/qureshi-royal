const token = process.env.API_TOKEN || '';
if (token.length < 24) {
  console.error('API_TOKEN is missing or shorter than 24 characters. Set it in .env');
  process.exit(1);
}
module.exports = {
  port: parseInt(process.env.PORT, 10) || 3000,
  host: process.env.HOST || '0.0.0.0',
  token
};
