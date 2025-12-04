package com.mbtidating.view;

import com.mbtidating.network.ApiClient;
import com.mbtidating.network.ApiClient.HttpResult;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.regex.Pattern;

public class SignupView extends JPanel {

	private final MainApp mainApp;

	private final Color color1 = new Color(255, 189, 189);
	private final Color color2 = new Color(189, 255, 243);
	private final Color color3 = new Color(213, 201, 255);

	private final JButton sideLogin = new JButton("로그인");
	private final JButton sideSignup = new JButton("회원가입");

	// 아바타 선택 (1~5)
	private int selectedAvatar = 1; // 기본값 1번
	private JButton[] avatarButtons = new JButton[5];

	// 입력 필드
	private final JTextField tfId = new JTextField(20);
	private final JTextField tfUserName = new JTextField(20);
	private final JPasswordField tfPw = new JPasswordField(20);
	private final JComboBox<String> cbMBTI = new JComboBox<>(MBTI_ALL);
	private final JRadioButton rbF = new JRadioButton("여");
	private final JRadioButton rbM = new JRadioButton("남");
	private final JRadioButton rbO = new JRadioButton("기타");
	private final JSpinner spAge = new JSpinner(new SpinnerNumberModel(20, 18, 80, 1));
	private final JButton btnSubmit = new JButton("가입하기");

	// 에러 라벨
	private final JLabel lblIdError = new JLabel(" ");
	private final JLabel lblUserNameError = new JLabel(" ");
	private final JLabel lblPwError = new JLabel(" ");
	private final JLabel lblMbtiError = new JLabel(" ");
	private final JLabel lblGenderError = new JLabel(" ");

