# MBTI_Dating_APP

이 저장소는 **프로젝트의 최종 결과물**을 관리하는 레포토리지입니다.

**브랜치 규칙**
- `main` 브랜치: 안정화된 코드만 존재  
- `feature` 브랜치: 각 기능 개발 작업용  
  - 직접 `main`에 push하지 않고, 먼저 `feature/???` 브랜치에서 작업  
  - 팀원 리뷰 및 검증 후 main에 병합
- `fix` 브랜치: 디버깅용
  - 오류를 고칠 때 새로 개발중인 기능과 섞이면 처리하기 어려우니
    `feature` 브랜치에 push 하지 않고, `fix/???` 브랜치에 push
  - 팀원 리뷰 및 검증 후 main에 병합

> 요약: **작업은 항상 feature or fix 브랜치 → 코드 리뷰 → main merge 순서**로 진행합니다.


#프로젝트 테스트 실행 순서

1.serverApplication.java 
2. view/MainApp.java


# 프로젝트 구조

```
C:.
│  .classpath
│  .factorypath
│  .gitignore
│  .project
│  LICENSE
│  MBTI_javaProject_server.iml
│  pom.xml
│  README.md
│
├─images
│      default_profile.png
│      pencil.png
│      submit.png
│
├─src
│  └─main
│      ├─java
│      │  └─com
│      │      └─mbtidating
│      │          │  ServerApplication.java
│      │          │
│      │          ├─config
│      │          │      JwtUtil.java
│      │          │      PasswordConfig.java
│      │          │      WebSocketConfig.java
│      │          │
│      │          ├─controller
│      │          │      MatchController.java
│      │          │      UserController.java
│      │          │
│      │          ├─dto
│      │          │      Match.java
│      │          │      User.java
│      │          │      UserUpdateRequest.java
│      │          │
│      │          ├─handler
│      │          │      ChatSocketHandler.java
│      │          │      CompositeMatchStrategy.java
│      │          │      GenderScoreStrategy.java
│      │          │      MatchQueueManager.java
│      │          │      MatchSocketHandler.java
│      │          │      MatchStrategy.java
│      │          │      MbtiScoreStrategy.java
│      │          │
│      │          ├─map
│      │          │      MbtiScoreMap.java
│      │          │
│      │          ├─model
│      │          │      LoginRequest.java
│      │          │      SignupRequest.java
│      │          │
│      │          ├─network
│      │          │      ApiClient.java
│      │          │      WebSocketClient.java
│      │          │
│      │          ├─repository
│      │          │      MatchRepository.java
│      │          │      UserRepository.java
│      │          │
│      │          └─view
│      │                  ChatView.java
│      │                  HomeView.java
│      │                  LoginView.java
│      │                  MainApp.java
│      │                  MatchWaitView.java
│      │                  MBTIInformView.java
│      │                  MyMBTIView.java
│      │                  SignupView.java
│      │
│      └─resources
│              application-userdb.properties
│              application.properties
│
```
