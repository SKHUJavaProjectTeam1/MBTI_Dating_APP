package com.mbtidating.view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MBTIInformView extends JPanel {

    private final MainApp mainApp;

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

    public MBTIInformView(MainApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        add(header(), BorderLayout.NORTH);
        add(body(), BorderLayout.CENTER);

        showType("ENFP");
    }

    private JComponent header() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);

        JLabel t = new JLabel("MBTI 소개", SwingConstants.LEFT);
        t.setBorder(new EmptyBorder(16, 20, 8, 20));
        t.setFont(t.getFont().deriveFont(Font.BOLD, 22f));
        header.add(t, BorderLayout.WEST);

        // 닫기(홈으로)
        JButton backBtn = new JButton("홈으로");
        backBtn.setFocusPainted(false);
        backBtn.setBackground(new Color(245, 245, 245));
        backBtn.setBorder(new CompoundBorder(
                new LineBorder(new Color(190, 190, 190), 1, true),
                new EmptyBorder(8, 16, 8, 16)
        ));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> mainApp.showView(MainApp.HOME));

        header.add(backBtn, BorderLayout.EAST);
        return header;
    }

    private JComponent body() {
        JPanel grid = new JPanel(new GridLayout(4, 4, 12, 12));
        grid.setBorder(new EmptyBorder(0, 20, 0, 12));
        grid.setBackground(new Color(255, 189, 189));

        ButtonGroup g = new ButtonGroup();
        int row = 0, col = 0, columns = 4;

        Color[] rowColors = {
                new Color(198, 166, 232),
                new Color(151, 223, 113),
                new Color(140, 235, 216),
                new Color(245, 223, 99)
        };

        for (String type : INFO.keySet()) {
            JToggleButton b = makeTypeButton(type);
            int currentRow = row;
            b.setBackground(rowColors[currentRow]);
            b.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI());

            b.addActionListener(e -> {
                // 강조 효과
                for (AbstractButton btn : java.util.Collections.list(g.getElements())) {
                    if (btn.isSelected()) {
                        btn.setBackground(new Color(205, 96, 165));
                    } else {
                        int idx = java.util.Collections.list(g.getElements()).indexOf(btn);
                        int r = idx / columns;
                        btn.setBackground(rowColors[r]);
                    }
                }
                showType(type);
            });

            g.add(b);
            grid.add(b);

            col++;
            if (col == columns) {
                col = 0;
                row++;
            }
        }

        JPanel detail = new JPanel(new BorderLayout());
        detail.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(20, 24, 24, 24)
        ));
        detail.setBackground(new Color(189, 255, 243));

        bigType.setFont(bigType.getFont().deriveFont(Font.BOLD, 34f));
        bigType.setBorder(new EmptyBorder(0, 0, 12, 0));

        desc.setEditable(false);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        desc.setFont(desc.getFont().deriveFont(16f));
        desc.setBorder(new EmptyBorder(8, 4, 0, 4));

        detail.add(bigType, BorderLayout.NORTH);
        detail.add(new JScrollPane(desc) {{
            setBorder(null);
            getViewport().setBackground(new Color(189, 255, 243));
        }}, BorderLayout.CENTER);

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
                new LineBorder(new Color(210, 210, 210), 1, true),
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
}
