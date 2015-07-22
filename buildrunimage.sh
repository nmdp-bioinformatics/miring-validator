docker rm $(docker kill $(docker ps -aq))

docker rmi $(docker images -qf "dangling=true")

docker build -t miringvalidator .

docker run -it miringvalidator bash