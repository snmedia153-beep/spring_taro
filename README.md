# 🔮 Mystic Tarot | 클라우드 기반 타로 상담 및 커뮤니티 플랫폼

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot)
![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql)
![Render](https://img.shields.io/badge/Deployment-Render-46E3B7?logo=render)

> **서비스 배포 주소:** [https://spring-taro.onrender.com/](https://spring-taro.onrender.com/)

![Video-Getsitecontrol (1)](https://github.com/user-attachments/assets/1b7fbca6-77c6-4a3d-a86e-5b580f825aab)


## 🚀 프로젝트 개요
실시간 타로 상담 예약과 운세 기록을 위한 커뮤니티 플랫폼입니다. 기존 PHP 기반의 개발 경험을 바탕으로 **Java/Spring Boot**로 기술 스택을 확장하며 고도화하였습니다. 
특히 클라우드 네이티브 환경(Render)의 한계를 극복하기 위해 클라우드 플레어의 오브젝트 스토리지(R2)와 AWS 외부 RDS(Mysql)를 활용한 무상태(Stateless) 아키텍처를 구축하는 데 집중했습니다.

---

## 🛠 Tech Stack
- **Backend:** Java 17, Spring Boot 3.x, Spring Security, MyBatis
- **Database:** AWS RDS (MySQL 8.0)
- **Infrastructure:** Cloudflare R2 (Object Storage), Render (Cloud Hosting)
- **Frontend:** Thymeleaf, Bootstrap 5, JavaScript (ES6+)
- **API:** Toss Payments (결제 시스템 연동)

---

## 🌟 핵심 기능 및 성과

### 1. 클라우드 기반 미디어 아키텍처 구축
- **Cloudflare R2 연동:** 호스팅 서버의 휘발성 디스크 문제를 해결하기 위해 S3 호환 API를 지원하는 R2 스토리지를 도입, 데이터 영속성을 확보했습니다.
- **이미지 최적화 (Thumbnailator):** 서버 사이드에서 업로드 이미지를 리사이징 및 압축(JPEG 70% 품질) 처리하여 스토리지 비용을 절감하고 페이지 로딩 속도를 개선했습니다.

### 2. 하이브리드 사용자 권한 시스템
- **회원/비회원 공용 게시판:** `member_id` 식별자와 비회원용 비밀번호 기반의 하이브리드 검증 로직을 설계하여 사용자 접근성을 높였습니다.
- **Spring Security 커스텀:** `PrincipalDetails`를 확장하여 세션 정보와 DB 게시글 소유권을 대조하는 정교한 보안 로직을 구현했습니다.

### 3. 실시간 알림 서비스 (SSE)
- **SSE(Server-Sent Events) 구현:** 관리자가 사용자에게 실시간으로 상담 상태 및 공지 알림을 전송할 수 있는 단방향 통신 채널을 구축했습니다.

### 4. 안정적인 데이터 트랜잭션 관리
- 게시글 수정/삭제 시 클라우드 저장소의 물리 파일과 DB 레코드 삭제를 하나의 트랜잭션으로 묶어 데이터 불일치 문제를 방지했습니다.

---

## 📂 프로젝트 구조
```text
src/main/java/com/example/demo/
├── config/             # Security, S3(R2), MyBatis Configuration
├── controller/         # View 및 API Routing (Board, Admin, Payment)
├── dto/                # Data Transfer Object (Board, Member, File)
├── entity/             # JPA/DB Entity (Member)
├── repository/         # SqlSessionTemplate 기반 DB Access
└── service/            # Business Logic (R2 연동, 게시판 로직 등)
