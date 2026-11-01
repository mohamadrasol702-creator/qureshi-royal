# Qureshi Royal (standalone: no Termux, no server)

Bot engine (Node.js) runs INSIDE the app as a foreground service.

## Build with GitHub (no Android Studio needed)
1. Make a free GitHub account, create a new repository.
2. Upload ALL files of this folder (keep the folders, including `.github`).
3. Tab "Actions" > "Build APK" > "Run workflow". Wait ~10 minutes.
4. Open the finished run > Artifacts > download `QureshiRoyal-APK`, unzip, install the APK.

## Build with Android Studio
Put libnode.so (arm64-v8a) + headers from nodejs-mobile into app/libnode/, run `npm install --omit=dev` in nodejs-src, zip nodejs-src into app/src/main/assets/nodejs-project.zip, then Run.

## Screens
Home (status + start/stop), WhatsApp (pairing code), Custom (name, prefix, messages, protection, own commands), Terminal.

## First use (inside the app)
battery
whatsapp pairing 93700000000
bot start
group list / security antilink on / settings set botname X / commands create hi Hello
