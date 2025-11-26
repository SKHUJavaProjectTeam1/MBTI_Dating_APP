package com.mbtidating.view;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.*;

import com.mbtidating.dto.User;
import com.mbtidating.network.ApiClient;
import org.json.JSONObject;

public class LoginView extends JPanel {

    private final MainApp mainApp;

    private final Color color1 = new Color(255, 189, 189);
    private final Color color2 = new Color(189, 255, 243);
    private final Color color3 = new Color(213, 201, 255);

    private final JButton sideLogin = new JButton("로그인");
    private final JButton sideSignup = new JButton("회원가입");

    private final JTextField tfId = new JTextField(20);
    private final JPasswordField tfPw = new JPasswordField(20);
    private final JButton btnLogin = new JButton("로그인하기");

    public LoginView(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel side = gradientPanel();
        styleSide(sideLogin);
        styleSide(sideSignup);

        sideLogin.setBackground(color3);
        sideSignup.setBackground(color3);

        side.add(Box.createVerticalGlue());
        side.add(sideLogin);
        side.add(Box.createVerticalStrut(12));
        side.add(sideSignup);
        side.add(Box.createVerticalGlue());

        sideSignup.addActionListener(e -> mainApp.showView(MainApp.SIGNUP));
        return side;
    }

    private JPanel buildContent() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(250, 250, 250));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.gridx = 0; gc.gridy = 0;

        JLabel title = new JLabel("LOGIN");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 30));
        p.add(title, gc);

        gc.gridy++;
        tfId.setPreferredSize(new Dimension(420, 40));
        tfId.setBorder(inputBorder());
        p.add(tfId, gc);

        gc.gridy++;
        tfPw.setPreferredSize(new Dimension(420, 40));
        tfPw.setBorder(inputBorder());
        p.add(tfPw, gc);

        gc.gridy++;
        styleOval(btnLogin);
        btnLogin.setBackground(color1);
        p.add(btnLogin, gc);

        btnLogin.addActionListener(e -> doLogin());
        return p;
    }

    private void doLogin() {
        String id = tfId.getText().trim();
        String pw = new String(tfPw.getPassword());

        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID와 PW를 입력하세요.");
            return;
        }

        try {
            // JSON 요청 생성
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("id", id);
            jsonObj.put("pwd", pw);
            String json = jsonObj.toString();

            ApiClient.HttpResult res = ApiClient.postJson("http://localhost:8080/api/users/login", json);

            if (res.isOk()) {
                String token = extractAccessToken(res.body);

                if (token == null || token.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "로그인 응답에 토큰이 없습니다.");
                    return;
                }

                mainApp.setJwtToken(token);

                User user = parseUser(res.body);
                mainApp.setLoggedInUser(user);

                JOptionPane.showMessageDialog(this, "로그인 성공!");
                mainApp.showView(MainApp.HOME);

            } else {
                JOptionPane.showMessageDialog(this, "로그인 실패 (" + res.code + ")");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "서버 오류: " + ex.getMessage());
        }
    }

    // 🔹 JSON에서 access 토큰 추출
    private String extractAccessToken(String body) {
        try {
            JSONObject jsonObj = new JSONObject(body);
            return jsonObj.optString("access", "");
        } catch (Exception e) {
            return "";
        }
    }

    // 🔹 로그인 응답(JSON)에서 id / gender / age / mbti 추출
    private User parseUser(String body) {
        User u = new User();

        try {
            JSONObject jsonObj = new JSONObject(body);
            u.setId(jsonObj.optString("id", ""));
            u.setGender(jsonObj.optString("gender", ""));
            u.setAge(jsonObj.optInt("age", 0));

            Map<String, String> mbtiMap = new HashMap<>();
            if (jsonObj.has("EI")) mbtiMap.put("EI", jsonObj.getString("EI"));
            if (jsonObj.has("SN")) mbtiMap.put("SN", jsonObj.getString("SN"));
            if (jsonObj.has("TF")) mbtiMap.put("TF", jsonObj.getString("TF"));
            if (jsonObj.has("JP")) mbtiMap.put("JP", jsonObj.getString("JP"));
            if (!mbtiMap.isEmpty()) {
                u.setMbti(mbtiMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return u;
    }

    // ----- UI 헬퍼 -----
    private JPanel gradientPanel() {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, color1, 0, getHeight(), color2));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        p.setPreferredSize(new Dimension(180, 0));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, new Color(200, 200, 200)),
                new EmptyBorder(20, 16, 20, 16)));
        return p;
    }

    private void styleSide(JButton b) {
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(10, 18, 10, 18)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleOval(JButton b) {
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
                new LineBorder(new Color(180, 180, 180), 1, true),
                new EmptyBorder(8, 20, 8, 20)));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private Border inputBorder() {
        return new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(8, 10, 8, 10));
    }
}
