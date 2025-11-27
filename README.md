# MBTI_Dating_APP

이 저장소는 **프로젝트의 최종 결과물**을 관리하는 레포토리지입니다.

## 📌 브랜치 규칙
- **main 브랜치**  
  - 안정화된 코드만 존재
- **feature 브랜치**  
  - 각 기능 개발 작업용  
  - 직접 `main`에 push하지 않고, 반드시 `feature/???` 브랜치에서 작업  
  - 팀원 리뷰 및 검증 후 main에 병합
- **fix 브랜치**  
  - 디버깅 전용  
  - 개발중인 기능과 섞이지 않도록 `fix/???` 브랜치에서 작업  
  - 팀원 리뷰 및 검증 후 main에 병합

> **요약: 작업은 항상 feature 또는 fix 브랜치 → 코드 리뷰 → main merge 순서로 진행합니다.**

---

# Java Swing Client & Spring Boot 기반 실시간 매칭·채팅 데이팅 서비스

MBTI_Dating_APP은  
MBTI 성향 기반 매칭 알고리즘,  
WebSocket 기반 실시간 채팅,  
JWT 인증,  
Java Swing 클라이언트 UI로 구성된  
클라이언트–서버형 데이팅 애플리케이션입니다.

이 프로젝트는 백엔드(Spring Boot)와 데스크톱 클라이언트(Java Swing)가  
독립적으로 작동하며 WebSocket을 통해 실시간 통신합니다.

---

## 🚀 주요 기능

### 🔹 1. 사용자 인증 (JWT)
- 회원가입 / 로그인  
- 서버에서 JWT 발급 → 클라이언트는 토큰을 포함해 WebSocket 연결  
- 인증 실패 시 소켓 연결 거부

### 🔹 2. MBTI 기반 매칭 시스템
- `CompositeMatchStrategy` 기반  
- 여러 스코어 전략 합산  
- MBTI 궁합 점수 반영  
- 성별 / 나이 조건 적용  
- 사용자 상태 기반 큐 매칭  
- 매칭 성공 시 WebSocket 방 생성

### 🔹 3. 실시간 채팅 (WebSocket)
- 매칭된 두 사용자 간 실시간 채팅  
- Swing UI 말풍선 스타일  
- 서버에서 메시지 브로드캐스트  
- MongoDB 저장 가능(옵션)

### 🔹 4. Java Swing 기반 클라이언트 UI
- 로그인 / 홈 / 매칭대기 / 채팅 UI 제공  
- 메시지 버블 UI  
- WebSocket 자동 연결 & 재연결  
- JWT 기반 WebSocket 인증

### 🔹 5. Spring Boot 서버
- REST Controller (인증)  
- WebSocket Handler (매칭/채팅)  
- `MatchService`로 사용자 큐 관리  
- `UserRepository`로 MongoDB 연동

---

## 🧩 프로젝트 구조


### 📌 1) 서버 (Spring Boot)
```
MBTI_javaProject_server/
 ├─ controller/
 ├─ service/
 ├─ handler/
 ├─ repository/
 ├─ dto/
 ├─ model/
 ├─ config/
 └─ MBTIDatingAppApplication.java
```

### 📌 2) 클라이언트 (Java Swing)
```
MBTI_javaProject_client/
 ├─ view/
 ├─ dto/
 └─ network/
```


---

## 📡 WebSocket 구조

### 🟦 매칭 WebSocket  
`ws://localhost:8080/ws/match/{token}`

### 🟩 채팅 WebSocket  
`ws://localhost:8080/ws/chat/{roomId}/{userName}`

### 📤 클라이언트 → 서버 메시지 예시
```json
{
  "type": "chat",
  "content": "안녕하세요!"
}
```

## 🔐 인증 흐름

사용자가 로그인 → 서버에서 JWT 발급

클라이언트가 WebSocket 연결 시 URL에 토큰 포함

서버가 JWT 검증

유효하면 소켓 연결 허용

매칭 또는 채팅 핸들러로 사용자 등록

## ⚙️ 실행 방법
### 📌 1) 서버 실행 (Spring Boot)
```
cd MBTI_javaProject_server
./mvnw spring-boot:run
```

또는 IntelliJ에서 MBTIDatingAppApplication 실행

기본 포트: 8080

### 📌 2) 클라이언트 실행 (Java Swing)
```
cd MBTI_javaProject_client
java -jar client.jar
```

또는 IntelliJ에서 MainApp 실행

## 🛠️ 사용 기술 스택
**Backend**
- Java 17+
- Spring Boot
- Spring Web / WebSocket
- Lombok
- JWT (jjwt)
- MongoDB
- Maven

**Client**
- Java Swing
- AWT/Swing UI
- JSR-356 WebSocket Client

## 🧪 주요 클래스 설명
### 🔹 CompositeMatchStrategy
- 여러 매칭 점수 전략을 조합하여 총점을 계산하고
- 가장 높은 점수를 가진 후보를 매칭합니다.

### 🔹 MatchSocketHandler
- 사용자 매칭 큐 관리
- 일정 주기로 큐 내 사용자 매칭
- 매칭 성공 시 방 생성 후 각 사용자에게 결과 전달

### 🔹 ChatSocketHandler
- 채팅 메시지 중계
- 방에 속한 두 사용자에게 메시지 브로드캐스트

### 🔹 WebSocketClient (클라이언트)
- 서버와 WebSocket 연결
- 메시지 수신 핸들러 관리
- 매칭 / 채팅 타입 분리

## 📘 향후 개선 예정 기능

- MBTI 가중치 기반 고도화 (선호도/취향 태그 추가)
- 이미지 전송 기능
- 채팅 내역 자동 저장 및 조회
- 친구 목록 및 재매칭 기능
- UI 개선 (JavaFX 전환 고려)

## 👨‍💻 개발팀

SKHU Java Project Team 1
- 박민지
- 김민아
- 김민석
- 유희승

## 📄 License
이 프로젝트는 MIT License를 따릅니다.
