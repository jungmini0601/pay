# 베이스 이미지 openjdk17
FROM openjdk:17 AS builder
COPY build/libs/*.jar pay.jar
# 80 컨테이너 포트 노출
EXPOSE 80
# jar 파일 실행
ENTRYPOINT ["java","-jar","/pay.jar"]
