# Self-Driving Car Sim with Edge Servers

## Overview

## Build Process
* clone the repository into a local directory
```
  git clone https://github.com/Jayliu227/EdgeExperiment.git
```

* start minikube and docker
```
  minikube start
```

* navigate to EdgeServer folder and build docker images
```
  cd EdgeServer/ 
  bash build.sh
```
This would first remove any docker images with the same name and rebuild them using the dockerfile provided in the same folder. Then it will make sure minikube can access docker local registry.
Any time when you make changes to the code, this needs to be done again.

* start deployment in kubernetes
```
  cd ../LaunchTools/
  python3 launch.py boot <number_of_edge_servers>
  python3 launch.py createsvc
```
This will create exactly one backend server and whatever number of edge servers you passed in. You need to create service after creating those pods.

* port forwarding
Since minikube is run inside a virtual machine, we need to manually forward the service port.
```
  kubectl get svc
```
This will show the external port exposed to the outside, and we need to go into VirtualBox (if you are using other hypervisor, do the same thing) and set network forwarding correctly.

* figure out minikube's ip
```
  minikube ip
```
We need to update the ip address in EdgyVisualization Unity project's EdgeClient.cs

* use launch.py
'launch.py' has some helper functionalities to help manage the current state of the cluster and debug each pod.
