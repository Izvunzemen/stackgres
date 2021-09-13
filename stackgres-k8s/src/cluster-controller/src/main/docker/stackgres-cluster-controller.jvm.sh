#!/bin/sh

if [ "$DEBUG_CLUSTER_CONTROLLER" = true ]
then
  set -x
  DEBUG_JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=$([ "$DEBUG_CLUSTER_CONTROLLER_SUSPEND" = true ] && echo y || echo n)"
fi
if [ -n "$CLUSTER_CONTROLLER_LOG_LEVEL" ]
then
  APP_OPTS="$APP_OPTS -Dquarkus.log.level=$CLUSTER_CONTROLLER_LOG_LEVEL"
fi
if [ "$CLUSTER_CONTROLLER_SHOW_STACK_TRACES" = true ]
then
  APP_OPTS="$APP_OPTS -Dquarkus.log.console.format=%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c{4.}] (%t) %s%e%n"
fi
exec java \
  -Djava.net.preferIPv4Stack=true \
  -Djava.awt.headless=true -XX:MaxRAMPercentage=75.0 \
  -Djava.util.logging.manager=org.jboss.logmanager.LogManager \
  $JAVA_OPTS $DEBUG_JAVA_OPTS -jar /app/stackgres-cluster-controller.jar \
  -Dquarkus.http.host=0.0.0.0 \
  -Dquarkus.http.port=8080 \
  -Dquarkus.http.ssl-port=8443 \
  $APP_OPTS
