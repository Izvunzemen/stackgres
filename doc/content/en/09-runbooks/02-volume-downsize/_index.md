---
title: Volume downsize
weight: 2
url: runbooks/volume-downsize
description: Steps about how to perform a volume downsize
---

This runbook will show you how to perform a volume downsize. The normal operation is to extend a volume but in some cases you probably over-dimension your volumes and you might need to reduce cost and resource usage so you'll need to downsize your resources, in this case, your volumes.


## Scenario

Having a StackGres cluster with:
  - Instances: `3`
  - Namespace: `ongres-db`
  - Cluster name: `ongres-db`
  - Volume size: `20Gi`

```
$ kubectl exec -it -n ongres-db ongres-db-2 -c patroni -- patronictl list

+ Cluster: ongres-db (6918002883456245883) -------+----+-----------+
|    Member   |      Host      |  Role  |  State  | TL | Lag in MB |
+-------------+----------------+--------+---------+----+-----------+
| ongres-db-0 | 10.0.7.11:7433 | Leader | running |  3 |           |
| ongres-db-1 | 10.0.0.10:7433 |        | running |  3 |         0 |
| ongres-db-2 | 10.0.6.9:7433  |        | running |  3 |         0 |
+-------------+----------------+--------+---------+----+-----------+
```

Verifying the PVC's:

```
$ kubectl get pvc -n ongres-db

NAME                                     STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
distributedlogs-data-distributedlogs-0   Bound    pvc-9bab7a68-a209-4d9a-93f7-871a217a28b1   50Gi       RWO            standard       162m
ongres-db-data-ongres-db-0               Bound    pvc-a2aa5198-c553-4e0d-a1e1-914669abb69f   20Gi       RWO            gp2-data       11m
ongres-db-data-ongres-db-1               Bound    pvc-c724b2bf-cf17-4f57-a882-3a5da6947f44   20Gi       RWO            gp2-data       10m
ongres-db-data-ongres-db-2               Bound    pvc-5124b9d2-ec35-46d7-9eda-7543d9ed7148   20Gi       RWO            gp2-data       4m47s
```

Assuming the disk size is over-dimensioned, and you need to perform a downsize to `15Gi`.

## Perform a switchover to the pod with higher index number ( ongres-db-2)

Execute:

`kubectl exec -it -n ongres-db ongres-db-0 -c patroni -- patronictl switchover`

```
Master [ongres-db-0]:

Candidate ['ongres-db-1', 'ongres-db-2'] []: ongres-db-2

When should the switchover take place (e.g. 2021-01-15T16:40 )  [now]:

Current cluster topology
+ Cluster: ongres-db (6918002883456245883) -------+----+-----------+
|    Member   |      Host      |  Role  |  State  | TL | Lag in MB |
+-------------+----------------+--------+---------+----+-----------+
| ongres-db-0 | 10.0.7.11:7433 | Leader | running |  3 |           |
| ongres-db-1 | 10.0.0.10:7433 |        | running |  3 |         0 |
| ongres-db-2 | 10.0.6.9:7433  |        | running |  3 |         0 |
+-------------+----------------+--------+---------+----+-----------+
Are you sure you want to switchover cluster ongres-db, demoting current master ongres-db-0? [y/N]:y


2021-01-15 15:41:11.93457 Successfully switched over to "ongres-db-2"
+ Cluster: ongres-db (6918002883456245883) -------+----+-----------+
|    Member   |      Host      |  Role  |  State  | TL | Lag in MB |
+-------------+----------------+--------+---------+----+-----------+
| ongres-db-0 | 10.0.7.11:7433 |        | stopped |    |   unknown |
| ongres-db-1 | 10.0.0.10:7433 |        | running |  3 |         0 |
| ongres-db-2 | 10.0.6.9:7433  | Leader | running |  3 |           |
+-------------+----------------+--------+---------+----+-----------+
```

Now check the cluster state:

```
$ kubectl exec -it -n ongres-db ongres-db-2 -c patroni -- patronictl list
+ Cluster: ongres-db (6918002883456245883) -------+----+-----------+
|    Member   |      Host      |  Role  |  State  | TL | Lag in MB |
+-------------+----------------+--------+---------+----+-----------+
| ongres-db-0 | 10.0.7.11:7433 |        | running |  4 |         0 |
| ongres-db-1 | 10.0.0.10:7433 |        | running |  4 |         0 |
| ongres-db-2 | 10.0.6.9:7433  | Leader | running |  4 |           |
+-------------+----------------+--------+---------+----+-----------+
```

## Edit the SGCluster definition with the new size

