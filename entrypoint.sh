#!/bin/sh

# S3에서 Fluent Bit 설정 파일 다운로드
echo "Downloading Fluent Bit configuration from S3..."
aws s3 cp s3://deepdive2team.shop/fluent-bit.conf /fluent-bit/etc/fluent-bit.conf

if [ $? -ne 0 ]; then
  echo "Failed to download Fluent Bit configuration file. Exiting..."
  exit 1
fi

echo "Successfully downloaded Fluent Bit configuration file."

# Fluent Bit 실행 백그라운드로 실행
echo "Starting Fluent Bit..."
/fluent-bit/bin/fluent-bit -c /fluent-bit/etc/fluent-bit.conf &

# 애플리케이션 실행
echo "Starting application..."
exec java -jar /app/app.jar