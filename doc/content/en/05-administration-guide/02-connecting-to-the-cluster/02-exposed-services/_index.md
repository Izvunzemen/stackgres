---
title: Exposing Services
weight: 2
url: administration/cluster/connection/exposed
description: |
    Describes how to connect on the cluster exposing its services on the internet.
---

To allow access outside the k8s cluster is necessary to update the [services that exposes access to the StackGres cluster]({{% relref "/05-administration-guide/02-connecting-to-the-cluster/01-dns/" %}}) changing it to `NodePort` or `LoadBalancer`.

All examples on this pages are assuming that there is a cluster named `my-db-cluster` on the `default` namespace.

## Portforward access to the SGCluster

The easiest way to use `kubectl` to port-forward the postgres port on the SGCluster:


```bash
## get the service name for the primary database on my-dbcluster
kubectl get services -o name -l app=StackGresCluster,cluster-name=my-db-cluster,role=master
# service/my-db-cluster-primary

kubectl port-forward service/my-db-cluster-primary --address 0.0.0.0 5432:5432
```

On another session run:

```bash
psql -h localhost -U postgres
```

## Updating the service configuration

By default, SGCluster services type are `ClusterIP` which means that the SGCluster will not be opened outside the k8s cluster. To change that behavior, is necessary to update the cluster, changing the service configuration.

### Connecting through NodePort

NodePort is a k8s mechanism to expose a service into a dynamic in each cluster nodes. Update the SGCluster configuration like below:

```yaml
---
apiVersion: stackgres.io/v1
kind: SGCluster
metadata:
  name: my-db-cluster
  namespace: default
spec:
  ## ...
  postgresServices:
    primary:
      type: NodePort
    replicas:
      type: NodePort
```
> Check the [SGCluster reference]({{% relref "06-crd-reference/01-postgres-clusters" %}}) for more details about the cluster configuration.

Once applied, the service configuration is updated to `NodePort`:

```bash
kubectl get services -l cluster=true,cluster-name=my-db-cluster
# NAME                     TYPE       CLUSTER-IP       EXTERNAL-IP   PORT(S)                         AGE
# my-db-cluster-primary    NodePort   10.101.139.224   <none>        5432:31884/TCP,5433:31998/TCP   35m
# my-db-cluster-replicas   NodePort   10.99.44.2       <none>        5432:32106/TCP,5433:31851/TCP   35m

```

Get the node ip address (kind ip address on the example below):

```bash
kubectl get nodes -o wide
# NAME                 STATUS   ROLES    AGE    VERSION    INTERNAL-IP   EXTERNAL-IP   OS-IMAGE                                     KERNEL-VERSION     CONTAINER-RUNTIME
# kind-control-plane   Ready    master   115s   v1.17.11   172.18.0.3    <none>        Ubuntu Groovy Gorilla (development branch)   5.8.0-36-generic   containerd://1.4.0
# kind-worker          Ready    <none>   79s    v1.17.11   172.18.0.4    <none>        Ubuntu Groovy Gorilla (development branch)   5.8.0-36-generic   containerd://1.4.0
# kind-worker2         Ready    <none>   79s    v1.17.11   172.18.0.7    <none>        Ubuntu Groovy Gorilla (development branch)   5.8.0-36-generic   containerd://1.4.0
# kind-worker3         Ready    <none>   79s    v1.17.11   172.18.0.5    <none>        Ubuntu Groovy Gorilla (development branch)   5.8.0-36-generic   containerd://1.4.0
# kind-worker4         Ready    <none>   79s    v1.17.11   172.18.0.2    <none>        Ubuntu Groovy Gorilla (development branch)   5.8.0-36-generic   containerd://1.4.0
# kind-worker5         Ready    <none>   85s    v1.17.11   172.18.0.6    <none>        Ubuntu Groovy Gorilla (development branch)   5.8.0-36-generic   containerd://1.4.0
```

Connect on the cluster using `psql` with the `INTERNAL IP` of any node (172.18.0.2 per example) and the service port (`31884` will point to `my-db-cluster-primary` on port `5432`):

```bash
psql -h 172.18.0.2 -U postgres -p 31884
```

### Connecting through a LoadBalancer

LoadBalancer is another option to expose cluster access to outside the k8s cluster. This option needs an extra configuration on the k8s cluster to install and configure an Ingress Controller that will route the connections to the target service. 

The below example is implemented with [kind](https://kind.sigs.k8s.io/) and it uses [MetalLB](https://metallb.universe.tf/) under the hood. For non-premise environments, check your cloud vendor's documentation about the Load Balancer implementation details.

Update the SGCluster configuration like below:

```yaml
---
apiVersion: stackgres.io/v1
kind: SGCluster
metadata:
  name: my-db-cluster
  namespace: default
spec:
  ## ...
  postgresServices:
    primary:
      type: LoadBalancer
    replicas:
      type: LoadBalancer
```
> Check the [SGCluster reference]({{% relref "06-crd-reference/01-postgres-clusters" %}}) for more details about the cluster configuration.

Once updated, get the service information:

```bash
kubectl get services -l cluster=true,cluster-name=my-db-cluster
# NAME                     TYPE           CLUSTER-IP      EXTERNAL-IP    PORT(S)                         AGE
# my-db-cluster-primary    LoadBalancer   10.108.32.129   172.18.0.102   5432:30219/TCP,5433:30886/TCP   8m13s
# my-db-cluster-replicas   LoadBalancer   10.111.30.87    172.18.0.101   5432:31146/TCP,5433:32063/TCP   8m13s
```
> Please note that, since we change both services to `LoadBalancer`, two loadbalancers were created, one for each service. 
> Be aware that additional charges may apply on your cloud provider.

To connect on the database, just use the `EXTERNAL-IP`, like below:

```bash
psql -h 172.18.0.102 -U postgres
```