#!/bin/sh

set -e

JOBS_IMAGE_NAME="${JOBS_IMAGE_NAME:-"stackgres/jobs:main"}"
BASE_IMAGE="registry.access.redhat.com/ubi8-minimal:8.3-298.1618432845"
TARGET_JOBS_IMAGE_NAME="${TARGET_JOBS_IMAGE_NAME:-$JOBS_IMAGE_NAME}"

docker build -t "$TARGET_JOBS_IMAGE_NAME" --build-arg BASE_IMAGE="$BASE_IMAGE" -f jobs/src/main/docker/Dockerfile.native jobs
