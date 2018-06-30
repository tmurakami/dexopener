#!/bin/sh

# Check the number of parameters
test $# -eq 1 || { echo "Usage: $(basename $0) PROJECT_ID"; exit 1; }

ROOT_DIR=$(cd "$(dirname $0)/.." && pwd)
PROJECT_ID=$1

# Run tests on Firebase Test Lab
gcloud config set project ${PROJECT_ID}
if gcloud firebase test android run \
    --type instrumentation \
    --app "$ROOT_DIR/firebase/dummy.apk" \
    --test "$ROOT_DIR/build/outputs/apk/androidTest/debug/$PROJECT_ID-debug-androidTest.apk" \
    --device model=Pixel2,version=28,locale=en_US,orientation=portrait \
    --no-record-video \
    --no-performance-metrics
then
    echo 'Test matrix successfully finished'
else
    echo "Test matrix exited abnormally with non-zero exit code: $?"
    exit 1
fi
