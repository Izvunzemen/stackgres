---
title: SGCluster
weight: 1
url: reference/crd/sgcluster
description: Details about SGCluster configurations
---

StackGres PostgreSQL cluster can be created using a cluster Custom Resource (CR) in Kubernetes.

___

**Kind:** SGCluster

**listKind:** SGClusterList

**plural:** sgclusters

**singular:** sgcluster
___

**Spec**

| Property                                                                                   | Required | Updatable | Type     | Default                             | Description                                                        |
|:-------------------------------------------------------------------------------------------|----------|-----------|:---------|:------------------------------------|:-------------------------------------------------------------------|
| postgresVersion                                                                            | ✓        | ✓         | string   |                                     | {{< crd-field-description SGCluster.spec.postgresVersion >}}       |
| instances                                                                                  | ✓        | ✓         | integer  |                                     | {{< crd-field-description SGCluster.spec.instances >}}             |
| [sgInstanceProfile]({{% relref "/06-crd-reference/02-sginstanceprofile" %}})               |          | ✓         | string   | will be generated                   | {{< crd-field-description SGCluster.spec.sgInstanceProfile >}}     |
| [metadata](#metadata)                                                                      |          | ✓         | object   |                                     | {{< crd-field-description SGCluster.spec.metadata >}}              |
| [postgresServices](#postgres-services)                                                     |          | ✓         | object   |                                     | {{< crd-field-description SGCluster.spec.postgresServices >}}      |
| [pods](#pods)                                                                              | ✓        | ✓         | object   |                                     | {{< crd-field-description SGCluster.spec.pods >}}                  |
| [configurations](#configurations)                                                          |          | ✓         | object   |                                     | {{< crd-field-description SGCluster.spec.configurations >}}        |
| [postgresExtensions](#postgres-extensions)                                                 |          | ✓         | array    |                                     | {{< crd-field-description SGCluster.spec.postgresExtensions >}}    |
| prometheusAutobind                                                                         |          | ✓         | boolean  | false                               | {{< crd-field-description SGCluster.spec.prometheusAutobind >}}    |
| [initialData](#initial-data-configuration)                                                 |          |           | object   |                                     | {{< crd-field-description SGCluster.spec.initialData >}}           |
| [distributedLogs](#distributed-logs)                                                       |          | ✓         | object   |                                     | {{< crd-field-description SGCluster.spec.distributedLogs >}}       |
| [nonProductionOptions](#non-production-options)                                            |          | ✓         | array    |                                     | {{< crd-field-description SGCluster.spec.nonProductionOptions >}}  |

Example:

```yaml
apiVersion: stackgres.io/v1
kind: SGCluster
metadata:
  name: stackgres
spec:
  instances: 1
  postgresVersion: 'latest'
  pods:
    persistentVolume:
      size: '5Gi'
  sgInstanceProfile: 'size-xs'
```

### Metadata

Holds custom metadata information for StackGres generated resources to have.

| Property                      | Required | Updatable | Type     | Default        | Description |
|:------------------------------|----------|-----------|:---------|:---------------|:------------|
| [annotations](#annotations)   |          | ✓         | object   |                | {{< crd-field-description SGCluster.spec.metadata.annotations >}} |

### Annotations

Holds custom annotations for StackGres generated resources to have.

| Property                      | Required | Updatable | Type     | Default        | Description |
|:------------------------------|----------|-----------|:---------|:---------------|:------------|
| allResources                  |          | ✓         | object   |                | {{< crd-field-description SGCluster.spec.metadata.annotations.allResources >}} |
| pods                          |          | ✓         | object   |                | {{< crd-field-description SGCluster.spec.metadata.annotations.pods >}} |
| services                      |          | ✓         | object   |                | {{< crd-field-description SGCluster.spec.metadata.annotations.services >}} |

```yaml
apiVersion: stackgres.io/v1
kind: SGCluster
metadata:
  name: stackgres
spec:
  metadata:
    annotations:
      pods:
        customAnnotations: customAnnotationValue
```

## Postgres Services

Specifies the service configuration for the cluster:

| Property                            | Required | Updatable | Type     | Default                              | Description                                                            |
|:------------------------------------|----------|-----------|:---------|:-------------------------------------|:-----------------------------------------------------------------------|
| [Primary](#primary-service-type)    |          | ✓         | object   | [primary](#primary-service-type)   | {{< crd-field-description SGCluster.spec.postgresServices.primary >}}  |
| [Replicas](#replicas-service-type)  |          | ✓         | object   | [replicas](#replicas-service-type) | {{< crd-field-description SGCluster.spec.postgresServices.replicas >}} |

### Primary service type

| Property                        | Required | Updatable | Type     | Default   | Description                                                                 |
|:--------------------------------|----------|-----------|:---------|:----------|:----------------------------------------------------------------------------|
| enabled                         |          | ✓         | boolean  | true      | {{< crd-field-description SGCluster.spec.postgresServices.primary.enabled >}}  |
| type                            |          | ✓         | string   | ClusterIP | {{< crd-field-description SGCluster.spec.postgresServices.primary.type >}}  |
| annotations                     |          | ✓         | object   |           | {{< crd-field-description SGCluster.spec.postgresServices.primary.annotations >}}  |

### Replicas service type

| Property                        | Required | Updatable | Type     | Default   | Description                                                                 |
|:--------------------------------|----------|-----------|:---------|:----------|:----------------------------------------------------------------------------|
| enabled                         |          | ✓         | boolean  | true      | {{< crd-field-description SGCluster.spec.postgresServices.replicas.enabled >}}  |
| type                            |          | ✓         | string   | ClusterIP | {{< crd-field-description SGCluster.spec.postgresServices.replicas.type >}}  |
| annotations                     |          | ✓         | object   |           | {{< crd-field-description SGCluster.spec.postgresServices.replicas.annotations >}}  |

## Pods

Cluster's pod configuration

| Property                               | Required | Updatable | Type     | Default                             | Description |
|:---------------------------------------|----------|-----------|:---------|:------------------------------------|:------------|
| [persistentVolume](#persistent-volume) | ✓        | ✓         | object   |                                     | {{< crd-field-description SGCluster.spec.pods.persistentVolume >}} |
| disableConnectionPooling               |          | ✓         | boolean  | false                               | {{< crd-field-description SGCluster.spec.pods.disableConnectionPooling >}} |
| disableMetricsExporter                 |          | ✓         | boolean  | false                               | {{< crd-field-description SGCluster.spec.pods.disableMetricsExporter >}} |
| disablePostgresUtil                    |          | ✓         | boolean  | false                               | {{< crd-field-description SGCluster.spec.pods.disablePostgresUtil >}} |
| [metadata](#pods-metadata)             |          | ✓         | object   |                                     | {{< crd-field-description SGCluster.spec.pods.metadata >}} |
| [scheduling](#scheduling)              |          | ✓         | object   |                                     | {{< crd-field-description SGCluster.spec.pods.scheduling >}} |

### Sidecar containers

A sidecar container is a container that adds functionality to PostgreSQL or to the cluster
 infrastructure. Currently StackGres implement following sidecar containers:

* `envoy`: this container is always present, and is not possible to disable it. It serve as
 a edge proxy from client to PostgreSQL instances or between PostgreSQL instances. It enables
 network metrics collection to provide connection statistics.
* `pgbouncer`: a container with pgbouncer as the connection pooling for the PostgreSQL instances.
* `prometheus-postgres-exporter`: a container with postgres exporter that exports metrics for
 the PostgreSQL instances.
* `fluent-bit`: a container with fluent-bit that send logs to a distributed logs cluster.
* `postgres-util`: a container with psql and all PostgreSQL common tools in order to connect to the
 database directly as root to perform any administration tasks.

The following example, disable all optional sidecars:

```yaml
apiVersion: stackgres.io/v1
kind: SGCluster
metadata:
  name: stackgres
spec:
  pods:
    disableConnectionPooling: false
    disableMetricsExporter: false
    disablePostgresUtil: false
```

### Persistent Volume

Holds the configurations of the persistent volume that the cluster pods are going to use.

| Property     | Required | Updatable | Type     | Default                             | Description |
|:-------------|----------|-----------|:---------|:------------------------------------|:------------|
| size         | ✓        | ✓         | string   |                                     | {{< crd-field-description SGCluster.spec.pods.persistentVolume.size >}} |
| storageClass |          | ✓         | string   | default storage class               | {{< crd-field-description SGCluster.spec.pods.persistentVolume.storageClass >}} |

```yaml
apiVersion: stackgres.io/v1
kind: SGCluster
metadata:
  name: stackgres
spec:
  pods:
    persistentVolume:
      size: '5Gi'
      storageClass: default
```

### Pods metadata

Holds custom metadata information for StackGres pods to have.

| Property     | Required | Updatable | Type     | Default          | Description |
|:-------------|----------|-----------|:---------|:-----------------|:------------|
| labels       |          | ✓         | string   |                  | {{< crd-field-description SGCluster.spec.pods.metadata.labels >}} |

```yaml
apiVersion: stackgres.io/v1
kind: SGCluster
metadata:
  name: stackgres
spec:
  pods:
    metadata:
      labels:
        customLabel: customLabelValue
```

### Scheduling

Holds scheduling configuration for StackGres pods to have.

| Property                    | Required | Updatable | Type     | Default        | Description |
|:----------------------------|----------|-----------|:---------|:---------------|:------------|
| nodeSelector                |          | ✓         | object   |                | {{< crd-field-description SGCluster.spec.pods.scheduling.nodeSelector >}} |
| [tolerations](#tolerations) |          | ✓         | array    |                | {{< crd-field-description SGCluster.spec.pods.scheduling.tolerations >}} |

#### Tolerations

Holds scheduling configuration for StackGres pods to have.

| Property          | Required | Updatable | Type     | Default                 | Description |
|:------------------|----------|-----------|:---------|:------------------------|:------------|
| key               |          | ✓         | string   |                         | {{< crd-field-description SGCluster.spec.pods.scheduling.tolerations.items.key >}} |
| operator          |          | ✓         | string   | Equal                   | {{< crd-field-description SGCluster.spec.pods.scheduling.tolerations.items.operator >}} |
| value             |          | ✓         | string   |                         | {{< crd-field-description SGCluster.spec.pods.scheduling.tolerations.items.value >}} |
| effect            |          | ✓         | string   | match all taint effects | {{< crd-field-description SGCluster.spec.pods.scheduling.tolerations.items.effect >}} |
| tolerationSeconds |          | ✓         | string   | 0                       | {{< crd-field-description SGCluster.spec.pods.scheduling.tolerations.items.tolerationSeconds >}} |

## Configurations

Custom configurations to be applied to the cluster.

| Property                                                                                  | Required | Updatable | Type     | Default           | Description |
|:------------------------------------------------------------------------------------------|----------|-----------|:---------|:------------------|:------------|
| [sgPostgresConfig]({{% relref "06-crd-reference/03-sgpostgresconfig" %}})           |          | ✓         | string   | will be generated | {{< crd-field-description SGCluster.spec.configurations.sgPostgresConfig >}} |
| [sgPoolingConfig]({{% relref "06-crd-reference/04-sgpoolingconfig" %}})  |          | ✓         | string   | will be generated | {{< crd-field-description SGCluster.spec.configurations.sgPoolingConfig >}} |
| [sgBackupConfig]({{% relref "06-crd-reference/05-sgbackupconfig" %}})                     |          | ✓         | string   |                   | {{< crd-field-description SGCluster.spec.configurations.sgBackupConfig >}} |

Example:

``` yaml

apiVersion: stackgres.io/v1
kind: SGCluster
metadata:
  name: stackgres
spec:
  configurations:
    sgPostgresConfig: 'postgresconf'
    sgPoolingConfig: 'pgbouncerconf'
    sgBackupConfig: 'backupconf'

```

## Postgres extensions

Extensions to be installed in the cluster.

| Property         | Required | Updatable | Type     | Default           | Description |
|:-----------------|----------|-----------|:---------|:------------------|:------------|
| name             | ✓        | ✓         | string   |                   | {{< crd-field-description SGCluster.spec.postgresExtensions.items.name >}} |
| version          |          | ✓         | string   | stable            | {{< crd-field-description SGCluster.spec.postgresExtensions.items.version >}} |
| publisher        |          | ✓         | string   | com.ongres        | {{< crd-field-description SGCluster.spec.postgresExtensions.items.publisher >}} |
| repository       |          | ✓         | string   |                   | {{< crd-field-description SGCluster.spec.postgresExtensions.items.repository >}} |

Example:

``` yaml

apiVersion: stackgres.io/v1
kind: SGCluster
metadata:
  name: stackgres
spec:
  postgresExtensions:
  - name: 'timescaledb'
```

## Initial Data Configuration
Specifies the cluster initialization data configurations

| Property                          | Required | Updatable | Type     | Default | Description |
|:----------------------------------|----------|-----------|:---------|:--------|:------------|
| [restore](#restore-configuration) |          |           | object   |         | {{< crd-field-description SGCluster.spec.initialData.restore >}} |
| [scripts](#scripts-configuration) |          |           | object   |         | {{< crd-field-description SGCluster.spec.initialData.scripts >}} |

## Restore configuration

By default, stackgres it's creates as an empty database. To create a cluster with data
 from an existent backup, we have the restore options. It works, by simply indicating the
 backup CR UUI that we want to restore.

| Property                                 | Required | Updatable | Type     | Default | Description |
|:-----------------------------------------|----------|-----------|:---------|:--------|:------------|
| [fromBackup](#from-backup-configuration) | ✓        |           | object   |         | {{< crd-field-description SGCluster.spec.initialData.restore.fromBackup >}} |
| downloadDiskConcurrency                  |          |           | integer  | 1       | {{< crd-field-description SGCluster.spec.initialData.restore.downloadDiskConcurrency >}} |

### From backup configuration

| Property                                   | Required | Updatable | Type     | Default | Description |
|:-------------------------------------------|----------|-----------|:---------|:--------|:------------|
| uid                                        | ✓        |           | string   |         | {{< crd-field-description SGCluster.spec.initialData.restore.fromBackup.uid >}} |
| [pointInTimeRecovery](#pitr-configuration) |          |           | object   |         | {{< crd-field-description SGCluster.spec.initialData.restore.fromBackup.pointInTimeRecovery >}} |

### PITR configuration

| Property           | Required | Updatable | Type     | Default | Description |
|:-------------------|----------|-----------|:---------|:--------|:------------|
| restoreToTimestamp |          |           | string   |         | {{< crd-field-description SGCluster.spec.initialData.restore.fromBackup.pointInTimeRecovery.restoreToTimestamp >}} |

Example:

```yaml
apiVersion: stackgres.io/v1
kind: SGCluster
metadata:
  name: stackgres
spec:
  initialData:
    restore:
      fromBackup:
        uid: d7e660a9-377c-11ea-b04b-0242ac110004
      downloadDiskConcurrency: 1
```

## Scripts configuration

By default, stackgres creates as an empty database. To execute some scripts, we have the scripts
 options where you can specify a script or reference a key in a ConfigMap or a Secret that contains
 the script to execute.

| Property                   | Required | Updatable | Type     | Default  | Description |
|:---------------------------|----------|-----------|:---------|:---------|:------------|
| name                       |          |           | string   |          | {{< crd-field-description SGCluster.spec.initialData.scripts.items.name >}} |
| database                   |          |           | string   | postgres | {{< crd-field-description SGCluster.spec.initialData.scripts.items.database >}} |
| script                     |          |           | string   |          | {{< crd-field-description SGCluster.spec.initialData.scripts.items.script >}} |
| [scriptFrom](#script-from) |          |           | object   |          | {{< crd-field-description SGCluster.spec.initialData.scripts.items.scriptFrom >}} |

Example:

```yaml
apiVersion: stackgres.io/v1
kind: SGCluster
metadata:
  name: stackgres
spec:
  initialData:
    scripts:
    - name: create-stackgres-user
      scriptFrom:
        secretKeyRef: # read the user from a Secret to maintain credentials in a safe place
          name: stackgres-secret-sqls-scripts
          key: create-stackgres-user.sql
    - name: create-stackgres-database
      script: |
        CREATE DATABASE stackgres WITH OWNER stackgres;
    - name: create-stackgres-schema
      database: stackgres
      scriptFrom:
        configMapKeyRef: # read long script from a ConfigMap to avoid have to much data in the helm releasea and the sgcluster CR
          name: stackgres-sqls-scripts
          key: create-stackgres-schema.sql
```

### Script from

| Property                                  | Required | Updatable | Type     | Default  | Description |
|:------------------------------------------|----------|-----------|:---------|:---------|:------------|
| [configMapKeyRef](#script-from-configmap) |          |           | object   |          | {{< crd-field-description SGCluster.spec.initialData.scripts.items.scriptFrom.configMapKeyRef >}} |
| [secretKeyRef](#script-from-configmap)    |          |           | object   |          | {{< crd-field-description SGCluster.spec.initialData.scripts.items.scriptFrom.secretKeyRef >}} |

#### Script from ConfigMap

| Property  | Required | Updatable | Type     | Default  | Description |
|:----------|----------|-----------|:---------|:---------|:------------|
| name      |          |           | string   |          | {{< crd-field-description SGCluster.spec.initialData.scripts.items.scriptFrom.configMapKeyRef.name >}} |
| key       |          |           | string   |          | {{< crd-field-description SGCluster.spec.initialData.scripts.items.scriptFrom.configMapKeyRef.key >}} |

#### Script from Secret

| Property  | Required | Updatable | Type     | Default  | Description |
|:----------|----------|-----------|:---------|:---------|:------------|
| name      |          |           | string   |          | {{< crd-field-description SGCluster.spec.initialData.scripts.items.scriptFrom.secretKeyRef.name >}} |
| key       |          |           | string   |          | {{< crd-field-description SGCluster.spec.initialData.scripts.items.scriptFrom.secretKeyRef.key >}} |

## Distributed logs
Specifies the distributed logs cluster to send logs to:

| Property                                                                     | Required | Updatable | Type     | Default | Description |
|:-----------------------------------------------------------------------------|----------|-----------|:---------|:--------|:------------|
| [sgDistributedLogs]({{% relref "/06-crd-reference/07-sgdistributedlogs" %}})  |          |           | string   |         | {{< crd-field-description SGCluster.spec.distributedLogs.sgDistributedLogs >}} |

Example:

```yaml
apiVersion: stackgres.io/v1
kind: SGCluster
metadata:
  name: stackgres
spec:
  distributedLogs:
    sgDistributedLogs: distributedlogs
```

## Non Production options

The following options should NOT be enabled in a production environment.

| Property                      | Required | Updatable | Type     | Default | Description |
|:------------------------------|----------|-----------|:---------|:--------|:------------|
| disableClusterPodAntiAffinity |          | ✓         | boolean  | false   | {{< crd-field-description SGCluster.spec.nonProductionOptions.disableClusterPodAntiAffinity >}} |
