Démarrer consul

> a la racine du projet

```bash
docker compose up -d
```

Dans [school](school)

```bash
./gradlew jooqCodegen
```

```bash
./gradlew quarkusDev
```

Dans [gateway](gateway)

```bash
./gradlew quarkusDev
```

Dans [config](config)

```bash
./gradlew quarkusDev
```

Dans [student](school)

```bash
./gradlew quarkusDev
```

Le port de la gateway est sur 8080

¨Pour modifier la config de la gateway, il faut modifier le fichier [gateway.yaml](config/src/main/resources/META-INF/resources/gateway.yaml)

Routes disponibles:

- *GET* `/school/{id}` : Récupérer une école
- *POST* `/school` JSON: `{"name": "name", "address": "address", "directorName": "directorName"}` : Créer une école
- *DELETE* `/school/{id}` : Supprimer une école
- *PUT* `/school/{id}` JSON: `{"name": "name", "address": "address", "directorName": "directorName"}` : Mettre à jour une école

- *POST* `/student` JSON: `{"name": "name", "gender": "Male", "schoolId": 1}` : Créer un étudiant
- *GET* `/student/{id}` : Récupérer un étudiant
- *DELETE* `/student/{id}` : Supprimer un étudiant
- *PUT* `/student/{id}` JSON: `{"name": "name", "gender": "Male", "schoolId": 1}` : Mettre à jour un étudiant