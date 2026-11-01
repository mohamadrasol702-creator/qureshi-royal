# Qureshi Royal v0.5
One engine (`server/`), two ways to run it:
1. LOCAL (default): inside the APK, no server, no Termux.
2. VPS (24/7): run `server/` on a VPS; the app is only the remote control:
   config url http://SERVER_IP:3000
   config token YOUR_TOKEN

## Build the APK (GitHub Actions)
Push this folder to GitHub > Actions > Build APK > Run workflow > download artifact.

## VPS install (Ubuntu)
sudo apt update && sudo apt install -y git curl
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash - && sudo apt install -y nodejs
sudo npm i -g pm2
cd server && npm install
cp .env.example .env     # put API_TOKEN (openssl rand -hex 24)
pm2 start ecosystem.config.js && pm2 save && pm2 startup
sudo ufw allow 3000/tcp
