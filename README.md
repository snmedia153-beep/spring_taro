# 🔮 Mystic Tarot - Spring Boot Community & Consultation

> **"운명은 정해진 것이 아니라 선택하는 것입니다."** > 신비로운 타로 테마를 입힌 Spring Boot 기반 커뮤니티 및 상담 예약 플랫폼입니다.

---

## ✨ 주요 기능 (Key Features)

### 🌙 신비로운 타로 테마 UI/UX
* 다크 모드 기반의 고품격 디자인 (Gold & Dark Navy)
* 애니메이션과 아이콘을 활용한 사용자 몰입감 증대

### 🖋️ 커뮤니티 (운명의 광장)
* **게시판 CRUD:** MyBatis를 활용한 고성능 게시글 조회 및 관리
* **최신글 위젯:** 메인 페이지에서 실시간 업데이트되는 최신 게시글 4개 연동
* **이미지 첨부:** 멀티 파일 업로드 및 게시판 이미지 렌더링 지원

### 💳 결제 시스템 (운명의 계약)
* **Toss Payments 연동:** 실시간 카드 결제 프로세스 구현
* **승인 로직:** 서버 대 서버(Confirm API) 통신을 통한 안전한 결제 확정 및 DB 연동
* **MyPage:** 로그인한 사용자의 고유 결제 내역(Payment/Member FK 연동) 조회

### 🔒 보안 및 알림
* **Spring Security:** 인증/인가 처리 및 로그인 상태에 따른 동적 헤더 UI 구현
* **SSE(Server-Sent Events):** 결제 완료 시 사용자에게 실시간 통찰 알림 발송

---

## 📂 프로젝트 구조 (Project Structure)

```text
src/main/java/com/example/demo/
├── controller/     # 뷰 컨트롤러 및 API 컨트롤러
├── service/        # 비즈니스 로직 및 외부 API 연동
├── repository/     # MyBatis Mapper 및 JPA 리포지토리
├── dto/            # 데이터 전송 객체 (BoardDTO, MemberDTO)
├── entity/         # JPA 엔티티 (Payment, Member, Reservation)
└── config/         # Security, MyBatis, SSE 설정 관련
