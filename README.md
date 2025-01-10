# ⏰ 대규모 트래픽 대응을 위한 클라우드 네이티브 타임딜 서비스

## 📋 프로젝트 소개 
안녕하세요, 강력 2 팀입니다! 타임딜 서비스는 고객이 특정 시간에 상품을 구매할 때 발생하는 트래픽을 **AWS 기반 클라우드 서비스**를 활용해 관리합니다.
오토스케일링으로 **높은 가용성과 성능**을 보장해주며, 좋은 사용자 경험을 제공하기 위해 개발 및 설계되었습니다.

---

## 🎯 핵심 목표
1. **대규모 트래픽 대응**:
   - 타임딜 시작 시 발생하는 순간적인 사용자 요청(수천~만 TPS)을 처리하기 위한 확장 가능한 클라우드 아키텍처 설계.
2. **클라우드 네이티브 구현**:
   - **AWS ECS Fargate**, **Elasticache (Redis)**, **RDS**를 활용해 서버리스 아키텍처 구현.
3. **오토스케일링 지원**:
   - 트래픽 변화에 따라 자동으로 리소스를 조정하여 비용 효율성과 성능을 극대화.
4. **안정적인 메시징 시스템**:
   - **SQS**, **SNS**, **Lambda**를 활용하여 구매 처리 및 알림 전송 안정성 확보.
5. **CI/CD 파이프라인 구축**:
   - **GitHub Actions**와 **ECR**을 연계한 자동화된 배포 환경 구성.

---

## 📦 주요 기능
1. **타임딜 상품 관리**
   - 상품 등록, 수정, 삭제 기능 제공.
2. **실시간 재고 관리**
   - Redis를 이용한 캐싱을 통해 빠른 응답 속도 보장.
3. **구매 요청 처리**
   - SQS와 Lambda를 이용한 비동기 구매 요청 처리.
4. **알림 서비스**
   - SNS와 Discord, Slack 연동을 통한 실시간 알림 전송.
5. **사용자 분석 및 모니터링**
   - **CloudWatch**와 백업 서비스(S3)를 활용한 시스템 상태 분석 및 데이터 백업.

---

## 🛠️ 아키텍처 구성
아래는 본 프로젝트의 AWS 기반 클라우드 아키텍처 구성도입니다:

![타임딜 아키텍처 다이어그램](https://github.com/user-attachments/assets/c5d09b8d-cfd0-482d-87e0-f4b9974e3fee)

---

## 🌐 사용 기술
### 클라우드 서비스
- **AWS ECS Fargate**: 서버리스 컨테이너 오케스트레이션
- **Elasticache (Redis)**: 실시간 캐싱 및 세션 관리
- **RDS (MySQL)**: 관계형 데이터베이스 관리
- **S3**: 데이터 백업 및 정적 파일 저장
- **SQS/SNS**: 비동기 메시징 및 알림
- **CloudFront**: 전역 CDN 서비스


---

## 🗃️ 데이터베이스 스키마
아래는 본 프로젝트에서 사용된 데이터베이스 스키마 설계도입니다:

<img width="800" alt="스크린샷 2025-01-09 오전 3 13 15" src="https://github.com/user-attachments/assets/55828a2c-7e3c-486e-91ed-be463861ffde" />



### CI/CD 파이프라인
- **GitHub Actions**: 자동화된 테스트 및 배포
- **ECR**: Docker 컨테이너 이미지 저장소

---

## 👨‍💻 팀원
- **고현정** **김현민** **김홍집** **박경묵** **홍구**
---

## 📈 기대 효과
- **확장성**: 수만 TPS 트래픽을 처리할 수 있는 안정적인 구조.
- **비용 절감**: 트래픽 변화에 따른 오토스케일링으로 불필요한 리소스 낭비 최소화.
- **사용자 만족도**: 빠르고 안정적인 서비스 제공으로 사용자 경험 향상.
