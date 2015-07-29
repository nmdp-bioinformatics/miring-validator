docker rm $(docker kill $(docker ps -aq))

docker rmi $(docker images -qf "dangling=true")

docker build -t miringvalidator .

docker run -it -p 8080 miringvalidator bash