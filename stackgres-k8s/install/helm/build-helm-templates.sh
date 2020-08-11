#!/bin/sh

set -e

cd "$(dirname "$0")"

STACKGRES_VERSION=$(grep '<artifactId>stackgres-parent</artifactId>' "../../src/pom.xml" -A 2 -B 2 \
 | grep -o '<version>\([^<]\+\)</version>' | tr '<>' '  ' | cut -d ' ' -f 3)

mkdir -p "target/templates"
cat << EOF > "target/templates/demo-operator.yml"
apiVersion: v1
kind: Namespace
metadata:
  name: stackgres
---
EOF
for CRD in stackgres-operator/crds/*.yaml
do
  cat "$CRD" >> "target/templates/demo-operator.yml"
  echo --- >> "target/templates/demo-operator.yml"
done
helm repo add stable https://kubernetes-charts.storage.googleapis.com
helm repo update
helm dependency update stackgres-operator
helm dependency update stackgres-cluster

helm template --namespace stackgres stackgres-operator \
  stackgres-operator \
  --set-string adminui.service.type=LoadBalancer \
  >> "target/templates/demo-operator.yml"

helm template --namespace default simple \
  stackgres-cluster \
  --set nonProductionOptions.createMinio=false \
  --set configurations.create=true \
  --set cluster.create=false \
  > "target/templates/demo-simple-config.yml"

helm template --namespace default simple \
  stackgres-cluster \
  --set configurations.create=false \
  --set cluster.create=true \
  --set profiles=null \
  --set cluster.sgInstanceProfile=size-xs \
  --set cluster.instances=2 \
  --set instanceProfiles=null \
  --set nonProductionOptions.createMinio=false \
  --set nonProductionOptions.disableClusterPodAntiAffinity=true \
  > "target/templates/demo-simple-cluster.yml"

rm -rf target/minio
helm fetch stable/minio \
  --version 5.0.26 \
  --untar --untardir target
helm template minio \
  target/minio \
  --set buckets[0].name=stackgres,buckets[0].policy=none,buckets[0].purge=true \
  > "target/templates/demo-minio.yml"

mkdir -p "target/public/downloads/stackgres-k8s/stackgres"
rm -rf "target/public/downloads/stackgres-k8s/stackgres/$STACKGRES_VERSION"
cp -a target/templates "target/public/downloads/stackgres-k8s/stackgres/$STACKGRES_VERSION"
