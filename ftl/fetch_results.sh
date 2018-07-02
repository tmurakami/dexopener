#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR=$(cd "$(dirname $0)/.." && pwd)

# Check the number of parameters
test $# -eq 2 || { echo "Usage: $(basename $0) BUCKET_NAME RESULTS_DIR"; exit 1; }
BUCKET_NAME=$1
RESULTS_DIR=$2

# Download test results
mkdir -p ${RESULTS_DIR}
gsutil -m cp -r -U $(gsutil ls "gs://$BUCKET_NAME" | tail -1) ${RESULTS_DIR}
