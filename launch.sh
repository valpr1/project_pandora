#!/bin/bash

export SERVER_PORT=8080

echo "🚀 Lancement de Pandora Web Desktop..."
echo "Patientez pendant l'initialisation du serveur local."

# Lancer l'application en arrière-plan
java -jar target/pandora-desktop.jar &

# Attendre que le serveur réponde
while ! nc -z localhost 8080; do   
  sleep 0.5
done

echo "✅ Serveur démarré."
echo "🌍 Ouverture de l'interface dans le navigateur par défaut..."

# Ouvrir selon l'OS (macOS et Linux)
if which xdg-open > /dev/null
then
  xdg-open http://localhost:8080/
elif which open > /dev/null
then
  open http://localhost:8080/
fi

# Rendre la main au process Java pour voir les logs
wait
