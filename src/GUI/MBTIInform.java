package GUI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MBTIInform extends JFrame {

    // 오른쪽 카드(이미지 없음)
    private final JLabel bigType = new JLabel("", SwingConstants.CENTER);
    private final JTextArea desc = new JTextArea();

    // 간단 설명 데이터
    private static final Map<String, String> INFO = new LinkedHashMap<>();
    static {
        INFO.put("INTJ", "전략가형 • 통찰력, 계획적, 독립적");
        INFO.put("INTP", "사색가형 • 분석적, 호기심, 유연한 사고");
        INFO.put("ENTJ", "통솔자형 • 리더십, 목표지향, 결단력");
        INFO.put("ENTP", "변론가형 • 창의, 도전적, 토론 즐김");
        INFO.put("INFJ", "옹호자형 • 통찰, 배려, 신념 강함");
        INFO.put("INFP", "중재자형 • 이상가, 공감, 가치지향");
        INFO.put("ENFJ", "선도자형 • 사교적, 조정능력, 배려");
        INFO.put("ENFP", "활동가형 • 열정, 아이디어, 자유로움");
        INFO.put("ISTJ", "현실주의자 • 책임감, 성실, 신중");
        INFO.put("ISFJ", "수호자형 • 헌신, 세심, 안정추구");
        INFO.put("ESTJ", "관리자형 • 조직적, 실용, 원칙적");
        INFO.put("ESFJ", "집정관형 • 협력, 친절, 조화중시");
        INFO.put("ISTP", "장인형 • 실용적, 침착, 문제해결");
        INFO.put("ISFP", "모험가형 • 유연, 감성, 즉흥적");
        INFO.put("ESTP", "사업가형 • 활동적, 현실감각, 도전");
        INFO.put("ESFP", "연예인형 • 에너지, 사교, 즐거움");
    }

    public MBTIInform() {
        super("MBTI 소개");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        setLayout(new BorderLayout());
        add(header(), BorderLayout.NORTH);
        add(body(), BorderLayout.CENTER);

        // 초기 표시
        showType("ENFP");
    }

    private JComponent header() {
        JLabel t = new JLabel("MBTI 소개", SwingConstants.LEFT);
        t.setBorder(new EmptyBorder(16, 20, 8, 20));
        t.setFont(t.getFont().deriveFont(Font.BOLD, 22f));
        return t;
    }

    private JComponent body() {
        // 왼쪽 4×4 타입 버튼
        JPanel grid = new JPanel(new GridLayout(4, 4, 12, 12));
        grid.setBorder(new EmptyBorder(0, 20, 0, 12));
        ButtonGroup g = new ButtonGroup();
        for (String type : INFO.keySet()) {
            JToggleButton b = makeTypeButton(type);
            g.add(b);
            grid.add(b);
        }

        // 오른쪽 상세 카드
        JPanel detail = new JPanel(new BorderLayout());
        detail.setBorder(new CompoundBorder(
                new LineBorder(new Color(220,220,220), 1, true),
                new EmptyBorder(20, 24, 24, 24)
        ));
        bigType.setFont(bigType.getFont().deriveFont(Font.BOLD, 34f));
        bigType.setBorder(new EmptyBorder(0, 0, 12, 0));

        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFont(desc.getFont().deriveFont(16f));
        desc.setBorder(new EmptyBorder(8, 4, 0, 4));

        detail.add(bigType, BorderLayout.NORTH);
        detail.add(new JScrollPane(desc) {{ setBorder(null); }}, BorderLayout.CENTER);
        detail.setPreferredSize(new Dimension(420, 0));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.add(grid, BorderLayout.CENTER);
        wrap.add(detail, BorderLayout.EAST);
        return wrap;
    }

    private JToggleButton makeTypeButton(String type) {
        JToggleButton b = new JToggleButton(type);
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
                new LineBorder(new Color(210,210,210), 1, true),
                new EmptyBorder(12, 8, 12, 8)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> showType(type));
        return b;
    }

    private void showType(String type) {
        bigType.setText(type);
        String text = INFO.getOrDefault(type, "");
        desc.setText("""
                %s

                • 강점
                  - 상황 판단과 의사결정에서 %s 유형의 장점이 돋보입니다.

                • 팁
                  - 다른 성향을 존중하고 소통 방식을 맞추면 궁합이 좋아집니다.
                """.formatted(text, type));
    }

    // 단독 테스트
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MBTIInform().setVisible(true));
    }
}
