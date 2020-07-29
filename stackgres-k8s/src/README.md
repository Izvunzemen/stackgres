# StackGres Operator

The StackGres Operator is build in pure-Java and uses the [Quarkus](https://quarkus.io/) framework a Kubernetes
Native Java stack tailored for GraalVM & OpenJDK HotSpot, crafted from the best of breed Java
libraries and standards.

## Building

To build the operator you need to have GraalVM (or any Java SDK 8+ if you do not need to build the native operator)
installed locally or directly use a container to bootstrap the compile phase.
The native-image generation has been tested on Linux only but should also work on macOS.

### Building using a container

#### Prerequisites

- docker

#### Compiling and running

The build process is bootstraped in a maven profile, to run the build:

```
./mvnw clean package -P build-image-jvm
```

The image is loaded in local docker registry. You will have to upload the generated image to the registry used
by kubernetes. Then to deploy the operator run from the project roor folder:

```
helm install stackgres-cluster --namespace stackgres stackgres-k8s/install/helm/stackgres-cluster
```

### Building locally

#### Prerequisites

The prerequisites are the same for any Quarkus-based application.

- JDK 1.8+ installed with `JAVA_HOME` configured appropriately.
- GraalVM installed from the GraalVM web site. Using the community edition is enough.
- The `GRAALVM_HOME` environment variable configured appropriately.
- The `native-image` tool must be installed; this can be done by running `gu install native-image` from your GraalVM directory.
- A working C developer environment.

#### Compiling and running

To create the native executable you can use

```
./mvnw package -P native,build-image-native
```

The image is loaded in local docker registry. You will have to upload the generated image to the registry used
by kubernetes. Then to deploy the operator run from the project roor folder:

```
helm install stackgres-cluster --namespace stackgres stackgres-k8s/install/helm/stackgres-cluster
```

#### Integration tests

Integration tests requires docker to be installed (if not on Linux set the environment variable `DOCKER_HOST` pointing to the protocol, host and port of the docker daemon). To run the ITs:

```
./mvnw verify -P integration
```

---

```
   _____ _             _     _____
  / ____| |           | |   / ____|
 | (___ | |_ __ _  ___| | _| |  __ _ __ ___  ___
  \___ \| __/ _` |/ __| |/ / | |_ | '__/ _ \/ __|
  ____) | || (_| | (__|   <| |__| | | |  __/\__ \
 |_____/ \__\__,_|\___|_|\_\\_____|_|  \___||___/
                                  by OnGres, Inc.

```