	private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,20}$");

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

	    // 1) 회원가입 제목
	    JLabel title = new JLabel("회원가입");
	    title.setFont(new Font("맑은 고딕", Font.BOLD, 26));
	    title.setAlignmentX(Component.CENTER_ALIGNMENT);
	    p.add(title);
	    p.add(Box.createVerticalStrut(20));

	    // 🔥🔥 2) 캐릭터 선택을 최상단으로 끌어올림
	    buildAvatarSelector().setAlignmentX(Component.LEFT_ALIGNMENT);

	    p.add(buildAvatarSelector()); 
	    p.add(Box.createVerticalStrut(5));

	   
	    p.add(Box.createVerticalStrut(20));

	    // 3) 입력 폼들
	    p.add(row("아이디", tfId));
	    p.add(errorLabel(lblIdError));

	    p.add(row("닉네임", tfUserName));
	    p.add(errorLabel(lblUserNameError));

	    p.add(row("비밀번호", tfPw));
	    p.add(errorLabel(lblPwError));

	    p.add(row("MBTI", cbMBTI));
	    p.add(errorLabel(lblMbtiError));

	    // 4) 성별
	    ButtonGroup g = new ButtonGroup();
	    g.add(rbF);
	    g.add(rbM);
	    g.add(rbO);

	    JPanel genderPanel = new JPanel();
	    genderPanel.setOpaque(false);
	    genderPanel.add(rbF);
	    genderPanel.add(rbM);
	    genderPanel.add(rbO);

	    p.add(row("성별", genderPanel));
	    p.add(errorLabel(lblGenderError));

	    p.add(row("나이", spAge));

	    p.add(Box.createVerticalStrut(20));

	    // 5) 가입 버튼
	    styleOval(btnSubmit);
	    btnSubmit.setBackground(color2);
	    btnSubmit.setAlignmentX(Component.CENTER_ALIGNMENT);
	    p.add(btnSubmit);

	    attachValidationEvents();
	    btnSubmit.addActionListener(e -> doSignup());

	    return p;
	}
	
	//동그란 버튼
	class RoundedToggleButton extends JToggleButton {
	    public RoundedToggleButton(ImageIcon icon) {
	        super(icon);
	        setOpaque(false);
	    }

	    @Override
	    protected void paintComponent(Graphics g) {
	        Graphics2D g2 = (Graphics2D) g.create();
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	        // 배경 색
	        g2.setColor(getBackground());
	        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

	        super.paintComponent(g);
	        g2.dispose();
	    }

	    @Override
	    protected void paintBorder(Graphics g) {
	        Graphics2D g2 = (Graphics2D) g.create();
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	        g2.setColor(getBorder() instanceof LineBorder lb ? lb.getLineColor() : Color.GRAY);
	        g2.setStroke(new BasicStroke(2f));
	        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 20, 20);

	        g2.dispose();
	    }
	}


	
	//캐릭터 선택창
	private JPanel buildAvatarSelector() {

	    // 전체 컨테이너 (수직)
	    JPanel wrapper = new JPanel();
	    wrapper.setOpaque(false);
	    wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
	    wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);

	    // 🔥 1) "캐릭터 선택" 텍스트 (위로 이동 + 더 강조)
	    JLabel label = new JLabel("캐릭터 선택");
	    label.setFont(new Font("맑은 고딕", Font.BOLD, 15));
	    label.setForeground(new Color(60, 60, 60));
	    label.setAlignmentX(Component.CENTER_ALIGNMENT);
	    wrapper.add(label);
	    wrapper.add(Box.createVerticalStrut(10));

	    // 2) 캐릭터 아이콘들을 담을 영역 (가운데 정렬)
	    JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
	    row.setOpaque(false);

	    ButtonGroup group = new ButtonGroup();

	    Color highlightBorder = new Color(255, 128, 128);   // 선택된 색(연핑크)
	    Color highlightBG     = new Color(255, 240, 240);

	    for (int i = 1; i <= 5; i++) {
	        final int num = i;

	        String path = "/images/profile" + num + ".png";
	        ImageIcon icon;
	        java.net.URL url = getClass().getResource(path);

	        if (url != null) {
	            Image img = new ImageIcon(url).getImage()
	                    .getScaledInstance(70, 70, Image.SCALE_SMOOTH);
	            icon = new ImageIcon(img);
	        } else {
	            icon = new ImageIcon();
	        }

	        // 캐릭터 버튼
	        JToggleButton btn = new RoundedToggleButton(icon);
	        btn.setPreferredSize(new Dimension(80, 80));
	        btn.setFocusPainted(false);
	        btn.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
	        btn.setBackground(Color.WHITE);
	        btn.setContentAreaFilled(true);

	        // 초기 선택 (1번)
	        if (i == 1) {
	            btn.setSelected(true);
	            btn.setBorder(new LineBorder(highlightBorder, 3));
	            btn.setBackground(highlightBG);
	        }

	        // 클릭 시 색 변경
	        btn.addActionListener(e -> {
	            selectedAvatar = num;

	            for (Component c : row.getComponents()) {
	                if (c instanceof JToggleButton b) {
	                    if (b == btn) {
	                        b.setBorder(new LineBorder(highlightBorder, 3));
	                        b.setBackground(highlightBG);
	                    } else {
	                        b.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
	                        b.setBackground(Color.WHITE);
	                    }
	                }
	            }
	        });

	        group.add(btn);
	        row.add(btn);
	    }

	    wrapper.add(row);
	    wrapper.add(Box.createVerticalStrut(5));

	    return wrapper;
	}






	// --- 에러 라벨 한 줄 ---
	private JPanel errorLabel(JLabel lbl) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setOpaque(false);
		lbl.setForeground(Color.RED);
		lbl.setFont(lbl.getFont().deriveFont(11f));
		p.add(lbl);
		return p;
	}

	// ---- 실시간 유효성 검사 이벤트 등록 ----
	private void attachValidationEvents() {

		tfId.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				validateId();
			}
		});

		tfUserName.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				validateUserName();
			}
		});

		tfPw.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				validatePw();
			}
		});

		cbMBTI.addActionListener(e -> validateMbti());

		rbF.addActionListener(e -> validateGender());
		rbM.addActionListener(e -> validateGender());
		rbO.addActionListener(e -> validateGender());
	}

	// ---- 개별 유효성 ----
	private boolean validateId() {
		String id = tfId.getText().trim();
		if (id.isEmpty()) {
			lblIdError.setText("아이디를 입력해주세요.");
			return false;
		}
		if (!ID_PATTERN.matcher(id).matches()) {
			lblIdError.setText("ID 형식이 올바르지 않습니다. (영문/숫자/_, 4~20자)");
			return false;
		}
		lblIdError.setText(" ");
		return true;
	}

	private boolean validateUserName() {
		String s = tfUserName.getText().trim();
		if (s.isEmpty()) {
			lblUserNameError.setText("닉네임을 입력해주세요.");
			return false;
		}
		if (s.length() > 20) {
			lblUserNameError.setText("닉네임은 20자 이하이어야 합니다.");
			return false;
		}
		lblUserNameError.setText(" ");
		return true;
	}

	private boolean validatePw() {
		String pw = new String(tfPw.getPassword());
		if (pw.isEmpty()) {
			lblPwError.setText("비밀번호를 입력해주세요.");
			return false;
		}
		if (pw.length() < 8) {
			lblPwError.setText("비밀번호는 8자 이상이어야 합니다.");
			return false;
		}
		lblPwError.setText(" ");
		return true;
	}

	private boolean validateMbti() {
		if (cbMBTI.getSelectedItem() == null) {
			lblMbtiError.setText("MBTI를 선택해주세요.");
			return false;
		}
		lblMbtiError.setText(" ");
		return true;
	}

	private boolean validateGender() {
		if (!rbF.isSelected() && !rbM.isSelected() && !rbO.isSelected()) {
			lblGenderError.setText("성별을 선택해주세요.");
			return false;
		}
		lblGenderError.setText(" ");
		return true;
	}

	// ---- 전체 검사 ----
	private boolean validateAll() {
		boolean ok = true;
		if (!validateId())
			ok = false;
		if (!validateUserName())
			ok = false;
		if (!validatePw())
			ok = false;
		if (!validateMbti())
			ok = false;
		if (!validateGender())
			ok = false;
		return ok;
	}

	private void doSignup() {

		if (!validateAll()) {
			JOptionPane.showMessageDialog(this, "입력값을 다시 확인해주세요.");
			return;
		}

		String id = tfId.getText().trim();
		String userName = tfUserName.getText().trim();
		String pw = new String(tfPw.getPassword());
		String mbti = (String) cbMBTI.getSelectedItem();
		String genderVal = rbF.isSelected() ? "f" : rbM.isSelected() ? "m" : "o";
		int age = (Integer) spAge.getValue();

		String json = String.format(
		        "{" +
		            "\"id\":\"%s\"," +
		            "\"userName\":\"%s\"," +
		            "\"pwd\":\"%s\"," +
		            "\"gender\":\"%s\"," +
		            "\"age\":%d," +
		            "\"mbti\":\"%s\"," +
		            "\"profileImg\":\"%d\"" +
		        "}",
		        escape(id),
		        escape(userName),
		        escape(pw),
		        genderVal,
		        age,
		        escape(mbti),
		        selectedAvatar   // 🔥 회원가입 시 선택한 아바타 번호
		);


		try {
			HttpResult res = ApiClient.post("/api/users", json);

			if (res.isOk()) {
				JOptionPane.showMessageDialog(this, "회원가입 성공! 로그인 화면으로 이동합니다.");
				mainApp.showView(MainApp.LOGIN);

			} else if (res.code == 409) {
				// 🔹 서버에서 온 메시지에 따라 구분해서 출력
				String body = res.body != null ? res.body : "";

				if (body.contains("아이디")) {
					JOptionPane.showMessageDialog(this, "중복된 아이디입니다.");
				} else if (body.contains("닉네임")) {
					JOptionPane.showMessageDialog(this, "중복된 닉네임입니다.");
				} else {
					// 혹시 예상 못 한 메시지이면 원래 바디도 같이 보여주기
					JOptionPane.showMessageDialog(this, "중복된 값이 있습니다.\n" + body);
				}

			} else {
				JOptionPane.showMessageDialog(this, "회원가입 실패 (" + res.code + ")");
			}

		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "서버 오류: " + ex.getMessage());
		}
	}

	// --- 기존 row 메소드 그대로 사용 ---
	private JPanel row(String label, JComponent field) {
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.setOpaque(false);
		JLabel l = new JLabel(label + " : ");
		l.setPreferredSize(new Dimension(60, 28));
		p.add(l);
		p.add(field);
		return p;
	}

	// --- 공통 UI ---
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
		p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 0, 1, new Color(200, 200, 200)),
				new EmptyBorder(20, 16, 20, 16)));
		return p;
	}

	private void styleSide(JButton b) {
		b.setFocusPainted(false);
		b.setContentAreaFilled(false);
		b.setOpaque(true);
		b.setBorder(
				new CompoundBorder(new LineBorder(new Color(200, 200, 200), 1, true), new EmptyBorder(10, 18, 10, 18)));
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}

	private void styleOval(JButton b) {
		b.setFocusPainted(false);
		b.setContentAreaFilled(false);
		b.setOpaque(true);
		b.setBorder(
				new CompoundBorder(new LineBorder(new Color(180, 180, 180), 1, true), new EmptyBorder(8, 20, 8, 20)));
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}
	
	

	private static String escape(String s) {
		return s.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static final String[] MBTI_ALL = { "INTJ", "INTP", "INFJ", "INFP", "ISTJ", "ISFJ", "ISTP", "ISFP", "ENTJ",
			"ENTP", "ENFJ", "ENFP", "ESTJ", "ESFJ", "ESTP", "ESFP" };
}
