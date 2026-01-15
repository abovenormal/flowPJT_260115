#!/bin/bash
echo "Starting extension-app..."

# JAR 파일을 홈 디렉토리로 복사
cp /home/ec2-user/app/build/libs/*.jar /home/ec2-user/extensionCheck-0.0.1-SNAPSHOT.jar

# 서비스 시작
sudo systemctl start extension-app
echo "Application started."
