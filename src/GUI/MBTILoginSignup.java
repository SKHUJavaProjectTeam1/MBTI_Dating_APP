package GUI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.net.URL;

public class MBTILoginSignup extends JFrame {

    // 왼쪽 버튼들
    private final JButton sideLogin = new JButton("로그인");
    private final JButton sideSignup = new JButton("회원가입");

    // 오른쪽 카드 영역
    private final JPanel contentCards = new JPanel(new CardLayout());
    private static final String CARD_LOGIN = "CARD_LOGIN";
    private static final String CARD_SIGNUP = "CARD_SIGNUP";

    public MBTILoginSignup() {
        super("MBTI MATCH - Login / Signup");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 620);
        setLocationRelativeTo(null);
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        setLayout(new BorderLayout());
        add(buildLeftSidebar(), BorderLayout.WEST);

        // 오른쪽 카드: 로그인/회원가입 화면
        contentCards.add(new LoginContent(), CARD_LOGIN);
        contentCards.add(new SignupContent(), CARD_SIGNUP);
        add(contentCards, BorderLayout.CENTER);

        // 초기 화면: 로그인
        showCard(CARD_LOGIN);

        // 사이드 버튼 동작
        sideLogin.addActionListener(e -> showCard(CARD_LOGIN));
        sideSignup.addActionListener(e -> showCard(CARD_SIGNUP));
    }

    private void showCard(String key) {
        ((CardLayout) contentCards.getLayout()).show(contentCards, key);
    }

    // =============== 왼쪽 사이드바 ===============
    private JComponent buildLeftSidebar() {
        JPanel left = new JPanel();
        left.setPreferredSize(new Dimension(180, 0));
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(new CompoundBorder(
                new MatteBorder(0,0,0,1, new Color(210,210,210)),
                new EmptyBorder(20, 16, 20, 16)
        ));
        left.setBackground(new Color(247,247,247));

        styleOval(sideLogin);
        styleOval(sideSignup);

        left.add(Box.createVerticalGlue());
        left.add(sideLogin);
        left.add(Box.createVerticalStrut(12));
        left.add(sideSignup);
        left.add(Box.createVerticalGlue());
        return left;
    }

    // =============== 오른쪽: 로그인 카드 ===============
    class LoginContent extends JPanel {
        private final JTextField tfId = new JTextField(20);
        private final JPasswordField tfPw = new JPasswordField(20);
        private final JButton btnLogin = new JButton("로그인하기");

        LoginContent() {
            setLayout(new GridBagLayout());
            setBorder(new EmptyBorder(0, 40, 0, 40));
            setBackground(new Color(250,250,250));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(10, 10, 10, 10);
            gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
            gc.anchor = GridBagConstraints.CENTER;

            JLabel title = new JLabel("LOGIN");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
            add(title, gc);

            gc.gridy++; gc.fill = GridBagConstraints.HORIZONTAL;
            tfId.setPreferredSize(new Dimension(420, 40));
            tfId.setBorder(inputBorder());
            add(tfId, gc);

            gc.gridy++;
            tfPw.setPreferredSize(new Dimension(420, 40));
            tfPw.setBorder(inputBorder());
            add(tfPw, gc);

            gc.gridy++; gc.gridwidth = 1; gc.fill = GridBagConstraints.NONE; gc.anchor = GridBagConstraints.EAST;
            styleSmall(btnLogin);
            add(btnLogin, gc);

            // 데모 동작
            btnLogin.addActionListener(e -> {
                String id = tfId.getText().trim();
                String pw = new String(tfPw.getPassword());
                if (id.isEmpty() || pw.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "ID와 PW를 입력하세요.");
                    return;
                }
                // TODO: 실제 검증/API 호출

                // 홈 화면으로 전환
                new MBTIHome().setVisible(true);
                SwingUtilities.getWindowAncestor(this).dispose();
            });

        }
    }

    // =============== 오른쪽: 회원가입 카드 ===============
    class SignupContent extends JPanel {
        private final JComboBox<String> cbMBTI = new JComboBox<>(MBTI_ALL);
        private final JTextField tfName = new JTextField(18);
        private final JRadioButton rbF = new JRadioButton("여");
        private final JRadioButton rbM = new JRadioButton("남");
        private final JRadioButton rbO = new JRadioButton("기타");
        private final JSpinner spAge = new JSpinner(new SpinnerNumberModel(20, 18, 80, 1));
        private final JTextField tfRegion = new JTextField(18);
        private final JButton btnSubmit = new JButton("가입하기");

        SignupContent() {
            setLayout(new BorderLayout());
            setBackground(new Color(250,250,250));
            setBorder(new EmptyBorder(0, 32, 0, 32));

            // 상단 타이틀
            JLabel title = new JLabel("회원가입");
            title.setBorder(new EmptyBorder(20, 0, 16, 0));
            title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
            title.setHorizontalAlignment(SwingConstants.LEFT);
            add(title, BorderLayout.NORTH);

            // 본문(왼 타이틀/오른 폼 레이아웃을 간단하게 한 열로 구성)
            JPanel form = new JPanel();
            form.setOpaque(false);
            form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
            form.setBorder(new EmptyBorder(8, 40, 8, 40));

            // 아바타 이미지 (원형 마크 없음)
            JLabel avatar = avatarLabel("images/default_profile.png", 72);
            JPanel avatarWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            avatarWrap.setOpaque(false);
            avatarWrap.add(avatar);
            form.add(avatarWrap);
            form.add(Box.createVerticalStrut(12));

            // MBTI
            cbMBTI.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            cbMBTI.setBorder(inputBorder());
            form.add(row("MBTI", cbMBTI));

            // 이름
            tfName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            tfName.setBorder(inputBorder());
            form.add(row("이름", tfName));

            // 성별 + 나이
            ButtonGroup g = new ButtonGroup(); g.add(rbF); g.add(rbM); g.add(rbO);
            rbF.setOpaque(false); rbM.setOpaque(false); rbO.setOpaque(false);
            JPanel genderAge = new JPanel();
            genderAge.setOpaque(false);
            genderAge.setLayout(new BoxLayout(genderAge, BoxLayout.X_AXIS));
            genderAge.add(rbF); genderAge.add(Box.createHorizontalStrut(8));
            genderAge.add(rbM); genderAge.add(Box.createHorizontalStrut(8));
            genderAge.add(rbO); genderAge.add(Box.createHorizontalStrut(16));
            genderAge.add(new JLabel("나이 "));
            spAge.setMaximumSize(new Dimension(80, 32));
            genderAge.add(spAge);
            form.add(row("성별", genderAge));

            // 지역
            tfRegion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
            tfRegion.setBorder(inputBorder());
            form.add(row("지역", tfRegion));

            // 가입하기 버튼(가운데)
            JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 16));
            btnWrap.setOpaque(false);
            styleOval(btnSubmit);
            btnWrap.add(btnSubmit);
            form.add(Box.createVerticalStrut(8));
            form.add(btnWrap);

            add(form, BorderLayout.CENTER);

            // 데모 동작
            btnSubmit.addActionListener(e -> {
                String name = tfName.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "이름을 입력하세요.");
                    return;
                }
                // TODO: 실제 회원가입 처리(API 호출)

                // 가입 후 바로 홈 화면으로 전환
                new MBTIHome().setVisible(true);
                SwingUtilities.getWindowAncestor(this).dispose();
            });

        }

        private JPanel row(String label, JComponent field) {
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
            JLabel l = new JLabel(label + "  |  ");
            l.setPreferredSize(new Dimension(52, 28)); // 스케치 느낌의 왼쪽 라벨
            p.add(l);
            p.add(Box.createHorizontalStrut(6));
            p.add(field);
            p.add(Box.createVerticalStrut(8));
            p.setBorder(new EmptyBorder(6, 0, 6, 0));
            return p;
        }
    }

    // =============== 공통 스타일/헬퍼 ===============
    private void styleOval(JButton b) {
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setBorder(new CompoundBorder(
                new LineBorder(new Color(200,200,200), 1, true),
                new EmptyBorder(10, 18, 10, 18)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSmall(JButton b) {
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(Color.WHITE);
        b.setBorder(new CompoundBorder(
                new LineBorder(new Color(190,190,190), 1, true),
                new EmptyBorder(6, 10, 6, 10)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private Border inputBorder() {
        return new CompoundBorder(
                new LineBorder(new Color(200,200,200), 1, true),
                new EmptyBorder(8,10,8,10)
        );
    }

    private JLabel avatarLabel(String pathOrClasspath, int size) {
        Image img;
        URL url = getClass().getResource(pathOrClasspath); // 클래스패스 우선
        if (url != null) img = new ImageIcon(url).getImage();
        else             img = new ImageIcon(pathOrClasspath).getImage(); // 파일 경로
        Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        JLabel label = new JLabel(new ImageIcon(scaled));
        label.setPreferredSize(new Dimension(size, size));
        label.setBorder(new CompoundBorder(
                new LineBorder(new Color(180,180,180), 1, true),
                new EmptyBorder(2,2,2,2)
        ));
        return label;
    }

    // MBTI 목록
    private static final String[] MBTI_ALL = {
    		"INTJ","INTP","INFJ","INFP","ISTJ","ISFJ","ISTP","ISFP",
    		"ENTJ","ENTP","ENFJ","ENFP","ESTJ","ESFJ","ESTP","ESFP"
    };

    // 실행 진입점
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MBTILoginSignup().setVisible(true));
    }
}
