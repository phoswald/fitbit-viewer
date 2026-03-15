# fitbit-viewer

Experiments with Fitbit Web API

## Run Standalone

~~~
$ mvn clean verify
$ export APP_SAMPLE_CONFIG=ValueFromShell
$ java \
  -Dquarkus.http.port=8080 \
  -jar target/quarkus-app/quarkus-run.jar
~~~

## Run with Dev Mode

~~~
$ mvn quarkus:dev \
  -Dquarkus.http.port=8080
~~~

## URLs

- http://localhost:8080/
