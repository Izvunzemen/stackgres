---
title: Building StackGres
weight: 2
url: developer/stackgres/build
---

To build stackgres run the following command inside folder `stackgres-k8s/src`:

```
./mvnw clean install
```

## Build with checks

Build with strength checks is needed in order to contribute to the project (since the CI will run those checks).
 To do so simply add the `safer` profile:

```
./mvnw clean install -P safer
```
