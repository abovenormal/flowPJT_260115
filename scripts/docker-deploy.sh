#!/bin/bash
APP_DIR=/home/ec2-user/app

cd $APP_DIR

docker-compose down
docker-compose pull
docker-compose up -d

docker image prune -f
