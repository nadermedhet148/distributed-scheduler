FROM amazoncorretto:21.0.2-alpine3.19

# Copy the Quarkus application files
COPY --chown=185 target/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 target/quarkus-app/*.jar /deployments/
COPY --chown=185 target/quarkus-app/app/ /deployments/app/
COPY --chown=185 target/quarkus-app/quarkus/ /deployments/quarkus/

USER 185

CMD ["java", "--enable-preview", "-XX:+UseZGC", "-XX:+ZGenerational", "-jar", "/deployments/quarkus-run.jar"]