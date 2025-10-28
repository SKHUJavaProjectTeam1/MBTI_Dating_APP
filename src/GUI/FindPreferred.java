package GUI;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.function.Consumer;

public class FindPreferred extends JFrame {

    // 질문/선택 텍스트
    private static final String[] QUESTIONS = {
            "어떤 상대를 원하시나요? (I / E)",
            "어떤 성향을 선호하나요? (S / N)",
            "어떤 소통을 선호하나요? (F / T)",
            "어떤 생활방식을 선호하나요? (J / P)"
    };
    private static final String[][] CHOICES = {
            {"내향적인 사람", "외향적인 사람"},   // I / E
            {"현실적인 사람", "직관적인 사람"},   // S / N
            {"공감해주는 사람", "논리적인 사람"}, // F / T
            {"계획적인 사람", "즉흥적인 사람"}    // J / P
    };
    // 각 문항의 왼/오 선택이 의미하는 글자
    private static final String[][] FACETS = {
            {"I", "E"}, {"S", "N"}, {"F", "T"}, {"J", "P"}
    };

    private final ButtonGroup[] groups = new ButtonGroup[4];
    private final JToggleButton[] leftBtns = new JToggleButton[4];
    private final JToggleButton[] rightBtns = new JToggleButton[4];
    private final JButton saveBtn = new JButton("저장");

    private final Consumer<String> onSelected; // 결과 콜백 (예: selected -> state.preferredType = selected)

    public FindPreferred(Consumer<String> onSelected) {
        super("선호 상대 찾기");
        this.onSelected = onSelected;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 640);
        setLocationRelativeTo(null);
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JLabel title = new JLabel("원하는 상대의 성향을 선택해주세요!", SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(18, 0, 6, 0));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        return title;
    }

    private JComponent buildCenter() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(12, 20, 12, 20));
        box.setOpaque(false);

        for (int i = 0; i < 4; i++) {
            box.add(questionBlock(i));
            if (i < 3) box.add(Box.createVerticalStrut(12));
        }
        return new JScrollPane(box) {{
            setBorder(null);
        }};
    }

    private JComponent questionBlock(int idx) {
        // 문항 라벨
        JLabel q = new JLabel("• " + QUESTIONS[idx], SwingConstants.LEFT);
        q.setBorder(new EmptyBorder(4, 4, 8, 4));
        q.setFont(q.getFont().deriveFont(Font.BOLD));

        // 좌/우 큰 토글 버튼
        leftBtns[idx]  = makeChoiceButton(CHOICES[idx][0]);
        rightBtns[idx] = makeChoiceButton(CHOICES[idx][1]);

        // 그룹화(한 문항당 하나만 선택)
        groups[idx] = new ButtonGroup();
        groups[idx].add(leftBtns[idx]);
        groups[idx].add(rightBtns[idx]);

        // 가운데 세로 구분선
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 160));
        sep.setForeground(new Color(210,210,210));

        JPanel centerRow = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0; gc.fill = GridBagConstraints.BOTH; gc.weighty = 1.0;

        gc.gridx = 0; gc.weightx = 1.0; centerRow.add(wrap(leftBtns[idx]), gc);
        gc.gridx = 1; gc.weightx = 0.0; centerRow.add(wrap(new JPanel(new GridBagLayout()) {{ add(sep); }}), gc);
        gc.gridx = 2; gc.weightx = 1.0; centerRow.add(wrap(rightBtns[idx]), gc);

        JPanel block = new JPanel();
        block.setLayout(new BorderLayout());
        block.setBorder(new CompoundBorder(
                new LineBorder(new Color(230,230,230), 1, true),
                new EmptyBorder(8,8,12,8)
        ));
        block.add(q, BorderLayout.NORTH);
        block.add(centerRow, BorderLayout.CENTER);

        return block;
    }

    private JToggleButton makeChoiceButton(String text) {
        JToggleButton b = new JToggleButton(text);
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 18f));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(Color.WHITE);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
                new LineBorder(new Color(200,200,200), 1, true),
                new EmptyBorder(26, 20, 26, 20)
        ));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setPreferredSize(new Dimension(300, 160));
        b.addActionListener(e -> saveBtn.setEnabled(allAnswered()));
        return b;
    }

    private JPanel wrap(JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(0, 12, 0, 12));
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    private JComponent buildFooter() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 12));
        JButton cancel = new JButton("닫기");
        styleBtn(saveBtn, true);
        styleBtn(cancel, false);
        saveBtn.setEnabled(false);

        saveBtn.addActionListener(e -> {
            if (!allAnswered()) {
                JOptionPane.showMessageDialog(this, "모든 문항을 선택해 주세요.");
                return;
            }
            StringBuilder code = new StringBuilder(4);
            for (int i = 0; i < 4; i++) {
                boolean left = leftBtns[i].isSelected();
                code.append(left ? FACETS[i][0] : FACETS[i][1]);
            }
            if (onSelected != null) onSelected.accept(code.toString()); // 예: "INFJ"
            dispose();
        });
        cancel.addActionListener(e -> dispose());

        bottom.add(saveBtn);
        bottom.add(cancel);
        return bottom;
    }

    private boolean allAnswered() {
        for (ButtonGroup g : groups) {
            if (g.getSelection() == null) return false;
        }
        return true;
    }

    private void styleBtn(JButton b, boolean primary) {
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setBackground(primary ? new Color(245,245,245) : Color.WHITE);
        b.setBorder(new CompoundBorder(
                new LineBorder(new Color(190,190,190), 1, true),
                new EmptyBorder(8,18,8,18)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        if (primary) b.setText("저장");
    }

    // 단독 테스트용 main
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new FindPreferred(selected -> System.out.println("선택 결과: " + selected))
                .setVisible(true)
        );
    }
}
