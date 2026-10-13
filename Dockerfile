# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.8
ARG PLATFORM=""
# Application image

FROM hmctsprod.azurecr.io/base/java${PLATFORM}:25-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/rd-location-ref-data-load.jar /opt/app/

EXPOSE 8099
CMD [ "rd-location-ref-data-load.jar" ]
