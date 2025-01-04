#!/bin/sh

# CONFIG_FILE 환경 변수 내용을 설정 파일로 저장
echo "$CONFIG_FILE" > /fluent-bit/etc/fluent-bit.conf

# Fluent Bit 실행 백그라운드로 실행
/fluent-bit/bin/fluent-bit -c /fluent-bit/etc/fluent-bit.conf &

# 애플리케이션 실행
exec java -jar /app/app.jar