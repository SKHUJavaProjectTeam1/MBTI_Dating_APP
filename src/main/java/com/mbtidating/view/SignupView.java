package com.mbtidating.view;

import com.mbtidating.network.ApiClient;
import com.mbtidating.network.ApiClient.HttpResult;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class SignupView extends JPanel {

    private final MainApp mainApp;

    private final Color color1 = new Color(255, 189, 189);
    private final Color color2 = new Color(189, 255, 243);
    private final Color color3 = new Color(213, 201, 255);

    private final JButton sideLogin = new JButton("로그인");
    private final JButton sideSignup = new JButton("회원가입");

    private final JTextField tfId = new JTextField(20);
    private final JPasswordField tfPw = new JPasswordField(20);
    private final JComboBox<String> cbMBTI = new JComboBox<>(MBTI_ALL);
    private final JRadioButton rbF = new JRadioButton("여");
    private final JRadioButton rbM = new JRadioButton("남");
    private final JRadioButton rbO = new JRadioButton("기타");
    private final JSpinner spAge = new JSpinner(new SpinnerNumberModel(20, 18, 80, 1));
    private final JButton btnSubmit = new JButton("가입하기");

    public SignupView(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildForm(), BorderLayout.CENTER);
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

        sideLogin.addActionListener(e -> mainApp.showView(MainApp.LOGIN));
        return side;
    }

    private JPanel buildForm() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(250, 250, 250));
        p.setBorder(new EmptyBorder(40, 60, 40, 60));

        JLabel title = new JLabel("회원가입");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 26));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(title);
        p.add(Box.createVerticalStrut(20));

        p.add(row("아이디", tfId));
        p.add(row("비밀번호", tfPw));
        p.add(row("MBTI", cbMBTI));

        ButtonGroup g = new ButtonGroup();
        g.add(rbF); g.add(rbM); g.add(rbO);
        JPanel gender = new JPanel();
        gender.setOpaque(false);
        gender.add(rbF); gender.add(rbM); gender.add(rbO);
        p.add(row("성별", gender));
        p.add(row("나이", spAge));

        p.add(Box.createVerticalStrut(20));
        styleOval(btnSubmit);
        btnSubmit.setBackground(color2);
        btnSubmit.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(btnSubmit);

        btnSubmit.addActionListener(e -> doSignup());
        return p;
    }

    private void doSignup() {
        String id = tfId.getText().trim();
        String pw = new String(tfPw.getPassword());
        String mbti = (String) cbMBTI.getSelectedItem();
        String genderVal = rbF.isSelected() ? "f" : rbM.isSelected() ? "m" : rbO.isSelected() ? "o" : "";
        int age = (Integer) spAge.getValue();

        if (id.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "아이디와 비밀번호를 입력하세요.");
            return;
        }

        String json = String.format(
                "{\"userName\":\"%s\",\"pwd\":\"%s\",\"gender\":\"%s\",\"age\":%d,\"mbti\":\"%s\"}",
                escape(id), escape(pw), genderVal, age, escape(mbti)
        );

        try {
            HttpResult res = ApiClient.post("/users", json);
            if (res.isOk()) {
                JOptionPane.showMessageDialog(this, "회원가입 성공! 로그인 화면으로 이동합니다.");
                mainApp.showView(MainApp.LOGIN);
            } else {
                JOptionPane.showMessageDialog(this, "회원가입 실패 (" + res.code + ")");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "서버 오류: " + ex.getMessage());
        }
    }

    private JPanel row(String label, JComponent field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setOpaque(false);
        JLabel l = new JLabel(label + " : ");
        l.setPreferredSize(new Dimension(60, 28));
        p.add(l); p.add(field);
        return p;
    }

    // ----- 공통 UI -----
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

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static final String[] MBTI_ALL = {
            "INTJ","INTP","INFJ","INFP","ISTJ","ISFJ","ISTP","ISFP",
            "ENTJ","ENTP","ENFJ","ENFP","ESTJ","ESFJ","ESTP","ESFP"
    };
}
