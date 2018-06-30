#!/bin/sh

# Check the number of parameters
test $# -eq 1 || { echo "Usage: $(basename $0) BUCKET_NAME"; exit 1; }

ROOT_DIR=$(cd "$(dirname $0)/.." && pwd)
BUCKET_NAME=$1

# Download test results
TEST_RESULTS_DIR="$ROOT_DIR/build/test-results/firebase"
mkdir -p ${TEST_RESULTS_DIR}
gsutil -m cp -r -U $(gsutil ls "gs://$BUCKET_NAME" | tail -1) ${TEST_RESULTS_DIR}
