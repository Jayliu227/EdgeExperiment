# remove any previous build
docker rmi edge-image:v1
docker rmi backend-image:v1

# build
docker build -f Dockerfile.edge -t edge-image:v1 .
docker build -f Dockerfile.backend -t backend-image:v1 .

# show all images
docker images
