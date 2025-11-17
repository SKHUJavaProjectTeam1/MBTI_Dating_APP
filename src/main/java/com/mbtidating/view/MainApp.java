package com.mbtidating.view;

import javax.swing.*;
import java.awt.*;

public class MainApp extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    // View 키값 정의
    public static final String LOGIN = "login";
    public static final String SIGNUP = "signup";
    public static final String HOME = "home";
    public static final String MBTI_INFO = "mbtiInfoView";
    public static final String MYMBTI = "myMbti";
    public static final String MATCH_WAIT = "matchWait";
    public static final String CHAT = "chat";

    // View 인스턴스
    private final LoginView loginView;
    private final SignupView signupView;
    private final HomeView homeView;
    private final MBTIInformView mbtiInfoView;
    private final MyMBTIView myMbtiView;
    private final MatchWaitView matchWaitView;
    private final ChatView chatView;

    // JWT 토큰 저장용
    private String jwtToken;

    public MainApp() {
        super("MBTI MATCH");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);

        // View 초기화
        loginView = new LoginView(this);
        signupView = new SignupView(this);
        homeView = new HomeView(this);
        mbtiInfoView = new MBTIInformView(this);
        myMbtiView = new MyMBTIView(this);
        matchWaitView = new MatchWaitView(this);
        chatView = new ChatView(this);

        // CardPanel에 추가
        cardPanel.add(loginView, LOGIN);
        cardPanel.add(signupView, SIGNUP);
        cardPanel.add(homeView, HOME);
        cardPanel.add(mbtiInfoView, MBTI_INFO);
        cardPanel.add(myMbtiView, MYMBTI);
        cardPanel.add(matchWaitView, MATCH_WAIT);
        cardPanel.add(chatView, CHAT);

        add(cardPanel);
        showView(LOGIN);
    }

    // ===== 뷰 전환 메서드 =====
    public void showView(String key) {
        cardLayout.show(cardPanel, key);
        cardPanel.revalidate();
        cardPanel.repaint();
        System.out.println(">> View switched to: " + key);
    }

    // ===== getter & setter =====
    public MatchWaitView getMatchWaitView() { return matchWaitView; }
    public ChatView getChatView() { return chatView; }

    public void setJwtToken(String token) { this.jwtToken = token; }
    public String getJwtToken() { return jwtToken; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainApp().setVisible(true));
    }
}
