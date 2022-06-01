# Introduction

Le composant "proxy" de connexion au lot 1 pour l'application Blade Balancing pour Airbus Helicopters

# Kit Starter

## Installation et configuration

1. Installer et configurer Maven
2. Installer JAVA 11
3. Installer Git
4. Instaler un IDE
5. Installer Postman

## Récupération du projet Git avec IntelliJ

1. Allez dans File/New/Project from Version Control...
2. Saisissez votre lien de dépôt Git.
3. (Facultatif) Paramétrez le "Directory".
4. Cliquez sur "Clone" en bas à droite.
5. (Facultatif) Allez dans File/Settings/Maven pour mettre à jour le maven path et user settings

## Préparation et exécution du projet

1. Allez dans l'onglet "Terminal" (en bas de votre fenêtre).
2. Vérifiez que vous êtes bien dans le répertoire du projet.
3. Exécutez la commande suivante : `mvn clean install`
4. Si tout se passe bien le terminal affiche : **BUILD SUCESS**
5. Exécutez ensuite la commande suivante pour lancer le projet: `mvn spring-boot:run -Dspring-boot.run.profiles=local`

## Intéraction avec le projet

1. Vérifiez que votre projet back est bien lancé.
2. Dans Postman, utilisez cette adresse pour intéragir avec le projet: GET `localhost:8081`

# Tests unitaires/intégrations (spring)
1. Tapez dans le terminal : `mvn test`

# Tests de vulnérabilités (owasp)
1. Tapez dans le terminal : `mvn dependency-check:check`
2. Un rapport est généré -> sps_back/target/dependency-check-report.html

# Métriques de l'application
- Elles sont disponibles sur le endpoint /monitoring
- Exemple : GET - http://localhost:8090/monitoring/info

# Documentation
- Le CI d'interface est mis à disposition via openAPI sur le endpoint /monitoring/swagger-ui
________________________________________________________________________________ 

# Standard des commits

- Pour les US ==> **feat**(n°US jira): description
- Pour les bugs ==> **fix**(n° bug jira): description
- Pour les refacto transverse, sonar, conf, ... ==> **chore**: description
- Pour l'ajout de TU/TI ==> **test**(n°US jira): description 
