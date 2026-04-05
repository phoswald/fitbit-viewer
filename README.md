# fitbit-viewer

Experiments with Fitbit Web API

## Run Standalone

~~~
$ mvn clean verify
$ export FITBIT_DATASOURCE_JDBC_URL=
$ export FITBIT_DATASOURCE_USERNAME=
$ export FITBIT_DATASOURCE_PASSWORD=
$ export FITBIT_CLIENT_ID=
$ export FITBIT_CLIENT_SECRET=
$ export FITBIT_COOKIE_SECRET=
$ java -jar target/quarkus-app/quarkus-run.jar
~~~

## Run with Dev Mode

~~~
$ mvn quarkus:dev
~~~

## URLs

- http://localhost:8080/
