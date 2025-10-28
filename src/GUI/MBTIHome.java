package GUI;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

public class MBTIHome extends JFrame {

    public MBTIHome() {
        super("MBTI MATCH - Home");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setLocationRelativeTo(null);
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    // 상단: 타이틀 + 내비
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new CompoundBorder(
                new MatteBorder(0,0,1,0, new Color(220,220,220)),
                new EmptyBorder(8,16,8,16)
        ));

        JLabel title = new JLabel("MBTI   MATCH", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        header.add(title, BorderLayout.NORTH);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));

        // ▼ 버튼들을 변수로 받아서 리스너를 달 수 있게 함
        JButton btnHome   = createNavButton("        HOME        ");
        JButton btnFind   = createNavButton("        선호 상대 찾기        ");
        JButton btnMyMBTI     = createNavButton("        나의 MBTI        ");
        JButton btnGuide  = createNavButton("        MBTI 소개        ");
        JButton btnChat   = createNavButton("        채팅        ");
        JButton btnLogout = createNavButton("        로그아웃        ");

        nav.add(btnHome);  nav.add(separator());
        nav.add(btnFind);  nav.add(separator());
        nav.add(btnMyMBTI);    nav.add(separator());
        nav.add(btnGuide); nav.add(separator());
        nav.add(btnChat);  nav.add(separator());
        nav.add(btnLogout);

        header.add(nav, BorderLayout.SOUTH);

        //  “상대 찾기” 리스너: 4문항 화면 띄우기
        btnFind.addActionListener(e -> {
            new FindPreferred(selectedMbti -> {
                // TODO: 선택값 저장/서버 전송 등
                System.out.println("선호유형 = " + selectedMbti);
            }).setVisible(true);
        });

        // "나의 MBTI 찾기" 리스너: 4문항 화면 띄우기
        btnMyMBTI.addActionListener(e -> { 
        	new MyMBTI(selectedMbti -> {
        		System.out.println("나의 MBTI = " + selectedMbti);
        	}).setVisible(true);
        });
        
     // "MBTI 소개" 리스너: 16가지 MBTI 
        btnGuide.addActionListener(e -> { 
        	new MBTIInform().setVisible(true);
        });
        
        
     // "채팅" 리스너: 채팅 기능
        //btnChat.addActionListener(e -> { .. });
        
        
     // “로그아웃” 리스너: 프로그램 종료
        btnLogout.addActionListener(e -> {
            // 바로 종료
            System.exit(0);
        });

        return header;
    }

    private JButton createNavButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6,10,6,10));
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private Component separator() {
        JLabel s = new JLabel(" | ");
        s.setBorder(new EmptyBorder(0,4,0,4));
        return s;
    }

    // 본문: 좌측 내 정보 / 중앙 추천 그리드 / 우측 최근 채팅
    private JComponent buildBody() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(new EmptyBorder(12,12,12,12));

        JPanel left = new InfoPanel(); left.setPreferredSize(new Dimension(260, 0));
        JPanel center = new JPanel(new BorderLayout());
        JLabel recTitle = new JLabel("추천 상대");
        recTitle.setFont(recTitle.getFont().deriveFont(Font.BOLD, 16f));
        recTitle.setBorder(new EmptyBorder(0,8,8,0));
        center.add(recTitle, BorderLayout.NORTH);
        center.add(recommendGrid(), BorderLayout.CENTER);

        JPanel right = chatPanel(); right.setPreferredSize(new Dimension(360, 0));

        JPanel middle = new JPanel(new BorderLayout(16,0));
        middle.add(left, BorderLayout.WEST);
        middle.add(center, BorderLayout.CENTER);
        middle.add(right, BorderLayout.EAST);

        root.add(middle, BorderLayout.CENTER);
        return root;
    }

    private JComponent recommendGrid() {
        JPanel grid = new JPanel(new GridLayout(2,4,16,16));
        for (int i=0;i<8;i++) grid.add(new ProfileCard("카드 " + (i+1)));
        return grid;
    }

    private JPanel chatPanel() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBorder(new CompoundBorder(new LineBorder(new Color(220,220,220), 1, true),
                                          new EmptyBorder(12,12,12,12)));
        JLabel h = new JLabel("최근 채팅");
        h.setFont(h.getFont().deriveFont(Font.BOLD));
        h.setBorder(new EmptyBorder(0,0,8,0));
        wrap.add(h, BorderLayout.NORTH);

        BubbleArea bubbles = new BubbleArea();
        bubbles.addLeft("안녕하세요!");

        JScrollPane sp = new JScrollPane(bubbles);
        sp.setBorder(null);
        wrap.add(sp, BorderLayout.CENTER);

        JTextField input = new JTextField();
        JButton addBtn = new JButton("+");
        JButton send = new JButton("➤");
        addBtn.setPreferredSize(new Dimension(40, 36));
        send.setPreferredSize(new Dimension(48, 36));

        JPanel bottom = new JPanel(new BorderLayout(8,0));
        bottom.setBorder(new EmptyBorder(8,0,0,0));
        bottom.add(addBtn, BorderLayout.WEST);
        bottom.add(input, BorderLayout.CENTER);
        bottom.add(send, BorderLayout.EAST);

        wrap.add(bottom, BorderLayout.SOUTH);
        return wrap;
    }

    // 좌측 "내 정보" 패널 (이미지 아이콘 사용)
    class InfoPanel extends JPanel {
        InfoPanel() {
            setLayout(new BorderLayout());
            setBorder(new CompoundBorder(new LineBorder(new Color(220,220,220),1,true),
                                         new EmptyBorder(16,16,16,16)));

            JLabel title = new JLabel("내 정보");
            title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
            title.setBorder(new EmptyBorder(0,0,8,0));
            add(title, BorderLayout.NORTH);

            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setOpaque(false);

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
            edit.setAlignmentX(Component.CENTER_ALIGNMENT);
            body.add(edit);

            add(body, BorderLayout.CENTER);
        }

        private JComponent infoLine(String label) {
            JPanel p = new JPanel(new BorderLayout());
            p.setOpaque(false);
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            JLabel l = new JLabel(label + "  ");
            JSeparator sep = new JSeparator();
            sep.setForeground(new Color(200,200,200));
            p.add(l, BorderLayout.WEST);
            p.add(sep, BorderLayout.CENTER);
            p.setBorder(new EmptyBorder(4,0,4,0));
            return p;
        }
    }

    // 이미지 아이콘 로더(스무스 스케일)
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

    // ====== 카드/채팅 말풍선 컴포넌트 ======
    static class ProfileCard extends JPanel {
        ProfileCard(String title) {
            setOpaque(false);
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(150, 170));

            JPanel rect = new JPanel();
            rect.setPreferredSize(new Dimension(120,110));
            rect.setBackground(Color.WHITE);
            rect.setBorder(new CompoundBorder(new LineBorder(new Color(200,200,200),1,true),
                                              new EmptyBorder(6,6,6,6)));

            JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
            titleLbl.setBorder(new EmptyBorder(6,0,0,0));

            JPanel lines = new JPanel();
            lines.setLayout(new BoxLayout(lines, BoxLayout.Y_AXIS));
            lines.setOpaque(false);
            lines.add(smallLine());
            lines.add(smallLine());

            add(rect, BorderLayout.CENTER);
            add(titleLbl, BorderLayout.SOUTH);
            add(lines, BorderLayout.NORTH);
        }
        private JComponent smallLine() {
            JSeparator s = new JSeparator();
            s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
            s.setForeground(new Color(210,210,210));
            return s;
        }
    }

    static class BubbleArea extends JPanel {
        static class Msg { String text; boolean right; Msg(String t, boolean r){text=t; right=r;} }
        private final List<Msg> msgs = new ArrayList<>();
        BubbleArea() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setOpaque(false);
        }
        void addLeft(String t){ addMsg(new Msg(t,false)); }
        void addRight(String t){ addMsg(new Msg(t,true)); }
        private void addMsg(Msg m) {
            msgs.add(m);
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            Bubble b = new Bubble(m.text, m.right);
            if (m.right) row.add(b, BorderLayout.EAST);
            else row.add(b, BorderLayout.WEST);
            row.setBorder(new EmptyBorder(6,6,6,6));
            add(row);
        }
    }

    static class Bubble extends JComponent {
        private final String text;
        private final boolean right;
        Bubble(String text, boolean right) {
            this.text=text; this.right=right;
            setPreferredSize(new Dimension(220, 46 + (text.length()/16)*18));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            Shape r = new RoundRectangle2D.Float(0, 0, w-1, h-1, 18, 18);
            g2.setColor(right ? new Color(220, 245, 255) : new Color(245,245,245));
            g2.fill(r);
            g2.setColor(new Color(210,210,210));
            g2.draw(r);

            g2.setColor(Color.DARK_GRAY);
            FontMetrics fm = g2.getFontMetrics();
            int pad = 10, y = pad + fm.getAscent(), lineW = w - pad*2;
            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String candidate = (line.length()==0 ? word : line + " " + word);
                if (fm.stringWidth(candidate) > lineW) {
                    g2.drawString(line.toString(), pad, y);
                    line = new StringBuilder(word);
                    y += fm.getHeight();
                } else line = new StringBuilder(candidate);
            }
            g2.drawString(line.toString(), pad, y);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MBTIHome().setVisible(true));
    }
}
