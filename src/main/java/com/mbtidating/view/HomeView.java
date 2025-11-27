package com.mbtidating.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import com.mbtidating.dto.User;
import com.mbtidating.network.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class HomeView extends JPanel {

    private final MainApp mainApp;
    private InfoPanel infoPanel;
    private final ProfileCard[] cards = new ProfileCard[8];

    // 🎨 개선된 색상 팔레트 적용
    private final Color color1 = new Color(250, 240, 240); // Warm Off-White (배경)
    private final Color color2 = new Color(255, 218, 225); // Soft Light Pink ('내 정보' 패널)
    private final Color color3 = new Color(230, 220, 240); // Light Lavender ('최근 채팅' 패널)
    private final Color color4 = new Color(190, 150, 210); // Muted Lavender/Purple (버튼)
    private final Color cardBackground = Color.WHITE; // 프로필 카드 배경
    private final Color defaultFontColor = new Color(50, 50, 50); // 기본 폰트 색상
    private final Color subtleBorder = new Color(220, 220, 220); // 얇은 구분선/테두리

    public HomeView(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(color1);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        // ✅ 유저 정보 먼저 세팅 후, 프로필 목록 불러오기
        updateUserInfo(mainApp.getLoggedInUser());
        loadProfilesFromServer();
    }


    public void updateUserInfo(User user) {
        if (infoPanel != null) infoPanel.update(user);
    }
    
    // 이하는 서버 통신 관련 코드로, 디자인 변경 없이 유지합니다.
    private void loadProfilesFromServer() {
        new javax.swing.SwingWorker<Void, Void>() {
            String response = null;

            @Override
            protected Void doInBackground() throws Exception {
                String token = mainApp.getJwtToken();
                ApiClient.HttpResult res = ApiClient.get("/users", token);

                if (res.isOk()) {
                    response = res.body;
                } else {
                    System.err.println("💥 사용자 목록 조회 실패: " + res.code + " / " + res.body);
                }
                return null;
            }

            @Override
            protected void done() {
                if (response == null) return;

                try {
                    // 🔽 MBTI 직접 꺼내기 (UI에서 가져오지 말고)
                    User me = mainApp.getLoggedInUser();
                    String myMbti = "-";
                    if (me != null && me.getMbti() != null) {
                        Map<String, String> mbtiMap = me.getMbti();
                        myMbti = mbtiMap.getOrDefault("EI", "")
                                + mbtiMap.getOrDefault("SN", "")
                                + mbtiMap.getOrDefault("TF", "")
                                + mbtiMap.getOrDefault("JP", "");
                    }

                    JSONArray arr = new JSONArray(response);
                    for (int i = 0; i < arr.length() && i < cards.length; i++) {
                        JSONObject obj = arr.getJSONObject(i);

                        String name = obj.optString("userName", "이름 없음");
                        String gender = obj.optString("gender", "m").equals("m") ? "남자" : "여자";
                        int age = obj.optInt("age", 0);

                        String profileNum = obj.optString("profileImg", "1");
                        if ("default.jpg".equals(profileNum)) {
                            int randomNum = 1 + (int)(Math.random() * 5); // 1 ~ 5
                            profileNum = String.valueOf(randomNum);
                        }


                        JSONObject mbti = obj.optJSONObject("mbti");
                        String mbtiStr = "-";
                        if (mbti != null) {
                            mbtiStr = mbti.optString("EI", "")
                                    + mbti.optString("SN", "")
                                    + mbti.optString("TF", "")
                                    + mbti.optString("JP", "");
                        }

                        int matchPercent = calculateMbtiMatch(myMbti, mbtiStr);
                        cards[i].setProfile(name, mbtiStr, gender, age, profileNum, matchPercent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }.execute();
    }
    
    private int calculateMbtiMatch(String myMbti, String otherMbti) {
        if (myMbti == null || otherMbti == null || myMbti.length() != 4 || otherMbti.length() != 4)
            return 0;

        int score = 0;
        for (int i = 0; i < 4; i++) {
            if (myMbti.charAt(i) == otherMbti.charAt(i)) {
                score += 25;
            }
        }

        return score; // 최대 100점
    }


    // ========================== 헤더 영역 (큰 변경 없음) ==========================
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        // 테두리 색상 subtleBorder 적용
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, subtleBorder),
                new EmptyBorder(8, 16, 8, 16)));
        header.setBackground(color1); // 배경색 통일

        JLabel title = new JLabel("MBTI MATCH", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(defaultFontColor); // 폰트 색상 적용
        header.add(title, BorderLayout.NORTH);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 네비게이션 아래 구분선 색상 변경
                g.setColor(color4.darker()); // 강조색의 어두운 버전
                g.drawLine(0, getHeight()-1, getWidth(), getHeight()-1);
            }
        };

        JButton btnHome = createNavButton("HOME");
        JButton btnMyMBTI = createNavButton("나의 MBTI");
        JButton btnGuide = createNavButton("MBTI 소개");
        JButton btnChat = createNavButton("채팅");
        JButton btnLogout = createNavButton("로그아웃");
        JButton btnMatch = createNavButton("매칭하기");

        nav.add(btnHome); nav.add(separator());
        nav.add(btnMatch); nav.add(separator());
        nav.add(btnMyMBTI); nav.add(separator());
        nav.add(btnGuide); nav.add(separator());
        nav.add(btnChat); nav.add(separator());
        nav.add(btnLogout);

        nav.setBackground(color1);
        header.add(nav, BorderLayout.SOUTH);

        // 액션 리스너는 변경 없이 유지
        btnLogout.addActionListener(e -> mainApp.showView(MainApp.LOGIN));
        btnGuide.addActionListener(e -> mainApp.showView(MainApp.MBTI_INFO));
        btnMyMBTI.addActionListener(e -> mainApp.showView(MainApp.MYMBTI));
        btnChat.addActionListener(e -> {
            String token = mainApp.getJwtToken();
            User loggedIn = mainApp.getLoggedInUser();

            if (loggedIn == null || token == null || token.isEmpty()) {
                JOptionPane.showMessageDialog(this, "로그인이 필요합니다.");
                mainApp.showView(MainApp.LOGIN);
                return;
            }

            String selfId = loggedIn.getId();
            String selfName = loggedIn.getUserName();

            try {
                // 1) 내가 속한 채팅방 목록 조회
                ApiClient.HttpResult res = ApiClient.get("/chat/rooms/" + selfId);

                if (!res.isOk()) {
                    JOptionPane.showMessageDialog(this, "서버 연결 오류");
                    return;
                }

                JSONArray arr = new JSONArray(res.body);

                // 방이 하나라도 있으면 그 방으로 바로 입장
                if (arr.length() > 0) {
                    JSONObject room = arr.getJSONObject(0);
                    String roomId = room.getString("roomId");

                    // ---------- 상대방 정보 추출 ----------
                    String partnerId = null;
                    String partnerName = "(상대 없음)";

                    if (room.has("participants")) {
                        JSONArray ps = room.getJSONArray("participants");

                        for (int i2 = 0; i2 < ps.length(); i2++) {
                            JSONObject p = ps.getJSONObject(i2);
                            String uid = p.optString("userId", "");
                            String uname = p.optString("userName", "");

                            // 자기 자신이 아닌 사람 = 상대방
                            if (!uid.isEmpty() && !uid.equals(selfId)) {
                                partnerId = uid;
                                partnerName = !uname.isEmpty() ? uname : uid;
                                break;
                            }
                        }
                    }

                    // ---------- ChatView 호출 ----------
                    ChatView chatView = mainApp.getChatView();
                    chatView.startChat(
                            roomId,
                            selfId,
                            selfName,
                            partnerId,
                            partnerName
                    );

                    mainApp.showView(MainApp.CHAT);
                    return;
                }

                // 방이 없으면 안내
                JOptionPane.showMessageDialog(this, "매칭하기를 통해 대화를 시작하세요.");

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "채팅방 불러오기 오류");
            }
        });


        btnMatch.addActionListener(e -> {
            mainApp.showView(MainApp.MATCH_WAIT);
            mainApp.getMatchWaitView().startMatching(mainApp.getJwtToken());
        });

        return header;
    }

    private JButton createNavButton(String text) {
        JButton b = new JButton(" " + text + " ");
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 10, 6, 10));
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setForeground(defaultFontColor);
        return b;
    }

    private Component separator() {
        JLabel s = new JLabel(" | ");
        s.setBorder(new EmptyBorder(0, 4, 0, 4));
        s.setForeground(new Color(150, 150, 150));
        return s;
    }
    // ========================== 본문 영역 ==========================
    private JComponent buildBody() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(20, 20, 20, 20)); // 여백 증가
        root.setBackground(color1);

        infoPanel = new InfoPanel();
        JPanel left = infoPanel;
        left.setPreferredSize(new Dimension(280, 0)); // '내 정보' 패널 너비 증가

        JPanel center = new JPanel(new BorderLayout());
        JLabel recTitle = new JLabel("추천 상대");
        recTitle.setFont(recTitle.getFont().deriveFont(Font.BOLD, 18f)); // 폰트 크기 증가
        recTitle.setBorder(new EmptyBorder(0, 8, 12, 0)); // 여백 증가
        recTitle.setForeground(defaultFontColor);
        center.add(recTitle, BorderLayout.NORTH);
        center.add(recommendGrid(), BorderLayout.CENTER);
        center.setBackground(color1);

        JPanel right = chatPanel();
        right.setPreferredSize(new Dimension(380, 0)); // '채팅' 패널 너비 증가

        JPanel middle = new JPanel(new BorderLayout(24, 0)); // 컴포넌트 간 간격 증가
        middle.add(left, BorderLayout.WEST);
        middle.add(center, BorderLayout.CENTER);
        middle.add(right, BorderLayout.EAST);
        middle.setBackground(color1);

        root.add(middle, BorderLayout.CENTER);
        return root;
    }

    private JComponent recommendGrid() {
        // 간격 증가
        JPanel grid = new JPanel(new GridLayout(2, 4, 18, 18));
        grid.setOpaque(false);

        for (int i = 0; i < cards.length; i++) {
            cards[i] = new ProfileCard(cardBackground); // 💡 흰색 배경 전달
            grid.add(cards[i]);
        }

        loadProfilesFromServer();

        return grid;
    }


    private JPanel chatPanel() {
        JPanel wrap = new JPanel(new BorderLayout());
        // 둥근 모서리 적용을 위해 RoundPanel 사용
        wrap = new RoundPanel(20, color3, subtleBorder, 1);
        wrap.setLayout(new BorderLayout());
        wrap.setBorder(new EmptyBorder(16, 16, 16, 16));
        wrap.setOpaque(false);


        JLabel h = new JLabel("최근 채팅");
        h.setFont(h.getFont().deriveFont(Font.BOLD, 16f));
        h.setBorder(new EmptyBorder(0, 0, 10, 0));
        h.setForeground(defaultFontColor);
        wrap.add(h, BorderLayout.NORTH);

        BubbleArea bubbles = new BubbleArea(color3);
        bubbles.addLeft("안녕하세요! 매칭을 축하드립니다.");

        JScrollPane sp = new JScrollPane(bubbles);
        sp.setBorder(null);
        // 스크롤 패널 배경색을 채팅 패널 배경색과 일치
        sp.getViewport().setBackground(color3); 
        wrap.add(sp, BorderLayout.CENTER);

        JTextField input = new JTextField();
        // 폰트 크기 및 색상 조정
        input.setFont(input.getFont().deriveFont(14f));
        input.setForeground(defaultFontColor);
        input.setBorder(new CompoundBorder(
            new LineBorder(subtleBorder, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));

        JButton send = new JButton("보내기"); // 아이콘 대신 텍스트로 변경
        send.setPreferredSize(new Dimension(60, 36));
        send.setBackground(color4);
        send.setForeground(Color.WHITE); // 버튼 폰트 흰색
        send.setBorderPainted(false);
        send.setOpaque(true);
        send.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));


        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setBorder(new EmptyBorder(12, 0, 0, 0));
        bottom.setOpaque(false);
        bottom.add(input, BorderLayout.CENTER);
        bottom.add(send, BorderLayout.EAST);

        wrap.add(bottom, BorderLayout.SOUTH);
        return wrap;
    }

    // ========================== 왼쪽 내 정보 패널 ==========================
    class InfoPanel extends JPanel {

        private JLabel avatarLabel;   // 🔥 아바타 라벨을 필드로 선언
        private final JLabel idValue = new JLabel("-");
        private final JLabel mbtiValue = new JLabel("-");
        private final JLabel genderValue = new JLabel("-");
        private final JLabel ageValue = new JLabel("-");
        private final JLabel userNameValue = new JLabel("-");

        InfoPanel() {
            super(new BorderLayout());

            JPanel wrapper = new RoundPanel(20, color2, subtleBorder, 1);
            wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
            wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
            wrapper.setOpaque(false);

            JLabel title = new JLabel("내 정보");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            wrapper.add(title);

            // 🔥 placeholder 아바타 먼저 넣기
            avatarLabel = avatarLabel("/images/default_profile.png", 100);
            avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            avatarLabel.setBorder(new LineBorder(color4, 2, true));
            wrapper.add(avatarLabel);
            wrapper.add(Box.createVerticalStrut(24));

            JPanel infoContainer = new JPanel();
            infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.Y_AXIS));
            infoContainer.setOpaque(false);
            infoContainer.add(infoLine("아이디", idValue));
            infoContainer.add(infoLine("닉네임", userNameValue));
            infoContainer.add(infoLine("MBTI", mbtiValue));
            infoContainer.add(infoLine("성별", genderValue));
            infoContainer.add(infoLine("나이", ageValue));
            wrapper.add(infoContainer);
            wrapper.add(Box.createVerticalStrut(16)); // 여백 약간 추가

            JButton edit = new JButton("프로필 수정");
            edit.setBackground(color4);
            edit.setForeground(Color.WHITE);
            edit.setBorderPainted(false);
            edit.setOpaque(true);
            edit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            edit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            edit.setAlignmentX(Component.CENTER_ALIGNMENT);

            edit.addActionListener(e -> {
                User user = mainApp.getLoggedInUser();
                if (user == null) {
                    JOptionPane.showMessageDialog(InfoPanel.this, "로그인 정보가 없습니다.");
                    return;
                }

                Window owner = SwingUtilities.getWindowAncestor(HomeView.this);
                ProfileEditDialog dialog = new ProfileEditDialog(owner, user);
                dialog.setLocationRelativeTo(HomeView.this);
                dialog.setVisible(true);

                update(user); // 수정 후 정보 갱신
            });

            wrapper.add(edit);

            wrapper.add(Box.createVerticalGlue());

            add(wrapper, BorderLayout.CENTER);
            setOpaque(false);
        }

        // 🔥 user 정보가 갱신될 때 아바타도 갱신하도록
        void update(User user) {

            if (user == null) return;

            idValue.setText(user.getId());
            userNameValue.setText(user.getUserName());
            mbtiValue.setText(buildMbti(user.getMbti()));
            genderValue.setText(buildGender(user.getGender()));
            ageValue.setText(user.getAge() + "세");

            // 🔥 프로필 이미지 적용
            String profileNum = user.getProfileImg();
            if (profileNum == null || profileNum.equals("default.jpg")) {
                profileNum = String.valueOf(1 + (int)(Math.random()*5));
            }

            String avatarPath = "/images/profile" + profileNum + ".png";

            ImageIcon icon = new ImageIcon(
                    new ImageIcon(getClass().getResource(avatarPath))
                    .getImage()
                    .getScaledInstance(100, 100, Image.SCALE_SMOOTH)
            );

            avatarLabel.setIcon(icon);

            revalidate();
            repaint();
        }
    }


        private JComponent infoLine(String label, JLabel valueLabel) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);

            JLabel l = new JLabel(label + " ");
            l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
            l.setForeground(defaultFontColor);

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(200, 200, 200));
            sep.setBorder(new EmptyBorder(0, 8, 0, 8)); // 구분선 좌우 여백

            valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            valueLabel.setForeground(defaultFontColor.darker());

            p.add(l, BorderLayout.WEST);
            p.add(sep, BorderLayout.CENTER);
            p.add(valueLabel, BorderLayout.EAST);

            p.setBorder(new EmptyBorder(8, 0, 8, 0)); // 상하 여백 증가
            return p;
        }
        
        // MBTI, Gender 빌드 로직은 변경 없이 유지
        private String buildMbti(Map<String, String> mbti) {
             if (mbti == null) return "-";
             String[] keys = {"EI", "SN", "TF", "JP"};
             StringBuilder sb = new StringBuilder();
             for (String k : keys) {
                 String v = mbti.get(k);
                 if (v != null) sb.append(v);
             }
             return sb.length() == 0 ? "-" : sb.toString();
        }

        private String buildGender(String g) {
            if (g == null) return "-";
            g = g.toLowerCase();
            if (g.startsWith("m")) return "남자";
            if (g.startsWith("f")) return "여자";
            return g;
        }
    // ========================== 공통 유틸: 아바타 ==========================
    private JLabel avatarLabel(String pathOrClasspath, int size) {
        Image img;
        URL url = getClass().getResource(pathOrClasspath);
        if (url != null)
            img = new ImageIcon(url).getImage();
        else
            img = new ImageIcon(pathOrClasspath).getImage();

        Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        JLabel label = new JLabel(new ImageIcon(scaled));
        label.setPreferredSize(new Dimension(size, size));
        label.setMinimumSize(new Dimension(size, size));
        label.setMaximumSize(new Dimension(size, size));
        // 아바타 테두리 삭제 및 원형 효과를 위해 별도 처리 (여기서는 단순 사각형으로만 처리)
        label.setBorder(null); 
        return label;
    }

    // ========================== 추천 카드 (개선) ==========================
    static class ProfileCard extends JPanel {
        private JLabel nameLabel;
        private JLabel mbtiLabel;
        private JLabel genderAgeLabel;
        private JLabel imageLabel;
        private Color cardBackground;
        private JLabel matchLabel;
        private JProgressBar matchBar;
        private RoundPanel panel;
        private String currentProfileNum = "1"; // 이미지 번호 기억용

        private Color getMatchColor(int percent) {
            if (percent >= 80) return new Color(255, 105, 180);
            if (percent >= 60) return new Color(255, 165, 0);
            if (percent >= 40) return new Color(255, 215, 0);
            return new Color(200, 200, 200);
        }

        ProfileCard(Color cardBackground) {
            this.cardBackground = cardBackground;
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(150, 220));
            setOpaque(false);

            // 둥근 모서리 패널
            panel = new RoundPanel(16, cardBackground, new Color(200, 200, 200), 1);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(12, 12, 12, 12));
            panel.setOpaque(false);

            // 이미지 라벨
            imageLabel = new JLabel(new ImageIcon("images/default_profile.png"));
            imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imageLabel.setBorder(new LineBorder(new Color(255, 218, 225), 2, true));
            imageLabel.setOpaque(false);
            panel.add(imageLabel);
            panel.add(Box.createVerticalStrut(12));

            // 이름
            nameLabel = new JLabel("이름");
            nameLabel.setFont(new Font("Dialog", Font.BOLD, 15));
            nameLabel.setForeground(new Color(30, 30, 30));
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(nameLabel);

            // MBTI
            mbtiLabel = new JLabel("MBTI");
            mbtiLabel.setForeground(new Color(190, 150, 210));
            mbtiLabel.setFont(new Font("Dialog", Font.BOLD, 13));
            mbtiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(mbtiLabel);

            // 성별/나이
            genderAgeLabel = new JLabel("성별 / 나이");
            genderAgeLabel.setForeground(new Color(100, 100, 100));
            genderAgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(genderAgeLabel);

            // 마우스 오버 시 효과 (이미지 크기 포함)
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    panel.setBackground(new Color(240, 230, 255));
                    panel.setBorder(new LineBorder(new Color(180, 120, 210), 2, true));
                    updateProfileImage(100); // 확대
                    panel.repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    panel.setBackground(cardBackground);
                    panel.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));
                    updateProfileImage(80); // 원래 크기로 복원
                    panel.repaint();
                }
            });

            add(panel, BorderLayout.CENTER);
        }

        public void setProfile(String name, String mbti, String gender, int age, String profileNum, int matchPercent) {
            nameLabel.setText(name);
            mbtiLabel.setText(mbti);
            genderAgeLabel.setText(gender + " / " + age + "세");
            this.currentProfileNum = profileNum;

            updateProfileImage(80); // 초기 크기 적용
        }

        private void updateProfileImage(int size) {
            String imgPath = "/images/profile" + currentProfileNum + ".png";
            URL url = getClass().getResource(imgPath);
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
                ImageIcon icon = new ImageIcon(img);
                imageLabel.setIcon(icon);

                imageLabel.setPreferredSize(new Dimension(size, size));
                imageLabel.setMinimumSize(new Dimension(size, size));
                imageLabel.setMaximumSize(new Dimension(size, size));
                imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                imageLabel.setHorizontalAlignment(JLabel.CENTER);
            }
        }
    }

    // ========================== 채팅 말풍선 영역 (개선) ==========================
    static class BubbleArea extends JPanel {
        private final List<Msg> msgs = new ArrayList<>();
        private Color bgColor;

        static class Msg {
            String text;
            boolean right;
            Msg(String t, boolean r) { text = t; right = r; }
        }

        BubbleArea(Color bgColor) {
            this.bgColor = bgColor;
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(bgColor);
            msgs.clear(); // 초기화
        }

        void addLeft(String t) { addMsg(new Msg(t, false)); }

        private void addMsg(Msg m) {
            msgs.add(m);
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            Bubble b = new Bubble(m.text, m.right);
            
            if (m.right) {
                row.add(b, BorderLayout.EAST);
            } else {
                row.add(b, BorderLayout.WEST);
            }
            
            row.setBorder(new EmptyBorder(6, 6, 6, 6));
            add(row);
            revalidate();
            repaint();
            // 스크롤을 맨 아래로 이동하는 로직이 필요하면 여기에 추가
        }
    }

    // ========================== 말풍선 컴포넌트 (개선) ==========================
    static class Bubble extends JComponent {
        private final String text;
        private final boolean right;

        Bubble(String text, boolean right) {
            this.text = text;
            this.right = right;
            // 텍스트 길이에 따라 크기를 동적으로 조절
            int lineCount = (int) Math.ceil(text.length() / 20.0);
            int prefHeight = 24 + lineCount * 18;
            int prefWidth = Math.min(240, 60 + text.length() * 10); // 최대 너비 240
            setPreferredSize(new Dimension(prefWidth, prefHeight));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int arc = 12; // 둥근 정도 감소

            // 말풍선 배경
            Color bubbleColor = right ? new Color(255, 255, 255) : new Color(255, 230, 240); // 내가 보낸 메시지: 흰색, 상대방: 연한 핑크
            g2.setColor(bubbleColor);
            Shape r = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, arc, arc);
            g2.fill(r);
            
            // 말풍선 테두리
            g2.setColor(new Color(220, 220, 220));
            g2.draw(r);

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Dialog", Font.PLAIN, 13));
            FontMetrics fm = g2.getFontMetrics();
            int pad = 10, y = pad + fm.getAscent();

            // 텍스트 줄바꿈 개선
            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String candidate = (line.length() == 0 ? word : line + " " + word);
                if (fm.stringWidth(candidate) > w - pad * 2) {
                    g2.drawString(line.toString(), pad, y);
                    line = new StringBuilder(word);
                    y += fm.getHeight();
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            g2.drawString(line.toString(), pad, y);
            g2.dispose();
        }
    }
    
    // ========================== 둥근 모서리 패널 클래스 추가 ==========================
    // HomeView 내부에 정의
    static class RoundPanel extends JPanel {
        private int cornerRadius = 15;
        private Color bgColor;
        private Color borderColor;
        private int borderThickness;

        public RoundPanel(int radius, Color bgColor, Color borderColor, int thickness) {
            this.cornerRadius = radius;
            this.bgColor = bgColor;
            this.borderColor = borderColor;
            this.borderThickness = thickness;
            setOpaque(false); // 배경을 투명하게 만듦
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            
            // 1. 배경 채우기
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0, width - 1, height - 1, cornerRadius, cornerRadius));

            // 2. 테두리 그리기
            if (borderThickness > 0) {
                g2.setColor(borderColor);
                // 테두리 두께를 고려하여 외곽선을 그림
                g2.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1, cornerRadius, cornerRadius));
            }
        }
    }


    // ========================== 프로필 수정 다이얼로그 (생략 및 유지) ==========================
    // ProfileEditDialog 클래스는 기능적 요소가 많으므로 디자인 변경 없이 기존 코드를 유지했습니다.
    class ProfileEditDialog extends JDialog {
        // ... (기존 코드와 동일하게 유지) ...
        private final User user;

        private final JTextField tfId = new JTextField();
        private final JTextField tfUserName = new JTextField();
        private final JTextField tfMbti = new JTextField();
        private final JComboBox<String> cbGender =
                new JComboBox<>(new String[]{"남자", "여자"});
        private final JSpinner spAge =
                new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));

        ProfileEditDialog(Window owner, User user) {
            super(owner, "프로필 수정", ModalityType.APPLICATION_MODAL);
            this.user = user;
            
            // 디자인 개선: 다이얼로그 배경색을 color2로 변경
            ((JComponent) getContentPane()).setBackground(new Color(255, 240, 245));
            
            setLayout(new BorderLayout(10, 10));
            ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false); // 배경색 적용을 위해 투명하게 설정
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 4, 4, 4);
            c.fill = GridBagConstraints.HORIZONTAL;

            int row = 0;

            // 아이디 (읽기 전용)
            c.gridx = 0; c.gridy = row;
            form.add(new JLabel("아이디"), c);
            c.gridx = 1;
            tfId.setEditable(false);
            form.add(tfId, c);
            row++;

            //닉네임
            c.gridx = 0; c.gridy = row;
            form.add(new JLabel("닉네임"), c);
            c.gridx = 1;
            form.add(tfUserName, c);
            row++;
            
            // MBTI
            c.gridx = 0; c.gridy = row;
            form.add(new JLabel("MBTI (예: INTJ)"), c);
            c.gridx = 1;
            form.add(tfMbti, c);
            row++;

            // 성별
            c.gridx = 0; c.gridy = row;
            form.add(new JLabel("성별"), c);
            c.gridx = 1;
            form.add(cbGender, c);
            row++;

            // 나이
            c.gridx = 0; c.gridy = row;
            form.add(new JLabel("나이"), c);
            c.gridx = 1;
            form.add(spAge, c);
            row++;

            add(form, BorderLayout.CENTER);

            // 버튼 영역
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.setOpaque(false); // 배경색 적용을 위해 투명하게 설정
            JButton btnOk = new JButton("저장");
            JButton btnCancel = new JButton("취소");
            
            // 버튼 디자인 적용
            btnOk.setBackground(color4);
            btnOk.setForeground(Color.WHITE);
            btnCancel.setBackground(subtleBorder);
            
            buttons.add(btnCancel);
            buttons.add(btnOk);
            add(buttons, BorderLayout.SOUTH);

            initFields();

            btnCancel.addActionListener(e -> dispose());
            btnOk.addActionListener(e -> {
                applyToUser();
                try {
                    String token = mainApp.getJwtToken();
                    String json = buildUpdateJson(user);
                    String path = "/users/" + user.getId();
                    ApiClient.HttpResult res = ApiClient.put(path, json, token);

                    if (!res.isOk()) {
                        JOptionPane.showMessageDialog(this,
                                "서버 저장 실패: " + res.code + "\n" + res.body);
                    } else {
                        JOptionPane.showMessageDialog(this, "프로필이 저장되었습니다.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "서버 오류: " + ex.getMessage());
                }

                dispose();
            });

            pack();
            setResizable(false);
        }

        // User -> 폼 채우기 (기존 로직 유지)
        private void initFields() {
             tfId.setText(user.getId());
             
             tfUserName.setText(user.getUserName());

             if (user.getMbti() != null && !user.getMbti().isEmpty()) {
                 StringBuilder sb = new StringBuilder();
                 String[] keys = {"EI", "SN", "TF", "JP"};
                 for (String k : keys) {
                     String v = user.getMbti().get(k);
                     if (v != null) sb.append(v);
                 }
                 tfMbti.setText(sb.toString());
             }

             String g = user.getGender();
             if (g != null && g.toLowerCase().startsWith("m"))
                 cbGender.setSelectedItem("남자");
             else if (g != null && g.toLowerCase().startsWith("f"))
                 cbGender.setSelectedItem("여자");

             if (user.getAge() != null)
                 spAge.setValue(user.getAge());
        }

        // 폼 -> User 반영 (기존 로직 유지)
        private void applyToUser() {
             user.setUserName(tfUserName.getText().trim());
             
             String genderKor = (String) cbGender.getSelectedItem();
             if ("남자".equals(genderKor)) user.setGender("m");
             else if ("여자".equals(genderKor)) user.setGender("f");

             user.setAge((Integer) spAge.getValue());

             String mbtiStr = tfMbti.getText().trim().toUpperCase();
             if (mbtiStr.length() == 4) {
                 Map<String, String> mbtiMap = user.getMbti();
                 if (mbtiMap == null) mbtiMap = new HashMap<>();

                 mbtiMap.put("EI", String.valueOf(mbtiStr.charAt(0)));
                 mbtiMap.put("SN", String.valueOf(mbtiStr.charAt(1)));
                 mbtiMap.put("TF", String.valueOf(mbtiStr.charAt(2)));
                 mbtiMap.put("JP", String.valueOf(mbtiStr.charAt(3)));

                 user.setMbti(mbtiMap);
             }
        }

        // User -> 서버로 보낼 JSON 문자열 (기존 로직 유지)
        private String buildUpdateJson(User u) {
             StringBuilder sb = new StringBuilder();
             sb.append("{");
             
          // userName (닉네임)
             sb.append("\"userName\":\"")
             .append(u.getUserName() == null ? "" : u.getUserName())
             .append("\",");

             // gender
             sb.append("\"gender\":\"")
               .append(u.getGender() == null ? "" : u.getGender())
               .append("\",");

             // age
             if (u.getAge() == null) {
                 sb.append("\"age\":null,");
             } else {
                 sb.append("\"age\":").append(u.getAge()).append(",");
             }

             // mbti
             Map<String, String> mbti = u.getMbti();
             if (mbti == null) mbti = new HashMap<>();

             sb.append("\"mbti\":{");
             sb.append("\"EI\":\"").append(mbti.getOrDefault("EI", "")).append("\",");
             sb.append("\"SN\":\"").append(mbti.getOrDefault("SN", "")).append("\",");
             sb.append("\"TF\":\"").append(mbti.getOrDefault("TF", "")).append("\",");
             sb.append("\"JP\":\"").append(mbti.getOrDefault("JP", "")).append("\"");
             sb.append("}");

             sb.append("}");
             return sb.toString();
        }
    }
}