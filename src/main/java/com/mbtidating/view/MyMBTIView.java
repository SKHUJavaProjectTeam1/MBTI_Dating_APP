package com.mbtidating.view;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.util.function.Consumer;

public class MyMBTIView extends JPanel {

    private final MainApp mainApp;
    private final Color SelecColor = new Color(255, 189, 189); // 분홍색

    private static final String[] QUESTIONS = {
            "당신은 어떤 성격입니까? (I / E)",
            "당신은 어떤 성향입니까? (S / N)",
            "당신은 어떤 소통을 선호하나요? (F / T)",
            "당신은 어떤 생활방식을 선호하나요? (J / P)"
    };
    private static final String[][] CHOICES = {
            {"내향적인 성격", "외향적인 성격"},
            {"현실적인 성향", "직관적인 성향"},
            {"공감하는 소통", "논리적인 소통"},
            {"계획적인 생활방식", "즉흥적인 생활방식"}
    };
    private static final String[][] FACETS = {
            {"I", "E"}, {"S", "N"}, {"F", "T"}, {"J", "P"}
    };

    private final ButtonGroup[] groups = new ButtonGroup[4];
    private final JToggleButton[] leftBtns = new JToggleButton[4];
    private final JToggleButton[] rightBtns = new JToggleButton[4];
    private final JButton saveBtn = new JButton("저장");

    public MyMBTIView(MainApp mainApp) {
        this.mainApp = mainApp;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 20, 12, 20));
        setBackground(Color.WHITE);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JLabel title = new JLabel("나의 성향을 선택해주세요!", SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(18, 0, 6, 0));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        return title;
    }

    private JComponent buildCenter() {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBorder(new EmptyBorder(12, 12, 12, 12));
        box.setOpaque(false);

        for (int i = 0; i < 4; i++) {
            box.add(questionBlock(i));
            if (i < 3)
                box.add(Box.createVerticalStrut(12));
        }
        return new JScrollPane(box) {{
            setBorder(null);
            getViewport().setBackground(Color.WHITE);
        }};
    }

    private JComponent questionBlock(int idx) {
        JLabel q = new JLabel("• " + QUESTIONS[idx], SwingConstants.LEFT);
        q.setBorder(new EmptyBorder(4, 4, 8, 4));
        q.setFont(q.getFont().deriveFont(Font.BOLD));

        leftBtns[idx] = makeChoiceButton(CHOICES[idx][0]);
        rightBtns[idx] = makeChoiceButton(CHOICES[idx][1]);

        groups[idx] = new ButtonGroup();
        groups[idx].add(leftBtns[idx]);
        groups[idx].add(rightBtns[idx]);

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 160));
        sep.setForeground(new Color(210, 210, 210));

        JPanel centerRow = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridy = 0;
        gc.fill = GridBagConstraints.BOTH;
        gc.weighty = 1.0;

        gc.gridx = 0; gc.weightx = 1.0;
        centerRow.add(wrap(leftBtns[idx]), gc);
        gc.gridx = 1; gc.weightx = 0.0;
        centerRow.add(wrap(sep), gc);
        gc.gridx = 2; gc.weightx = 1.0;
        centerRow.add(wrap(rightBtns[idx]), gc);

        JPanel block = new JPanel(new BorderLayout());
        block.setBorder(new CompoundBorder(
                new LineBorder(new Color(145, 78, 78), 1, true),
                new EmptyBorder(8, 8, 12, 8)
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
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(26, 20, 26, 20)
        ));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setPreferredSize(new Dimension(300, 160));
        b.setUI(new BasicButtonUI());

        b.addActionListener(e -> {
            if (b.isSelected())
                b.setBackground(SelecColor);
            saveBtn.setEnabled(allAnswered());
        });
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

            // ✅ MBTI 결과 표시 및 다음 화면 이동
            JOptionPane.showMessageDialog(this, "당신의 MBTI는 " + code + " 입니다!");
            mainApp.showView(MainApp.HOME);
        });

        cancel.addActionListener(e -> mainApp.showView(MainApp.HOME));

        bottom.add(saveBtn);
        bottom.add(cancel);
        return bottom;
    }

    private boolean allAnswered() {
        for (ButtonGroup g : groups) {
            if (g.getSelection() == null)
                return false;
        }
        return true;
    }

    private void styleBtn(JButton b, boolean primary) {
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setBackground(primary ? new Color(245, 245, 245) : Color.WHITE);
        b.setBorder(new CompoundBorder(
                new LineBorder(new Color(190, 190, 190), 1, true),
                new EmptyBorder(8, 18, 8, 18)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
