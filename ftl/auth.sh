#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR=$(cd "$(dirname $0)/.." && pwd)

# Check the number of parameters
test $# -eq 1 || { echo "Usage: $(basename $0) PROJECT_ID"; exit 1; }
PROJECT_ID=$1

# Authenticate to GCP
TMP_DIR="$ROOT_DIR/build/tmp"
mkdir -p ${TMP_DIR}
GCLOUD_SERVICE_KEY_JSON="$TMP_DIR/gcloud-service-key.json"
echo ${GCLOUD_SERVICE_KEY} | base64 -di > ${GCLOUD_SERVICE_KEY_JSON}
gcloud auth activate-service-account --key-file ${GCLOUD_SERVICE_KEY_JSON}
rm ${GCLOUD_SERVICE_KEY_JSON}

# Set project
gcloud config set project ${PROJECT_ID}
