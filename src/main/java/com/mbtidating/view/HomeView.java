package com.mbtidating.view;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class HomeView extends JPanel {

    private final MainApp mainApp;

    // 색상
    private final Color color1 = new Color(245, 235, 235);
    private final Color color2 = new Color(255, 189, 189);
    private final Color color3 = new Color(255, 199, 226);
    private final Color color4 = new Color(213, 201, 255);

    public HomeView(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(color1);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    // 상단: 타이틀 + 내비
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                new EmptyBorder(8, 16, 8, 16)));

        JLabel title = new JLabel("MBTI   MATCH", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        header.add(title, BorderLayout.NORTH);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(145, 78, 78));
                int y = getHeight() - 1;
                g.drawLine(0, y, getWidth(), y);
            }
        };

        // 버튼
        JButton btnHome = createNavButton("HOME");
        JButton btnFind = createNavButton("선호 상대 찾기");
        JButton btnMyMBTI = createNavButton("나의 MBTI");
        JButton btnGuide = createNavButton("MBTI 소개");
        JButton btnChat = createNavButton("채팅");
        JButton btnLogout = createNavButton("로그아웃");
        JButton btnMatch = createNavButton("매칭하기");

        nav.add(btnHome);
        nav.add(separator());
        nav.add(btnFind);
        nav.add(separator());
        nav.add(btnMatch);
        nav.add(separator());
        nav.add(btnMyMBTI);
        nav.add(separator());
        nav.add(btnGuide);
        nav.add(separator());
        nav.add(btnChat);
        nav.add(separator());
        nav.add(btnLogout);
        nav.setBackground(color1);

        header.add(nav, BorderLayout.SOUTH);
        header.setBackground(color1);

        // 버튼 동작 연결
        btnLogout.addActionListener(e -> mainApp.showView(MainApp.LOGIN));
        btnGuide.addActionListener(e -> mainApp.showView(MainApp.MBTI_INFO));
        btnFind.addActionListener(e -> JOptionPane.showMessageDialog(this, "선호 상대 찾기 (추후 구현)"));
        btnMyMBTI.addActionListener(e -> mainApp.showView(MainApp.MYMBTI));
        btnChat.addActionListener(e -> {
            mainApp.showView(MainApp.CHAT);
            String token = mainApp.getJwtToken();
            if (token == null || token.isEmpty()) {
                JOptionPane.showMessageDialog(this, "로그인이 필요합니다.");
                mainApp.showView(MainApp.LOGIN);
                return;
            }
            mainApp.getChatView().startChat("상대방", token);
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
        return b;
    }

    private Component separator() {
        JLabel s = new JLabel(" | ");
        s.setBorder(new EmptyBorder(0, 4, 0, 4));
        return s;
    }

    // 본문 구성
    private JComponent buildBody() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(color1);

        JPanel left = new InfoPanel();
        left.setPreferredSize(new Dimension(260, 0));

        JPanel center = new JPanel(new BorderLayout());
        JLabel recTitle = new JLabel("추천 상대");
        recTitle.setFont(recTitle.getFont().deriveFont(Font.BOLD, 16f));
        recTitle.setBorder(new EmptyBorder(0, 8, 8, 0));
        center.add(recTitle, BorderLayout.NORTH);
        center.add(recommendGrid(), BorderLayout.CENTER);
        center.setBackground(color1);

        JPanel right = chatPanel();
        right.setPreferredSize(new Dimension(360, 0));

        JPanel middle = new JPanel(new BorderLayout(16, 0));
        middle.add(left, BorderLayout.WEST);
        middle.add(center, BorderLayout.CENTER);
        middle.add(right, BorderLayout.EAST);
        middle.setBackground(color1);
        root.add(middle, BorderLayout.CENTER);
        return root;
    }

    private JComponent recommendGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 4, 16, 16));

        for (int i = 0; i < 8; i++) {
            ProfileCard card = new ProfileCard();  // 카드 객체 생성
            card.setProfile(
                "유저" + (i + 1),
                "INFP",
                (i % 2 == 0 ? "남성" : "여성"),
                20 + i,
                "서울"
            );
            grid.add(card);  // 그리드에 추가
        }

        return grid;
    }



    private JPanel chatPanel() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(12, 12, 12, 12)));
        wrap.setBackground(color3);

        JLabel h = new JLabel("최근 채팅");
        h.setFont(h.getFont().deriveFont(Font.BOLD));
        h.setBorder(new EmptyBorder(0, 0, 8, 0));
        wrap.add(h, BorderLayout.NORTH);

        BubbleArea bubbles = new BubbleArea();
        bubbles.addLeft("안녕하세요!");

        JScrollPane sp = new JScrollPane(bubbles);
        sp.setBorder(null);
        wrap.add(sp, BorderLayout.CENTER);

        JTextField input = new JTextField();
        JButton send = new JButton("➤");
        send.setPreferredSize(new Dimension(48, 36));
        send.setBackground(color3);

        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setBorder(new EmptyBorder(8, 0, 0, 0));
        bottom.setBackground(color3);
        bottom.add(input, BorderLayout.CENTER);
        bottom.add(send, BorderLayout.EAST);

        wrap.add(bottom, BorderLayout.SOUTH);
        return wrap;
    }

    class InfoPanel extends JPanel {
        InfoPanel() {
            setLayout(new BorderLayout());
            setBorder(new CompoundBorder(new LineBorder(new Color(220, 220, 220), 1, true),
                    new EmptyBorder(16, 16, 16, 16)));
            setBackground(color2);

            JLabel title = new JLabel("내 정보");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            title.setBorder(new EmptyBorder(0, 0, 8, 0));
            add(title, BorderLayout.NORTH);

            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBackground(color2);

            JLabel avatar = avatarLabel("images/default_profile.png", 90);
            avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
            body.add(avatar);
            body.add(Box.createVerticalStrut(16));

            body.add(infoLine("MBTI"));
            body.add(infoLine("이름"));
            body.add(infoLine("성별"));
            body.add(infoLine("나이"));
            body.add(infoLine("직업"));
            body.add(Box.createVerticalStrut(12));

            JButton edit = new JButton("수정하기");
            edit.setBackground(color4);
            edit.setBorderPainted(false);
            edit.setAlignmentX(Component.CENTER_ALIGNMENT);
            body.add(edit);

            add(body, BorderLayout.CENTER);
        }

        private JComponent infoLine(String label) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            JLabel l = new JLabel(label + "  ");
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(200, 200, 200));
            p.add(l, BorderLayout.WEST);
            p.add(sep, BorderLayout.CENTER);
            p.setBorder(new EmptyBorder(4, 0, 4, 0));
            return p;
        }
    }

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
        label.setBorder(new CompoundBorder(new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(2, 2, 2, 2)));
        return label;
    }

    public static class ProfileCard extends JPanel {

	    private JLabel photoLabel;
	    private JLabel mbtiLabel;
	    private JLabel genderAgeLabel;
	    private JLabel regionLabel;
	    private JButton chatButton;

	    public ProfileCard() {
	    	
	    	
	        setLayout(new BorderLayout());
	        setBackground(new Color(235, 255, 255));
	        setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));

	        // 상단 사진
	        photoLabel = new JLabel();
	        photoLabel.setPreferredSize(new Dimension(120, 120));
	        photoLabel.setHorizontalAlignment(JLabel.CENTER);
	        photoLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	        add(photoLabel, BorderLayout.NORTH);

	        // 가운데 정보 패널
	        JPanel infoPanel = new JPanel();
	        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
	        infoPanel.setBackground(new Color(235, 255, 255));
	        infoPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

	        mbtiLabel = new JLabel("MBTI: -");
	        genderAgeLabel = new JLabel("성별/나이: -");
	        regionLabel = new JLabel("지역: -");

	        mbtiLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
	        genderAgeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
	        regionLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

	        infoPanel.add(mbtiLabel);
	        infoPanel.add(genderAgeLabel);
	        infoPanel.add(regionLabel);
	        add(infoPanel, BorderLayout.CENTER);

	        // 하단 버튼
	     // 하단 버튼
	     // 하단 버튼
	        chatButton = new JButton("채팅하기");
	        chatButton.setFocusPainted(false);

	        // 미니멀 기본 스타일
	        chatButton.setOpaque(true);
	        chatButton.setBorder(BorderFactory.createLineBorder(new Color(120, 210, 190), 2));
	        chatButton.setBackground(Color.WHITE);
	        chatButton.setForeground(new Color(120, 210, 190));  // 글자색
	        chatButton.setFont(new Font("SansSerif", Font.BOLD, 13));

	        add(chatButton, BorderLayout.SOUTH);

	        // Hover 효과 (깔끔하게)
	        chatButton.addMouseListener(new java.awt.event.MouseAdapter() {
	            @Override
	            public void mouseEntered(java.awt.event.MouseEvent e) {
	                chatButton.setBackground(new Color(120, 210, 190));
	                chatButton.setForeground(Color.WHITE);
	            }

	            @Override
	            public void mouseExited(java.awt.event.MouseEvent e) {
	                chatButton.setBackground(Color.WHITE);
	                chatButton.setForeground(new Color(120, 210, 190));
	            }
	        });


	        }

	    
	    public ProfileCard(String title) {
	        this(); // 기본 생성자 먼저 호출됨

	        // title을 쓰고 싶다면 아래처럼 label 추가 가능
	        // JLabel titleLabel = new JLabel(title, JLabel.CENTER);
	        // add(titleLabel, BorderLayout.NORTH);
	    }
	    
	    public void setProfile(String userName, String mbti, String gender, Integer age, String region) {

	        String genderAgeText = gender;
	        if (age != null && age > 0) {
	            genderAgeText += " / " + age;
	        }

	        mbtiLabel.setText("MBTI: " + mbti);
	        genderAgeLabel.setText("성별/나이: " + genderAgeText);
	        regionLabel.setText("지역: " + region);

	        // ★★ 캐릭터 이미지를 리사이즈해서 넣기 ★★
	        try {
	            // 임시 캐릭터 번호 (원하면 DB에서 값 받아서 활용 가능)
	        	int imgNum = 1;

	        	String path = "/images/profile" + imgNum + ".png";

	        	System.out.println("이미지 경로 확인 시도: " + path);
	        	URL imgURL = getClass().getResource(path);
	        	System.out.println("로드된 URL: " + imgURL);

	        	if (imgURL != null) {
	        	    ImageIcon origin = new ImageIcon(imgURL);
	        	    Image scaled = origin.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
	        	    photoLabel.setIcon(new ImageIcon(scaled));
	        	} else {
	        	    System.out.println("❌ 이미지 로드 실패: " + path);
	        	}

	        } catch (Exception e) {
	            System.out.println("이미지 처리 중 오류 발생");
	            e.printStackTrace();
	        }
	    }





	    // 데이터 설정 메서드
	    public void setUserData(String mbti, String genderAge, String region, ImageIcon photo) {
	        photoLabel.setIcon(photo);
	        mbtiLabel.setText("MBTI: " + mbti);
	        genderAgeLabel.setText("성별/나이: " + genderAge);
	        regionLabel.setText("지역: " + region);
	    }
	}


    static class BubbleArea extends JPanel {
        static class Msg {
            String text;
            boolean right;
            Msg(String t, boolean r) { text = t; right = r; }
        }

        private final List<Msg> msgs = new ArrayList<>();

        BubbleArea() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(new Color(255, 199, 226));
        }

        void addLeft(String t) { addMsg(new Msg(t, false)); }

        private void addMsg(Msg m) {
            msgs.add(m);
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            Bubble b = new Bubble(m.text, m.right);
            row.add(b, BorderLayout.WEST);
            row.setBorder(new EmptyBorder(6, 6, 6, 6));
            add(row);
        }
    }

    static class Bubble extends JComponent {
        private final String text;
        private final boolean right;
        Bubble(String text, boolean right) {
            this.text = text;
            this.right = right;
            setPreferredSize(new Dimension(220, 46 + (text.length() / 16) * 18));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            Shape r = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, 18, 18);
            g2.setColor(right ? new Color(220, 245, 255) : new Color(245, 245, 245));
            g2.fill(r);
            g2.setColor(new Color(210, 210, 210));
            g2.draw(r);
            g2.setColor(Color.DARK_GRAY);
            FontMetrics fm = g2.getFontMetrics();
            int pad = 10, y = pad + fm.getAscent(), lineW = w - pad * 2;
            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String candidate = (line.length() == 0 ? word : line + " " + word);
                if (fm.stringWidth(candidate) > lineW) {
                    g2.drawString(line.toString(), pad, y);
                    line = new StringBuilder(word);
                    y += fm.getHeight();
                } else
                    line = new StringBuilder(candidate);
            }
            g2.drawString(line.toString(), pad, y);
            g2.dispose();
        }
    }
}