Update the volume size to with the new value:

```
kubectl patch sgclusters -n ongres-db ongres-db --type='json' -p '[{ "op": "replace", "path": "/spec/pods/persistentVolume/size", "value": "10Gi" }]'
```

You'll get the next message:

```
sgcluster.stackgres.io/ongres-db patched
```

Now if you check the events you will see an error like:

```
kubectl get events -n ongres-db

....
Failure executing: PATCH at: https://10.96.0.1/apis/apps/v1/namespaces/ongres-db/statefulsets/ongres-db. Message: StatefulSet.apps "ongres-db" is invalid: spec: Forbidden: updates to statefulset spec for fields other than 'replicas', 'template', and 'updateStrategy' are forbidden. Received status: Status(apiVersion=v1, code=422, details=StatusDetails(causes=[StatusCause(field=spec, message=Forbidden: updates to statefulset spec for fields other than 'replicas', 'template', and 'updateStrategy' are forbidden, reason=FieldValueForbidden, additionalProperties={})], group=apps, kind=StatefulSet, name=ongres-db, retryAfterSeconds=null, uid=null, additionalProperties={}), kind=Status, message=StatefulSet.apps "ongres-db" is invalid: spec: Forbidden: updates to statefulset spec for fields other than 'replicas', 'template', and 'updateStrategy' are forbidden, metadata=ListMeta(_continue=null, remainingItemCount=null, resourceVersion=null, selfLink=null, additionalProperties={}), reason=Invalid, status=Failure, additionalProperties={}).
....
```

This is expected because is forbidden to change the size value into a StatefulSet, so proceed to delete the statefulset and let the StackGres Operator recreate it.

```
$ kubectl delete sts -n ongres-db ongres-db --cascade=orphan
```

> Very important note: Do not forget the paramater `--cascade=orphan` because this will keep the existent pods.

## Verify the StatefulSet has the new volume size:

```
$ kubectl describe sts -n ongres-db ongres-db | grep -i capacity
  Capacity:      15Gi
```

## Edit the replica size to **1** (One)

```
$ kubectl patch sgclusters -n ongres-db ongres-db --type='json' -p '[{ "op": "replace", "path": "/spec/instances", "value": 1 }]'
```

Once you decrease the replicas you'll see something like:

```
$ kubectl get pods -n ongres-db
NAME                READY   STATUS    RESTARTS   AGE
distributedlogs-0   2/2     Running   0          3h4m
ongres-db-2         6/6     Running   0          27m
```

## Delete the non use PVCs and PVs

Proceed to delete the PVCs `ongres-db-data-ongres-db-0` and `ongres-db-data-ongres-db-1`:

```
$ kubectl delete pvc -n ongres-db ongres-db-data-ongres-db-0
persistentvolumeclaim "ongres-db-data-ongres-db-0" deleted

$ kubectl delete pvc -n ongres-db ongres-db-data-ongres-db-1
persistentvolumeclaim "ongres-db-data-ongres-db-1" deleted
```

This will release the PV and then you can proceed to delete them:

```
$ kubectl get pv
NAME                                       CAPACITY   ACCESS MODES   RECLAIM POLICY   STATUS     CLAIM                                              STORAGECLASS   REASON   AGE
pvc-5124b9d2-ec35-46d7-9eda-7543d9ed7148   20Gi       RWO            Retain           Bound      ongres-db/ongres-db-data-ongres-db-2               gp2-data                32m
pvc-9bab7a68-a209-4d9a-93f7-871a217a28b1   50Gi       RWO            Delete           Bound      ongres-db/distributedlogs-data-distributedlogs-0   standard                3h10m
pvc-a2aa5198-c553-4e0d-a1e1-914669abb69f   20Gi       RWO            Retain           Released   ongres-db/ongres-db-data-ongres-db-0               gp2-data                39m
pvc-c724b2bf-cf17-4f57-a882-3a5da6947f44   20Gi       RWO            Retain           Released   ongres-db/ongres-db-data-ongres-db-1               gp2-data                38m
```

Delete the disks with `Released` state:

```
$ kubectl delete pv pvc-a2aa5198-c553-4e0d-a1e1-914669abb69f

persistentvolume "pvc-a2aa5198-c553-4e0d-a1e1-914669abb69f" deleted
```

and

```
$ kubectl delete pv pvc-c724b2bf-cf17-4f57-a882-3a5da6947f44

persistentvolume "pvc-c724b2bf-cf17-4f57-a882-3a5da6947f44" deleted
```

## Increase the replica size to **2** (Two):

