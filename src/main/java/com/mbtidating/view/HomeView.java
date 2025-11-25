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

    public void updateUserInfo(User user) {
        if (infoPanel != null) infoPanel.update(user);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
                new EmptyBorder(8, 16, 8, 16)));

        JLabel title = new JLabel("MBTI   MATCH", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        header.add(title, BorderLayout.NORTH);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(145, 78, 78));
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

        btnLogout.addActionListener(e -> mainApp.showView(MainApp.LOGIN));
        btnGuide.addActionListener(e -> mainApp.showView(MainApp.MBTI_INFO));
        btnMyMBTI.addActionListener(e -> mainApp.showView(MainApp.MYMBTI));
        btnChat.addActionListener(e -> {
            String token = mainApp.getJwtToken();
            String userId = mainApp.getLoggedInUserId();   // ← 중요: 반드시 이게 있어야 한다

            if (token == null || token.isEmpty()) {
                JOptionPane.showMessageDialog(this, "로그인이 필요합니다.");
                mainApp.showView(MainApp.LOGIN);
                return;
            }

            try {
                // 1) DB에서 이 유저가 속한 채팅방 목록 조회
                ApiClient.HttpResult res = ApiClient.get("/chat/rooms/" + userId);

                if (!res.isOk()) {
                    JOptionPane.showMessageDialog(this, "서버 연결 오류");
                    return;
                }

                JSONArray arr = new JSONArray(res.body);

                // ---------------------------
                // ❗ 방이 하나라도 있으면 그 방으로 이동
                // ---------------------------
                if (arr.length() > 0) {
                    JSONObject room = arr.getJSONObject(0);
                    String roomId = room.getString("roomId");

                    ChatView chatView = mainApp.getChatView();
                    chatView.startChat(roomId, userId);

                    mainApp.showView(MainApp.CHAT);
                    return;
                }

                // ---------------------------
                // ❗ 방이 없을 때만 "매칭 먼저 하세요" 표시
                // ---------------------------
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
        return b;
    }

    private Component separator() {
        JLabel s = new JLabel(" | ");
        s.setBorder(new EmptyBorder(0, 4, 0, 4));
        return s;
    }
 // ========================== 본문 ==========================
    private JComponent buildBody() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.setBackground(color1);

        infoPanel = new InfoPanel();
        JPanel left = infoPanel;
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
        for (int i = 0; i < 8; i++)
            grid.add(new ProfileCard("카드 " + (i + 1)));
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

    // ========================== 왼쪽 내 정보 패널 ==========================
    class InfoPanel extends JPanel {

        private final JLabel idValue = new JLabel("-");
        private final JLabel mbtiValue = new JLabel("-");
        private final JLabel genderValue = new JLabel("-");
        private final JLabel ageValue = new JLabel("-");

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

            body.add(infoLine("아이디", idValue));
            body.add(infoLine("MBTI", mbtiValue));
            body.add(infoLine("성별", genderValue));
            body.add(infoLine("나이", ageValue));
            body.add(Box.createVerticalStrut(12));

            JButton edit = new JButton("수정하기");
            edit.setBackground(color4);
            edit.setBorderPainted(false);
            edit.setAlignmentX(Component.CENTER_ALIGNMENT);

            edit.addActionListener(e -> {
                System.out.println("[InfoPanel] 수정하기 버튼 클릭됨");

                User user = mainApp.getLoggedInUser();
                if (user == null) {
                    JOptionPane.showMessageDialog(InfoPanel.this, "로그인 정보가 없습니다.");
                    return;
                }

                Window owner = SwingUtilities.getWindowAncestor(HomeView.this);
                ProfileEditDialog dialog = new ProfileEditDialog(owner, user);
                dialog.setLocationRelativeTo(HomeView.this);
                dialog.setVisible(true);

                update(user);
            });

            body.add(edit);

            add(body, BorderLayout.CENTER);
        }

        void update(User user) {
            if (user == null) {
                idValue.setText("-");
                mbtiValue.setText("-");
                genderValue.setText("-");
                ageValue.setText("-");
                return;
            }

            idValue.setText(user.getId());
            mbtiValue.setText(buildMbti(user.getMbti()));
            genderValue.setText(buildGender(user.getGender()));
            ageValue.setText(user.getAge() != null ? user.getAge() + "세" : "-");

            revalidate();
            repaint();
        }

        private JComponent infoLine(String label, JLabel valueLabel) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);

            JLabel l = new JLabel(label + "  ");

            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(200, 200, 200));

            valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            p.add(l, BorderLayout.WEST);
            p.add(sep, BorderLayout.CENTER);
            p.add(valueLabel, BorderLayout.EAST);

            p.setBorder(new EmptyBorder(4, 0, 4, 0));
            return p;
        }

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
        label.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(2, 2, 2, 2)
        ));
        return label;
    }

    // ========================== 추천 카드 ==========================
    static class ProfileCard extends JPanel {
        ProfileCard(String title) {
            setOpaque(false);
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(150, 170));

            JPanel rect = new JPanel();
            rect.setPreferredSize(new Dimension(120, 110));
            rect.setBackground(new Color(189, 255, 243));
            rect.setBorder(new CompoundBorder(
                    new LineBorder(new Color(200, 200, 200), 1, true),
                    new EmptyBorder(6, 6, 6, 6)
            ));

            JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
            titleLbl.setBorder(new EmptyBorder(6, 0, 0, 0));

            add(rect, BorderLayout.CENTER);
            add(titleLbl, BorderLayout.SOUTH);
        }
    }

    // ========================== 채팅 말풍선 영역 ==========================
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

    // ========================== 말풍선 컴포넌트 ==========================
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
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            g2.drawString(line.toString(), pad, y);
            g2.dispose();
        }
    }

    // ========================== 프로필 수정 다이얼로그 ==========================
    class ProfileEditDialog extends JDialog {

        private final User user;

        private final JTextField tfId = new JTextField();
        private final JTextField tfMbti = new JTextField();
        private final JComboBox<String> cbGender =
                new JComboBox<>(new String[]{"남자", "여자"});
        private final JSpinner spAge =
                new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));

        ProfileEditDialog(Window owner, User user) {
            super(owner, "프로필 수정", ModalityType.APPLICATION_MODAL);
            this.user = user;

            setLayout(new BorderLayout(10, 10));
            ((JComponent) getContentPane()).setBorder(new EmptyBorder(12, 12, 12, 12));

            JPanel form = new JPanel(new GridBagLayout());
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
            JButton btnOk = new JButton("저장");
            JButton btnCancel = new JButton("취소");
            buttons.add(btnCancel);
            buttons.add(btnOk);
            add(buttons, BorderLayout.SOUTH);

            // 초기값 채우기
            initFields();

            // 취소
            btnCancel.addActionListener(e -> dispose());

            // 저장
            btnOk.addActionListener(e -> {
                System.out.println("[ProfileEditDialog] 저장 버튼 클릭됨");

                // 1) 폼 값 → User 객체 반영
                applyToUser();
                System.out.println("[ProfileEditDialog] applyToUser() 완료");
                System.out.println("[ProfileEditDialog] user id=" + user.getId()
                        + ", gender=" + user.getGender()
                        + ", age=" + user.getAge()
                        + ", mbti=" + user.getMbti());

                // 2) 서버로 PUT 요청
                try {
                    String token = mainApp.getJwtToken();
                    String json  = buildUpdateJson(user);

                    String path = "/users/" + user.getId();   // 서버: @PutMapping("/api/users/{id}")
                    System.out.println("[ProfileEditDialog] 요청 path=" + path);
                    System.out.println("[ProfileEditDialog] 요청 JSON=" + json);

                    ApiClient.HttpResult res = ApiClient.put(path, json, token);

                    System.out.println("[ProfileEditDialog] 응답 코드=" + res.code);
                    System.out.println("[ProfileEditDialog] 응답 바디=" + res.body);

                    if (!res.isOk()) {
                        JOptionPane.showMessageDialog(this,
                                "서버 저장 실패: " + res.code + "\n" + res.body);
                    } else {
                        JOptionPane.showMessageDialog(this, "프로필이 저장되었습니다.");
                    }

                } catch (Exception ex) {
                    System.out.println("[ProfileEditDialog] 예외 발생: " + ex);
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "서버 오류: " + ex.getMessage());
                }

                dispose();
            });

            pack();
            setResizable(false);
        }

        // User -> 폼 채우기
        private void initFields() {
            tfId.setText(user.getId());

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

        // 폼 -> User 반영
        private void applyToUser() {
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

        // User -> 서버로 보낼 JSON 문자열
        private String buildUpdateJson(User u) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");

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
