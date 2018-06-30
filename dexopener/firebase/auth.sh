#!/bin/sh

# Check $GCLOUD_SERVICE_KEY is not empty
test -n "$GCLOUD_SERVICE_KEY" || { echo '$GCLOUD_SERVICE_KEY is empty'; exit 1; }

ROOT_DIR=$(cd "$(dirname $0)/.." && pwd)

# Authenticate to GCP
TMP_DIR="$ROOT_DIR/build/tmp"
mkdir -p ${TMP_DIR}
GCLOUD_SERVICE_KEY_JSON="$TMP_DIR/gcloud-service-key.json"
echo ${GCLOUD_SERVICE_KEY} | base64 -di > ${GCLOUD_SERVICE_KEY_JSON}
gcloud auth activate-service-account --key-file ${GCLOUD_SERVICE_KEY_JSON}
rm ${GCLOUD_SERVICE_KEY_JSON}
