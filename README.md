# MBTI_Dating_APP

이 저장소는 **프로젝트의 최종 결과물**을 관리하는 레포토리지입니다.

본 프로젝트에서는 **Pull Request(PR) 방식을 사용**하여 안전하게 협업합니다.  
PR을 통해 사전 검수를 거치므로, 최대한 오류가 없는 상태로 프로젝트를 관리할 수 있습니다.

**브랜치 규칙**
- `main` 브랜치: 안정화된 코드만 존재  
- `feature` 브랜치: 각 기능 개발 작업용  
  - 직접 `main`에 push하지 않고, 먼저 `feature` 브랜치에서 작업 후 PR로 merge  
  - 팀원 리뷰 및 검증 후 main에 병합
- `fix` 브랜치: 아직 만들어지지 않았지만, 디버깅용
  - 오류를 고칠 때 새로 개발중인 기능과 섞이면 처리하기 어려우니
    `feature` 브랜치에 push 하지 않고, `fix` 브랜치에 push 하여 PR로 merge

> 요약: **개발 작업은 항상 feature 브랜치 → PR → main merge 순서**로 진행합니다.


# 프로젝트 구조

```
│  LICENSE
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
│      │          │      WebSocketConfig.java
│      │          │
│      │          ├─controller
│      │          │      MatchController.java
│      │          │      UserController.java
│      │          │
│      │          ├─dto
│      │          │      Match.java
│      │          │      User.java
│      │          │
│      │          ├─handler
│      │          │      ChatSocketHandler.java
│      │          │      CompositeMatchStrategy.java
│      │          │      GenderFilterStrategy.java
│      │          │      MatchQueueManager.java
│      │          │      MatchSocketHandler.java
│      │          │      MatchStrategy.java
│      │          │      OppositeMBTIStrategy.java
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
│              application-messagedb.properties
│              application-userdb.properties
│              application.properties
│
└─target
    └─classes
        │  application-messagedb.properties
        │  application-userdb.properties
        │  application.properties
        │
        └─com
            └─mbtidating
                │  ServerApplication.class
                │
                ├─config
                │      JwtUtil.class
                │      WebSocketConfig.class
                │
                ├─controller
                │      MatchController.class
                │      UserController.class
                │
                ├─dto
                │      Match$ChatMessage.class
                │      Match$Participant.class
                │      Match.class
                │      User$Tokens.class
                │      User.class
                │
                ├─handler
                │      ChatSocketHandler.class
                │      CompositeMatchStrategy.class
                │      GenderFilterStrategy.class
                │      MatchQueueManager.class
                │      MatchSocketHandler.class
                │      MatchStrategy.class
                │      OppositeMBTIStrategy.class
                │
                ├─model
                │      LoginRequest.class
                │      SignupRequest.class
                │
                ├─network
                │      ApiClient$HttpResult.class
                │      ApiClient.class
                │      WebSocketClient.class
                │
                ├─repository
                │      MatchRepository.class
                │      UserRepository.class
                │
                └─view
                        ChatView$1.class
                        ChatView.class
                        HomeView$1.class
                        HomeView$Bubble.class
                        HomeView$BubbleArea$Msg.class
                        HomeView$BubbleArea.class
                        HomeView$InfoPanel.class
                        HomeView$ProfileCard.class
                        HomeView.class
                        LoginView$1.class
                        LoginView.class
                        MainApp.class
                        MatchWaitView.class
                        MBTIInformView$1.class
                        MBTIInformView.class
                        MyMBTIView$1.class
                        MyMBTIView.class
                        SignupView$1.class
                        SignupView.class
```
