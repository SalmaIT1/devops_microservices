# Utilise l'image Java
FROM openjdk:17-jdk-alpine

# Crée un dossier temporaire
VOLUME /tmp

# Spécifie l’argument pour ton jar
ARG JAR_FILE=target/*.jar

# Copie le jar dans l'image
COPY ${JAR_FILE} app.jar

# Démarre l’application
ENTRYPOINT ["java","-jar","/app.jar"]
