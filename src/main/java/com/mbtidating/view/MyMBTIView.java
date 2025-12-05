package com.mbtidating.view;

import com.mbtidating.dto.User;
import com.mbtidating.network.ApiClient;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MyMBTIView extends JPanel {

    private final MainApp mainApp;

    // 🎨 컬러 팔레트
    private final Color BG_LAVENDER = new Color(248, 245, 255);     // 전체 배경 연보라
    private final Color CARD_BG     = new Color(255, 250, 254);     // 질문 카드 배경
    private final Color CARD_BORDER = new Color(210, 180, 230);     // 카드 테두리 라벤더
    private final Color BTN_NORMAL  = new Color(255, 245, 248);     // 선택지 기본 배경 (로즈쿼츠 톤)
    private final Color BTN_SELECTED= new Color(242, 210, 255);     // 선택된 버튼 배경 (연보라 + 핑크)
    private final Color BTN_BORDER  = new Color(205, 175, 220);     // 선택 박스 테두리
    private final Color TITLE_COLOR = new Color(120, 90, 150);

    private static final String[] QUESTIONS = {
            "어떤 환경에서 더 편안함과 활력을 느끼나요?",
            "당신은 어떤 사고방식을 지향하나요?",
            "당신은 어떤 소통을 선호하나요?",
            "당신은 어떤 생활방식을 선호하나요?"
    };
    private static final String[][] CHOICES = {
            {"차분한 개인공간(I)", "활기있는 사회적 환경(E)"},
            {"현실적인 사고방식(S)", "추상적인 사고방식(N)"},
            {"공감하는 소통(F)", "논리적인 소통(T)"},
            {"계획적인 생활방식(J)", "즉흥적인 생활방식(P)"}
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
        setBackground(BG_LAVENDER);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JLabel title = new JLabel("나의 성향을 선택해주세요!", SwingConstants.CENTER);
        title.setBorder(new EmptyBorder(18, 0, 10, 0));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(TITLE_COLOR);
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
                box.add(Box.createVerticalStrut(16));
        }

        JScrollPane sp = new JScrollPane(box);
        sp.setBorder(null);
        sp.getViewport().setBackground(BG_LAVENDER);
        sp.setBackground(BG_LAVENDER);
        return sp;
    }

    private JComponent questionBlock(int idx) {
        // 질문 라벨 (가운데 정렬 + 폰트 크게)
        JLabel q = new JLabel(QUESTIONS[idx], SwingConstants.CENTER);
        q.setBorder(new EmptyBorder(6, 4, 10, 4));
        q.setFont(q.getFont().deriveFont(Font.BOLD, 20f));
        q.setForeground(TITLE_COLOR);

        leftBtns[idx] = makeChoiceButton(CHOICES[idx][0]);
        rightBtns[idx] = makeChoiceButton(CHOICES[idx][1]);

        // 기본 배경 색
        leftBtns[idx].setBackground(BTN_NORMAL);
        rightBtns[idx].setBackground(BTN_NORMAL);

        groups[idx] = new ButtonGroup();
        groups[idx].add(leftBtns[idx]);
        groups[idx].add(rightBtns[idx]);

        // 가운데 세로 구분선
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 160));
        sep.setForeground(new Color(215, 205, 230));

        JPanel centerRow = new JPanel(new GridBagLayout());
        centerRow.setOpaque(false);
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

        // 각 쌍별로 배경색 동기화 + 저장 버튼 활성화 체크
        final int index = idx;
        leftBtns[idx].addActionListener(e -> updateSelectionColors(index));
        rightBtns[idx].addActionListener(e -> updateSelectionColors(index));

        JPanel block = new JPanel(new BorderLayout());
        block.setBorder(new CompoundBorder(
                new LineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(8, 8, 12, 8)
        ));
        block.setBackground(CARD_BG);
        block.setOpaque(true);

        block.add(q, BorderLayout.NORTH);
        block.add(centerRow, BorderLayout.CENTER);
        return block;
    }

    private void updateSelectionColors(int idx) {
        JToggleButton left = leftBtns[idx];
        JToggleButton right = rightBtns[idx];

        if (left.isSelected()) {
            left.setBackground(BTN_SELECTED);
        } else {
            left.setBackground(BTN_NORMAL);
        }

        if (right.isSelected()) {
            right.setBackground(BTN_SELECTED);
        } else {
            right.setBackground(BTN_NORMAL);
        }

        saveBtn.setEnabled(allAnswered());
    }

    private JToggleButton makeChoiceButton(String text) {
        // 둥근 모서리 커스텀 토글 버튼
        JToggleButton b = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int arc = 28;

                // 배경
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);

                // 테두리
                g2.setColor(BTN_BORDER);
                g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);

                g2.dispose();

                // 텍스트/아이콘은 기본 LAF로 그리게
                super.paintComponent(g);
            }
        };

        b.setFont(b.getFont().deriveFont(Font.PLAIN, 18f));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(26, 20, 26, 20));
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setPreferredSize(new Dimension(300, 160));
        b.setUI(new BasicButtonUI());

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
        bottom.setOpaque(false);

        JButton cancel = new JButton("닫기");
        styleBtn(saveBtn, true);
        styleBtn(cancel, false);
        saveBtn.setEnabled(false);

        saveBtn.addActionListener(e -> {
            if (!allAnswered()) {
                JOptionPane.showMessageDialog(this, "모든 문항을 선택해 주세요.");
                return;
            }

            // 1) MBTI 코드 생성
            StringBuilder code = new StringBuilder(4);
            for (int i = 0; i < 4; i++) {
                boolean left = leftBtns[i].isSelected();
                code.append(left ? FACETS[i][0] : FACETS[i][1]);
            }

            String mbtiResult = code.toString();   // EX) "INFJ"

            // 2) 현재 로그인 유저 가져오기
            User user = mainApp.getLoggedInUser();
            if (user == null) {
                JOptionPane.showMessageDialog(this, "로그인 정보가 없습니다.");
                return;
            }

            // 3) 문자열 MBTI → Map 변환
            Map<String, String> mbtiMap = new HashMap<>();
            mbtiMap.put("EI", "" + mbtiResult.charAt(0));
            mbtiMap.put("SN", "" + mbtiResult.charAt(1));
            mbtiMap.put("TF", "" + mbtiResult.charAt(2));
            mbtiMap.put("JP", "" + mbtiResult.charAt(3));

            user.setMbti(mbtiMap);

            // 4) 서버로 업데이트 요청
            try {
                String token = mainApp.getJwtToken();

                JSONObject json = new JSONObject();
                json.put("userName", user.getUserName());
                json.put("gender", user.getGender());
                json.put("age", user.getAge());
                json.put("profileImg", user.getProfileImg());
                json.put("mbti", new JSONObject(mbtiMap));

                ApiClient.HttpResult res =
                        ApiClient.put("/api/users/" + user.getId(), json.toString(), token);

                if (!res.isOk()) {
                    JOptionPane.showMessageDialog(this, "서버 저장 실패: " + res.body);
                } else {
                    JOptionPane.showMessageDialog(this, "당신의 MBTI는 " + mbtiResult + " 입니다!\n프로필에 반영되었습니다.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "서버 오류: " + ex.getMessage());
            }

            // 5) HomeView로 이동하여 화면 업데이트
            mainApp.showView(MainApp.HOME);
            mainApp.getHomeView().updateUserInfo(user);  // ← 사용자 정보 패널 즉시 갱신
        });


        cancel.addActionListener(e -> mainApp.showView(MainApp.HOME));

        bottom.add(saveBtn);
        bottom.add(cancel);
        return bottom;
    }

    private boolean allAnswered() {
        for (ButtonGroup g : groups) {
            if (g == null || g.getSelection() == null)
                return false;
        }
        return true;
    }

    private void styleBtn(JButton b, boolean primary) {
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setBackground(primary ? new Color(255, 220, 235) : Color.WHITE);
        b.setForeground(new Color(80, 60, 100));
        b.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 180, 210), 1, true),
                new EmptyBorder(8, 18, 8, 18)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
