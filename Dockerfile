# 1단계: Build Stage
FROM openjdk:17-jdk-bullseye AS builder

# 필수 패키지 설치
RUN apt-get update && apt-get install -y findutils

# 작업 디렉토리 설정
WORKDIR /app

# 애플리케이션 파일 복사
COPY . .

# Gradle 빌드 실행 (JAR 파일 생성)
RUN ./gradlew clean build -x test

# 2단계: Run Stage
FROM openjdk:17-jdk-slim

# 필수 패키지 설치 (AWS CLI 및 jq 설치)
RUN apt-get update && apt-get install -y awscli jq bash

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# Fluent Bit 실행에 필요한 entrypoint.sh 스크립트 복사
COPY entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

# Fluent Bit 설정 파일 디렉토리 생성
RUN mkdir -p /fluent-bit/etc

# 엔트리포인트 설정
ENTRYPOINT ["/app/entrypoint.sh"]


# 컨테이너 포트 노출
EXPOSE 8080

