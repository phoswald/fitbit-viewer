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

## Google Health API

Sources:

- https://developers.google.com/health
- https://developers.google.com/health/data-types

Conecpts:

- **Civil time** means local wall-clock time: the date and time a clock would have shown where the user was when the data was recorded.
- **Physical time** is the absolute moment in UTC.

Endpoints:

- **dataPoints:dailyRollUp**: returns one aggregate per calendar day.
  Uses **civil** time. This is the one to use for daily **dashboards**.
- **dataPoints:reconcile**: returns a consistent stream of **deduplicated** records
  (across sync batches and multiple recording devices). You get **records, not totals**.
- **dataPoints (list)**: returns **raw** stored data points, paginated, without deduplication.

Overview:

| Data Type           | Endpoint               | Comment            |
|---------------------|------------------------|--------------------|
| Profile             | profile                | OK                 |
| Steps               | dataPoints:dailyRollUp | OK, daily          |
| Heart Rate          | dataPoints:reconcile   | OK, no dailyRollUp |
| Cardio Fitness      | dataPoints:reconcile   | OK, no dailyRollUp |
| Active Zone Minutes | dataPoints:dailyRollUp | OK, daily          |
| Sleep               | dataPoints:reconcile   | OK, records        |
| Activities          | dataPoints (list)      | OK, no dailyRollUp |

### Migration status

Profile:

- Picture URL:   ✅️ from JWT token (⚠️ improve caching, rate limit applies)
- Display Name:  ❌️ missing
- Full Name:     ✅️ from JWT token
- Date of Brith: ❌️ missing
- Age:           ✅️ OK
- Gender:        ❌️ missing
- Member since:  ✅️ OK
- Height/Weight: ✅️ OK
- Stride Walking/Running: ✅️ OK (⚠️ limit digits)
- Steps (daily average):  ❌️ missing
- User ID:       ✅️ from JWT token

Steps:

- Steps: ✅️ OK (️👉️ many small differences in 2026-09, FitBit values are slightly larger)

Heart Rate:

- Resting Heart Rate: ✅️ OK (️👉️ many small differences in 2026-09)

Cardio Fitness:

- Score (estimated): ✅️ OK (️👉️ many small differences in 2026-09, Google values are fractional)
- Score (min, max):  ❌️ missing (data before 2026-08-22)

Active Zone Minutes:

- Total (min):    ✅️ OK (identical in 2026-06...2026-09, 💡 calculated)
- Fat Burn (min): ✅️ OK (identical in 2026-06...2026-09)
- Cardio (min):   ✅️ OK (identical in 2026-06...2026-09, Google with 0 instead of null)
- Peak (min):     ✅️ OK (identical in 2026-06...2026-09, Google with 0 instead of null)

Sleep:

- Total: ✅️ OK (👉️ many small differences in 2026-09, 💡 calculated)
- Sleep: ✅️ OK (👉️ many small differences in 2026-09, 💡 calculated)
- Deep:  ✅️ OK (👉️ many small differences in 2026-09)
- Light: ✅️ OK (👉️ many small differences in 2026-09)
- REM:   ✅️ OK (👉️ many small differences in 2026-09)
- Wake:  ✅️ OK (👉️ many small differences in 2026-09)
- Asleep   (classic): ✅️ OK (identical in 2026-01-05)
- Restless (classic): ✅️ OK (identical in 2026-01-05, Google with 0 instead of null)
- Awake    (classic): ✅️ OK (identical in 2026-01-05, Google with 0 instead of null)

Activities:

- Ordering:        ️✅️ OK
- Date & Time:     ✅️ OK (identical (?))
- Duration:        ✅️ OK (identical (?))
- Active Duration: ✅️ OK (👉️ many small differences in 2026-09)
- Calories:        ✅️ OK (👉️ many small differences in 2026-09)
- Steps:           ✅️ OK (👉️ many small differences in 2026-09)
- Distance:        ✅️ OK (👉️ many small differences in 2026-09)
- Pace:            ✅️ OK (👉️ many small differences in 2026-09)
- Speed:           ❌️ missing
- Avg Heart Rate:  ✅️ OK (identical (?))
- Log Type:        ✅️ OK (auto_detected -> PASSIVELY_MEASURED; mobile_run -> ACTIVELY_MEASURED)
- Source Name:     ✅️ OK (Inspire HR, Inspire 2, Charge 6; Fitbit for Android -> MobileTrack) 
                         (⚠️ sometimes null: 407883839870725264)
- Source Type:     ✅️ OK (tracker -> FITNESS_BAND, api -> PHONE)
- Source Features: ❌️ missing

Activity Heart Rate Zones:

- Zone:         ✅️ OK (newly hard-coded: Out of Range, Fat Burn, Cardio, Peak; identical)
- Heart Rate:   ✅️ OK (newly hard-coded: 30...98, 99...122, 123...152, 153...220; ⚠️ should depend on current personal fitness level)
- Time:         ✅️ OK (👉️ many small differences in 2026-09)
- Calories Out: ❌️ missing
- 👉️ difference when source is phone (6935854779612435696): missing in Google, single value for Out of Range	(30...220) in FitBit

Activity Levels: ❌️ missing

- Level: ❌️ missing (was: sedentary, lightly, fairly, very)
- Time:  ❌️ missing (was different from time in heart reate zones, but with equal respective sums)

Activity TCX

- Timestamp:  ✅️ OK (identical)
- Location:   ✅️ OK (identical)
- Altitude:   ✅️ OK (identical)
- Heart Rate: ✅️ OK (identical)
- Laps:       ✅️ OK (identical)
- ✅️ sometimes bitwise identical (probably when sources is phone),
- 👉️ sometimes differs in calories of lap only (probably when sources is tracker)
- 👉️ sometimes differs in missing positions and/or heart rates of some track points.