```
$ kubectl patch sgclusters -n ongres-db ongres-db --type='json' -p '[{ "op": "replace", "path": "/spec/instances", "value": 2 }]'
```

Now you cluster will have 2 pods:

```
$ kubectl get pods -n ongres-db
NAME                READY   STATUS    RESTARTS   AGE
distributedlogs-0   2/2     Running   0          3h15m
ongres-db-0         6/6     Running   0          49s
ongres-db-2         6/6     Running   0          37m

```

Check again the cluster state:

```
$ kubectl exec -it -n ongres-db ongres-db-2 -c patroni -- patronictl list
+ Cluster: ongres-db (6918002883456245883) -------+----+-----------+
|    Member   |      Host      |  Role  |  State  | TL | Lag in MB |
+-------------+----------------+--------+---------+----+-----------+
| ongres-db-0 | 10.0.7.12:7433 |        | running |  4 |         0 |
| ongres-db-2 | 10.0.6.9:7433  | Leader | running |  4 |           |
+-------------+----------------+--------+---------+----+-----------+
```

And the new pod will have the new disk size:

```
$ kubectl get pvc -n ongres-db
NAME                                     STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
distributedlogs-data-distributedlogs-0   Bound    pvc-9bab7a68-a209-4d9a-93f7-871a217a28b1   50Gi       RWO            standard       3h17m
ongres-db-data-ongres-db-0               Bound    pvc-37d96872-b132-4a89-a579-d87f8cf1fa92   15Gi       RWO            gp2-data       2m47s
ongres-db-data-ongres-db-2               Bound    pvc-5124b9d2-ec35-46d7-9eda-7543d9ed7148   20Gi       RWO            gp2-data       39m
```

## Perform a switchover to node **ongres-db-0**

```
$ kubectl exec -it -n ongres-db ongres-db-2 -c patroni -- patronictl switchover
Master [ongres-db-2]:
Candidate ['ongres-db-0'] []: ongres-db-0
When should the switchover take place (e.g. 2021-01-15T17:12 )  [now]:
Current cluster topology
+ Cluster: ongres-db (6918002883456245883) -------+----+-----------+
|    Member   |      Host      |  Role  |  State  | TL | Lag in MB |
+-------------+----------------+--------+---------+----+-----------+
| ongres-db-0 | 10.0.7.12:7433 |        | running |  4 |         0 |
| ongres-db-2 | 10.0.6.9:7433  | Leader | running |  4 |           |
+-------------+----------------+--------+---------+----+-----------+
Are you sure you want to switchover cluster ongres-db, demoting current master ongres-db-2? [y/N]: y
2021-01-15 16:12:57.14561 Successfully switched over to "ongres-db-0"
+ Cluster: ongres-db (6918002883456245883) -------+----+-----------+
|    Member   |      Host      |  Role  |  State  | TL | Lag in MB |
+-------------+----------------+--------+---------+----+-----------+
| ongres-db-0 | 10.0.7.12:7433 | Leader | running |  4 |           |
| ongres-db-2 | 10.0.6.9:7433  |        | stopped |    |   unknown |
+-------------+----------------+--------+---------+----+-----------+
```

This will delete the pod `ongres-db-2` and create the pod `ongres-db-1`


```
NAME                READY   STATUS    RESTARTS   AGE
distributedlogs-0   2/2     Running   0          3h19m
ongres-db-0         6/6     Running   0          4m51s
ongres-db-1         6/6     Running   0          41s
```

You can proceed to delete the PVC and PV of `ongres-db-2`

```
$ kubectl delete pvc -n ongres-db ongres-db-data-ongres-db-2
persistentvolumeclaim "ongres-db-data-ongres-db-2" deleted
```

and

```
$ kubectl delete pv pvc-5124b9d2-ec35-46d7-9eda-7543d9ed7148
persistentvolume "pvc-5124b9d2-ec35-46d7-9eda-7543d9ed7148" deleted
```

Now your cluster will have the new reduce disk size:

```
$ kubectl get pvc -n ongres-db
NAME                                     STATUS   VOLUME                                     CAPACITY   ACCESS MODES   STORAGECLASS   AGE
distributedlogs-data-distributedlogs-0   Bound    pvc-9bab7a68-a209-4d9a-93f7-871a217a28b1   50Gi       RWO            standard       3h24m
ongres-db-data-ongres-db-0               Bound    pvc-37d96872-b132-4a89-a579-d87f8cf1fa92   15Gi       RWO            gp2-data       9m21s
ongres-db-data-ongres-db-1               Bound    pvc-46c1433b-26e8-422c-aecf-145b1bb5aac1   15Gi       RWO            gp2-data       5m11s
```