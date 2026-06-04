# fitbit-viewer

Experiments with Fitbit Web API

## Run Standalone

~~~
$ mvn clean package
$ export FITBIT_DATASOURCE_JDBC_URL=
$ export FITBIT_DATASOURCE_USERNAME=
$ export FITBIT_DATASOURCE_PASSWORD=
$ export FITBIT_CLIENT_ID=
$ export FITBIT_CLIENT_SECRET=
$ export FITBIT_REDIRECT_URI=
$ export FITBIT_COOKIE_SECRET=
$ java -jar target/quarkus-app/quarkus-run.jar
~~~


## Run with Docker

~~~
$ mvn clean package -P docker
$ docker run -it --name fitbit-viewer --rm \
  -p 8080:8080 \
  -e FITBIT_DATASOURCE_JDBC_URL \
  -e FITBIT_DATASOURCE_USERNAME \
  -e FITBIT_DATASOURCE_PASSWORD \
  -e FITBIT_CLIENT_ID \
  -e FITBIT_CLIENT_SECRET \
  -e FITBIT_REDIRECT_URI \
  -e FITBIT_COOKIE_SECRET \
  philip/fitbit-viewer:0.1.0-SNAPSHOT
~~~

## Run with Dev Mode

~~~
$ mvn quarkus:dev
~~~

## URLs

- http://localhost:8080/fitbit
- http://localhost:8080/fitbit/kitchensink.html
