@echo off
set SERVER_PORT=8080

echo 🚀 Lancement de Pandora Web Desktop...
echo Patientez pendant l'initialisation du serveur local.

rem Lancer en tâche de fond n'est pas trivial en natif Batch, on lance avec start.
start "Pandora Web Desktop" java -jar target\pandora-desktop.jar

echo ✅ Le serveur demarre dans une nouvelle fenetre.
echo 🌍 Ouverture de l'interface dans votre navigateur...
ping 127.0.0.1 -n 4 > nul
start http://localhost:8080/
