#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR=$(cd "$(dirname $0)/.." && pwd)

# Check the number of parameters
test $# -eq 1 || { echo "Usage: $(basename $0) TEST_APK"; exit 1; }
TEST_APK=$1

# Run tests on Firebase Test Lab
gcloud firebase test android run \
    --type instrumentation \
    --app "$ROOT_DIR/ftl/dummy.apk" \
    --test ${TEST_APK} \
    --device model=Pixel2,version=28,locale=en_US,orientation=portrait \
    --no-record-video \
    --no-performance-metrics
