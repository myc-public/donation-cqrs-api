# CI (EDA8) : le BuildConfig impose l'image de base et le contexte ne contient que app.jar (valeurs par defaut ci-dessous).
# Local : la pile passe JAR_FILE=target/*.jar ; BASE_IMAGE = meme image, depuis le registry public Red Hat.
ARG BASE_IMAGE=registry.access.redhat.com/ubi8/openjdk-21-runtime:1.18
FROM ${BASE_IMAGE}

ARG JAR_FILE=app.jar
COPY ${JAR_FILE} /deployments/app.jar

EXPOSE 8080
USER 185

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/deployments/app.jar"]
