#!/bin/sh

APP_OPTS="${APP_OPTS:-"-Dquarkus.http.host=0.0.0.0 -Dquarkus.http.port=8080 -Dquarkus.http.ssl-port=8443 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"}"
if [ "$DEBUG_JOBS" = true ]
then
  set -x
  APP_OPTS="$APP_OPTS -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=$([ "$DEBUG_JOBS_SUSPEND" = true ] && echo y || echo n)"
fi
if [ -n "$JOBS_LOG_LEVEL" ]
then
  APP_OPTS="$APP_OPTS -Dquarkus.log.level=$JOBS_LOG_LEVEL"
fi
if [ "$JOBS_SHOW_STACK_TRACES" = true ]
then
  APP_OPTS="$APP_OPTS -Dquarkus.log.console.format=%d{yyyy-MM-dd HH:mm:ss,SSS} %-5p [%c{4.}] (%t) %s%e%n"
fi
exec /app/stackgres-jobs $APP_OPTS
