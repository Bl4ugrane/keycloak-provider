FROM quay.io/keycloak/keycloak:22.0.4

ENV KEYCLOAK_ADMIN=admin
ENV KEYCLOAK_ADMIN_PASSWORD=admin
ENV KC_HOSTNAME=localhost
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
ENV KC_DB_URL=jdbc:postgresql://postgres:5432/postgres
ENV KC_DB=postgres
ENV KC_DB_SCHEMA=public
ENV KC_DB_USERNAME=postgres
ENV KC_DB_PASSWORD=postgres

WORKDIR /opt/keycloak
COPY *.jar /opt/keycloak/providers
COPY themes/ /opt/keycloak/themes/
RUN /opt/keycloak/bin/kc.sh build