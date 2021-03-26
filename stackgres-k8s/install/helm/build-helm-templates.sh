#!/bin/sh

set -e

cd "$(dirname "$0")"

STACKGRES_VERSION="$(grep '<artifactId>stackgres-parent</artifactId>' "../../src/pom.xml" -A 2 -B 2 \
 | grep -o '<version>\([^<]\+\)</version>' | tr '<>' '  ' | cut -d ' ' -f 3)"

rm -Rf "target/stackgres-operator" "target/stackgres-cluster" "target/templates"
mkdir -p "target"

# Fix for Error: chart requires kubeVersion: X which is incompatible with Kubernetes Y
cp -a stackgres-operator "target/."
cp -a stackgres-cluster "target/."
sed -i '/^kubeVersion: .*$/d' "target/stackgres-operator/Chart.yaml"
sed -i '/^kubeVersion: .*$/d' "target/stackgres-cluster/Chart.yaml"
find target/stackgres-operator -type l \
  | while read LINK
    do
      ln -f -s "../$(readlink "$LINK")" "$LINK"
    done
find target/stackgres-cluster -type l \
  | while read LINK
    do
      ln -f -s "../$(readlink "$LINK")" "$LINK"
    done

mkdir -p "target/templates"
cat << EOF > "target/templates/stackgres-operator-demo.yml"
apiVersion: v1
kind: Namespace
metadata:
  name: stackgres
---
EOF

for HELM_REPO in 'minio|https://helm.min.io/'
do
  if ! helm repo list | grep -q "\s${HELM_REPO#*|}\s*$"
  then
    if helm repo list | grep -q "^${HELM_REPO%|*}"
    then
      helm repo remove "${HELM_REPO%|*}"
    fi
    helm repo add "${HELM_REPO%|*}" "${HELM_REPO#*|}"
    helm repo add minio https://helm.min.io/
  fi
done
helm repo update

helm template --namespace stackgres stackgres-operator \
  "target/stackgres-operator" \
  --set-string adminui.service.type=LoadBalancer \
  >> "target/templates/stackgres-operator-demo.yml"

helm template --namespace default simple \
  "target/stackgres-cluster" \
  --set configurations.create=true \
  --set cluster.create=false \
  > "target/templates/stackgres-simple-config-demo.yml"

helm template --namespace default simple \
  "target/stackgres-cluster" \
  --set configurations.create=false \
  --set cluster.create=true \
  --set profiles=null \
  --set cluster.sgInstanceProfile=size-xs \
  --set cluster.instances=2 \
  --set instanceProfiles=null \
  --set nonProductionOptions.disableClusterPodAntiAffinity=true \
  > "target/templates/stackgres-simple-cluster-demo.yml"

rm -rf target/minio
helm fetch minio/minio \
  --version 7.0.1 \
  --untar --untardir target

helm template --namespace default minio \
  target/minio \
  --set buckets[0].name=stackgres,buckets[0].policy=none,buckets[0].purge=true \
  | grep -v '^ \+namespace: "\?default"\?$' \
  > "target/templates/minio-demo.yml"

mkdir -p "target/public/downloads/stackgres-k8s/stackgres"
rm -rf "target/public/downloads/stackgres-k8s/stackgres/$STACKGRES_VERSION"
cp -a target/templates "target/public/downloads/stackgres-k8s/stackgres/$STACKGRES_VERSION"
