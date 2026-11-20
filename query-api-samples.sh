#!/bin/bash
set -euo pipefail

if [ -z "${GOOGLE_TOKEN}" ]; then
  echo "Error: GOOGLE_TOKEN environment variable is not set"
  exit 1
fi

# Profile
curl 'https://health.googleapis.com/v4/users/me/profile' -s -X GET \
  --oauth2-bearer "${GOOGLE_TOKEN}" \
  -H 'accept: application/json' \
  -o  src/test/resources/api-samples/profile.response.json

curl 'https://health.googleapis.com/v4/users/me/dataTypes/height/dataPoints' -s -X GET \
  --oauth2-bearer "${GOOGLE_TOKEN}" \
  --url-query "pageSize=1" \
  -H 'accept: application/json' \
  -o  src/test/resources/api-samples/profile-height.response.json

curl 'https://health.googleapis.com/v4/users/me/dataTypes/weight/dataPoints' -s -X GET \
  --oauth2-bearer "${GOOGLE_TOKEN}" \
  --url-query "pageSize=1" \
  -H 'accept: application/json' \
  -o  src/test/resources/api-samples/profile-weight.response.json

# Steps
curl 'https://health.googleapis.com/v4/users/me/dataTypes/steps/dataPoints:dailyRollUp' -s -X POST \
  --oauth2-bearer "${GOOGLE_TOKEN}" \
  -H 'content-type: application/json' \
  -H 'accept: application/json' \
  -d @src/test/resources/api-samples/steps.request.json \
  -o  src/test/resources/api-samples/steps.response.json

# Heart Rate
curl 'https://health.googleapis.com/v4/users/me/dataTypes/daily-resting-heart-rate/dataPoints:reconcile' -s -X GET \
  --oauth2-bearer "${GOOGLE_TOKEN}" \
  --url-query 'filter=daily_resting_heart_rate.date >= "2026-09-01" AND daily_resting_heart_rate.date < "2026-10-01"' \
  -H 'accept: application/json' \
  -o  src/test/resources/api-samples/heartrate.response.json

# Cardio Fitness Score
curl 'https://health.googleapis.com/v4/users/me/dataTypes/daily-vo2-max/dataPoints:reconcile' -s -X GET \
  --oauth2-bearer "${GOOGLE_TOKEN}" \
  --url-query 'filter=daily_vo2_max.date >= "2026-09-01" AND daily_vo2_max.date < "2026-10-01"' \
  -H 'accept: application/json' \
  -o  src/test/resources/api-samples/cardioscore.response.json

# Active Zone Minutes
curl 'https://health.googleapis.com/v4/users/me/dataTypes/active-zone-minutes/dataPoints:dailyRollUp' -s -X POST \
  --oauth2-bearer "${GOOGLE_TOKEN}" \
  -H 'content-type: application/json' \
  -H 'accept: application/json' \
  -d @src/test/resources/api-samples/azm.request.json \
  -o  src/test/resources/api-samples/azm.response.json

# Sleep
curl 'https://health.googleapis.com/v4/users/me/dataTypes/sleep/dataPoints:reconcile' -s -X GET \
  --oauth2-bearer "${GOOGLE_TOKEN}" \
  --url-query 'filter=sleep.interval.civil_end_time >= "2026-09-01" AND sleep.interval.civil_end_time < "2026-10-01"' \
  -H 'accept: application/json' \
  -o  src/test/resources/api-samples/sleep.response.json

# Activities
curl 'https://health.googleapis.com/v4/users/me/dataTypes/exercise/dataPoints' -s -X GET \
  --oauth2-bearer "${GOOGLE_TOKEN}" \
  --url-query 'filter=exercise.interval.civil_start_time >= "2026-09-01" AND exercise.interval.civil_start_time < "2026-10-01"' \
  -H 'accept: application/json' \
  -o  src/test/resources/api-samples/activity.response.json

# Activity TCX
curl 'https://health.googleapis.com/v4/users/me/dataTypes/exercise/dataPoints/3163336676098067440:exportExerciseTcx' -s -X GET \
  --oauth2-bearer "${GOOGLE_TOKEN}" \
  -H 'accept: application/json' \
  -o  src/test/resources/api-samples/tcx.response.json
