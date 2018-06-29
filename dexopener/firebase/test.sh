#!/bin/sh

ROOT_DIR=$(cd "$(dirname $0)/.." && pwd)

# Check $GCLOUD_SERVICE_KEY is not empty
test -n "$GCLOUD_SERVICE_KEY" || { echo '$GCLOUD_SERVICE_KEY is empty'; exit 1; }

# Authenticate to GCP
TMP_DIR="$ROOT_DIR/build/tmp"
mkdir -p ${TMP_DIR}
GCLOUD_SERVICE_KEY_JSON="$TMP_DIR/gcloud-service-key.json"
echo ${GCLOUD_SERVICE_KEY} | base64 -di > ${GCLOUD_SERVICE_KEY_JSON}
gcloud auth activate-service-account --key-file ${GCLOUD_SERVICE_KEY_JSON}
rm ${GCLOUD_SERVICE_KEY_JSON}

# Run tests on Firebase Test Lab
gcloud config set project dexopener
if gcloud firebase test android run \
    --type instrumentation \
    --app "$ROOT_DIR/firebase/dummy.apk" \
    --test "$ROOT_DIR/build/outputs/apk/androidTest/debug/dexopener-debug-androidTest.apk" \
    --device model=Pixel2,version=28,locale=en_US,orientation=portrait \
    --no-record-video \
    --no-performance-metrics
then
    echo "Test matrix successfully finished"
else
    echo "Test matrix exited abnormally with non-zero exit code: " $?
    exit 1
fi

# Download test results
TEST_RESULTS_DIR="$ROOT_DIR/build/test-results/firebase"
mkdir -p ${TEST_RESULTS_DIR}
gsutil -m cp -r -U $(gsutil ls gs://test-lab-52rkbqmf6t0pc-itz864dzcxk5x | tail -1) ${TEST_RESULTS_DIR}
