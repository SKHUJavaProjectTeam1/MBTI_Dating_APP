package com.mbtidating.view;

import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import com.mbtidating.dto.User;
import com.mbtidating.network.ApiClient;
import org.json.JSONArray;
import org.json.JSONObject;

public class HomeView extends JPanel {

	private final MainApp mainApp;
	
	private static final String[] MBTI_TYPES = {
	        "INTJ","INTP","ENTJ","ENTP",
	        "INFJ","INFP","ENFJ","ENFP",
	        "ISTJ","ISFJ","ESTJ","ESFJ",
	        "ISTP","ISFP","ESTP","ESFP"
	    };
	
	private static final Map<String, List<String>> mbtiIdealMatches = new HashMap<>();
	static {
		mbtiIdealMatches.put("INFP", Arrays.asList("ENFJ", "INFJ"));
		mbtiIdealMatches.put("ENFP", Arrays.asList("INFJ", "INTJ"));
		mbtiIdealMatches.put("INTJ", Arrays.asList("ENFP", "ENFJ"));
		mbtiIdealMatches.put("ENTP", Arrays.asList("INFJ", "INTJ"));
		mbtiIdealMatches.put("INFJ", Arrays.asList("ENFP", "ENTP"));
		mbtiIdealMatches.put("ENFJ", Arrays.asList("INFP", "ISFP"));
		mbtiIdealMatches.put("ISFP", Arrays.asList("ENFJ", "ESFJ"));
		mbtiIdealMatches.put("ISTJ", Arrays.asList("ESFP", "ESTP"));
		mbtiIdealMatches.put("ISFJ", Arrays.asList("ESFP", "ISFP"));
		mbtiIdealMatches.put("ESTJ", Arrays.asList("ISFP", "INTP"));
		mbtiIdealMatches.put("ESFJ", Arrays.asList("ISFP", "ISTP"));
		mbtiIdealMatches.put("ISTP", Arrays.asList("ESFJ", "ISFJ"));
		mbtiIdealMatches.put("ESTP", Arrays.asList("ISFJ", "ISTJ"));
		mbtiIdealMatches.put("ESFP", Arrays.asList("ISFJ", "ISTJ"));
		mbtiIdealMatches.put("INTP", Arrays.asList("ENTJ", "ESTJ"));
		mbtiIdealMatches.put("ENTJ", Arrays.asList("INFP", "INTP"));
	}

	private InfoPanel infoPanel;
	private final ProfileCard[] cards = new ProfileCard[8];

	// 🎨 개선된 색상 팔레트 적용
	private final Color color1 = new Color(250, 240, 240); // Warm Off-White (배경)
	private final Color color2 = new Color(255, 218, 225); // Soft Light Pink ('내 정보' 패널)
	private final Color color3 = new Color(230, 220, 240); // Light Lavender ('최근 채팅' 패널)
	private final Color color4 = new Color(190, 150, 210); // Muted Lavender/Purple (버튼)
	private final Color cardBackground = Color.WHITE; // 프로필 카드 배경
	private final Color defaultFontColor = new Color(50, 50, 50); // 기본 폰트 색상
	private final Color subtleBorder = new Color(220, 220, 220); // 얇은 구분선/테두리
	private BubbleArea chatBubbleArea;   // ← 이걸 추가

	public HomeView(MainApp mainApp) {
		this.mainApp = mainApp;
		setLayout(new BorderLayout());
		setBackground(color1);

		add(buildHeader(), BorderLayout.NORTH);
		add(buildBody(), BorderLayout.CENTER);

		// ✅ 유저 정보 먼저 세팅 후, 프로필 목록 불러오기
		updateUserInfo(mainApp.getLoggedInUser());
		loadProfilesFromServer();
	}

	public void updateUserInfo(User user) {
		if (infoPanel != null)
			infoPanel.update(user);
	}
	
	

	// 이하는 서버 통신 관련 코드로, 디자인 변경 없이 유지합니다.
	public void loadProfilesFromServer() {
	    new javax.swing.SwingWorker<Void, Void>() {

	        String response = null;

	        @Override
	        protected Void doInBackground() throws Exception {

	            User me = mainApp.getLoggedInUser();
	            if (me == null) return null;

	            String token = mainApp.getJwtToken();

	            // 🔥 기존 "/api/users" 대신 추천 전용 API 호출
	            ApiClient.HttpResult res =
	                    ApiClient.get("/api/users/recommend/" + me.getId(), token);

	            if (res.isOk()) {
	                response = res.body;
	            } else {
	                System.err.println("💥 추천 사용자 조회 실패: "
	                        + res.code + " / " + res.body);
	            }
	            return null;
	        }

	        @Override
	        protected void done() {
	            if (response == null) return;

	            try {
	                JSONArray arr = new JSONArray(response);

	                // 추천 결과는 이미 정렬되어 있음
	                for (int i = 0; i < cards.length; i++) {

	                    if (i >= arr.length()) {
	                        // 남은 칸은 비움
	                        cards[i].setProfile("-", "-", "-", 0, "1", 0, null);
	                        continue;
	                    }

	                    JSONObject obj = arr.getJSONObject(i);

	                    String name = obj.optString("userName", "이름 없음");
	                    String genderKor =
	                            obj.optString("gender", "m").equalsIgnoreCase("m")
	                                    ? "남자"
	                                    : "여자";

	                    int age = obj.optInt("age", 0);

	                    // 🔥 서버가 매칭 점수를 내려줌
	                    int score = obj.optInt("matchRate", 0);

	                    // 프로필 이미지
	                 // 프로필 이미지 (숫자/문자열 구분 처리)
	                    Object imgObj = obj.opt("profileImg");
	                    String profileNum;

	                    if (imgObj instanceof Number num) {
	                        profileNum = String.valueOf(num.intValue());
	                    } else {
	                        profileNum = obj.optString("profileImg", "1");
	                    }

	                    // default, null 처리 (안전망)
	                    if (profileNum == null || profileNum.isBlank() || profileNum.equals("default.jpg")) {
	                        profileNum = "1";
	                    }

	                    


	                    // MBTI 문자열 복원
	                    String mbtiStr = "-";
	                    JSONObject mbti = obj.optJSONObject("mbti");
	                    if (mbti != null) {
	                        String ei = mbti.optString("EI", "").toUpperCase();
	                        String sn = mbti.optString("SN", "").toUpperCase();
	                        String tf = mbti.optString("TF", "").toUpperCase();
	                        String jp = mbti.optString("JP", "").toUpperCase();
	                        if (ei.length() == 1 && sn.length() == 1 &&
	                            tf.length() == 1 && jp.length() == 1) {
	                            mbtiStr = ei + sn + tf + jp;
	                        }
	                    }

	                    String userId = obj.optString("id");

	                    // 🔥 카드 업데이트
	                    cards[i].setProfile(
	                            name,
	                            mbtiStr,
	                            genderKor,
	                            age,
	                            profileNum,
	                            score,
	                            userId
	                    );
	                }

	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }

	    }.execute();
	}

	
	private void loadChatRooms() {
	    new javax.swing.SwingWorker<Void, Void>() {

	        String response;

	        @Override
	        protected Void doInBackground() throws Exception {
	            User me = mainApp.getLoggedInUser();
	            if (me == null) return null;

	            String token = mainApp.getJwtToken();
	            ApiClient.HttpResult res =
	                ApiClient.get("/api/chat/rooms/user/" + me.getId(), token);

	            if (res.isOk()) {
	                response = res.body;
	            }
	            return null;
	        }

	        @Override
	        protected void done() {
	            if (response == null) return;

	            try {
	                chatBubbleArea.removeAll();

	                JSONArray arr = new JSONArray(response);
	                for (int i = 0; i < arr.length(); i++) {
	                    JSONObject room = arr.getJSONObject(i);

	                    // 마지막 메시지
	                    String lastMsg = "(메시지 없음)";
	                    JSONArray history = room.optJSONArray("chatHistory");
	                    if (history != null && history.length() > 0) {
	                        JSONObject last = history.getJSONObject(history.length() - 1);
	                        lastMsg = last.optString("message", lastMsg);
	                    }

	                    // 상대 이름
	                    String meId = mainApp.getLoggedInUser().getId();
	                    String partnerName = "상대 없음";

	                    JSONArray ps = room.optJSONArray("participants");
	                    if (ps != null) {
	                        for (int j = 0; j < ps.length(); j++) {
	                            JSONObject p = ps.getJSONObject(j);
	                            if (!p.getString("userId").equals(meId)) {
	                                partnerName = p.getString("userName");
	                            }
	                        }
	                    }

	                    chatBubbleArea.addLeft(partnerName + " : " + lastMsg);
	                }

	                chatBubbleArea.revalidate();
	                chatBubbleArea.repaint();

	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }.execute();
	}

	

	
	// ========================== 헤더 영역 (큰 변경 없음) ==========================
	private JComponent buildHeader() {
		JPanel header = new JPanel(new BorderLayout());
		// 테두리 색상 subtleBorder 적용
		header.setBorder(new CompoundBorder(new MatteBorder(0, 0, 1, 0, subtleBorder), new EmptyBorder(8, 16, 8, 16)));
		header.setBackground(color1); // 배경색 통일

		JLabel title = new JLabel("MBTI MATCH", SwingConstants.CENTER);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
		title.setForeground(defaultFontColor); // 폰트 색상 적용
		header.add(title, BorderLayout.NORTH);

		JPanel nav = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				// 네비게이션 아래 구분선 색상 변경
				g.setColor(color4.darker()); // 강조색의 어두운 버전
				g.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
			}
		};

		JButton btnHome = createNavButton("HOME");
		JButton btnMyMBTI = createNavButton("나의 MBTI");
		JButton btnGuide = createNavButton("MBTI 소개");
		JButton btnChat = createNavButton("채팅");
		JButton btnLogout = createNavButton("로그아웃");
		JButton btnMatch = createNavButton("매칭하기");

		nav.add(btnHome);
		nav.add(separator());
		nav.add(btnMatch);
		nav.add(separator());
		nav.add(btnMyMBTI);
		nav.add(separator());
		nav.add(btnGuide);
		nav.add(separator());
		nav.add(btnChat);
		nav.add(separator());
		nav.add(btnLogout);

		nav.setBackground(color1);
		header.add(nav, BorderLayout.SOUTH);

		btnLogout.addActionListener(e -> {
		    String token = mainApp.getJwtToken();

		    if (token != null && !token.isEmpty()) {
		        try {
		            // 서버에 로그아웃 요청 (Authorization 헤더에 토큰 넣어서)
		            ApiClient.HttpResult res =
		                    ApiClient.post("/api/users/logout", "{}", token);

		            if (!res.isOk()) {
		                System.err.println("로그아웃 API 실패: " + res.code + " / " + res.body);
		            }
		        } catch (Exception ex) {
		            ex.printStackTrace();
		        }
		    }

		    // 클라이언트 상태 정리 (jwt, 로그인 유저, 입력칸 등) — 이전에 말한 logout() 사용
		    mainApp.logout();
		});

		btnGuide.addActionListener(e -> mainApp.showView(MainApp.MBTI_INFO));
		btnMyMBTI.addActionListener(e -> mainApp.showView(MainApp.MYMBTI));
		btnChat.addActionListener(e -> {
			String token = mainApp.getJwtToken();
			User loggedIn = mainApp.getLoggedInUser();

			if (loggedIn == null || token == null || token.isEmpty()) {
				JOptionPane.showMessageDialog(this, "로그인이 필요합니다.");
				mainApp.showView(MainApp.LOGIN);
				return;
			}

			String selfId = loggedIn.getId();
			String selfName = loggedIn.getUserName();

			try {
				// 1) 내가 속한 채팅방 목록 조회
				ApiClient.HttpResult res = ApiClient.get("/api/chat/rooms/user/" + selfId, token);


				if (!res.isOk()) {
					JOptionPane.showMessageDialog(this, "서버 연결 오류");
					return;
				}

				JSONArray arr = new JSONArray(res.body);

				// 방이 하나라도 있으면 그 방으로 바로 입장
				if (arr.length() > 0) {
					JSONObject room = arr.getJSONObject(0);
					String roomId = room.getString("roomId");

					// ---------- 상대방 정보 추출 ----------
					String partnerId = null;
					String partnerName = "(상대 없음)";

					if (room.has("participants")) {
						JSONArray ps = room.getJSONArray("participants");

						for (int i2 = 0; i2 < ps.length(); i2++) {
							JSONObject p = ps.getJSONObject(i2);
							String uid = p.optString("userId", "");
							String uname = p.optString("userName", "");

							// 자기 자신이 아닌 사람 = 상대방
							if (!uid.isEmpty() && !uid.equals(selfId)) {
								partnerId = uid;
								partnerName = !uname.isEmpty() ? uname : uid;
								break;
							}
						}
					}

					// ---------- ChatView 호출 ----------
					ChatView chatView = mainApp.getChatView();
					chatView.startChat(roomId, selfId, selfName, partnerId, partnerName);

					mainApp.showView(MainApp.CHAT);
					return;
				}

				// 방이 없으면 안내
				JOptionPane.showMessageDialog(this, "매칭하기를 통해 대화를 시작하세요.");

			} catch (Exception ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(this, "채팅방 불러오기 오류");
			}
		});

		btnMatch.addActionListener(e -> {
			mainApp.showView(MainApp.MATCH_WAIT);
			mainApp.getMatchWaitView().startMatching(mainApp.getJwtToken());
		});

		return header;
	}

	private JButton createNavButton(String text) {
		JButton b = new JButton(" " + text + " ");
		b.setFocusPainted(false);
		b.setBorder(new EmptyBorder(6, 10, 6, 10));
		b.setContentAreaFilled(false);
		b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		b.setForeground(defaultFontColor);
		return b;
	}

	private Component separator() {
		JLabel s = new JLabel(" | ");
		s.setBorder(new EmptyBorder(0, 4, 0, 4));
		s.setForeground(new Color(150, 150, 150));
		return s;
	}

	// ========================== 본문 영역 ==========================
	private JComponent buildBody() {
		JPanel root = new JPanel(new BorderLayout());
		root.setBorder(new EmptyBorder(20, 20, 20, 20)); // 여백 증가
		root.setBackground(color1);

		infoPanel = new InfoPanel();
		JPanel left = infoPanel;
		left.setPreferredSize(new Dimension(280, 0)); // '내 정보' 패널 너비 증가

		JPanel center = new JPanel(new BorderLayout());
		JPanel titleBar = new JPanel(new BorderLayout());
		titleBar.setOpaque(false);

		JLabel recTitle = new JLabel("추천 상대");
		recTitle.setFont(recTitle.getFont().deriveFont(Font.BOLD, 18f));
		recTitle.setBorder(new EmptyBorder(0, 8, 12, 0));

		JButton refreshBtn = new PrettyButton("새로고침");

		refreshBtn.setBackground(color4);
		refreshBtn.setForeground(Color.WHITE);
		refreshBtn.setBorderPainted(false);
		refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		// 🔥 새로고침 이벤트 추가 (여기가 네가 물어본 부분)
		refreshBtn.addActionListener(e -> loadProfilesFromServer());

		titleBar.add(recTitle, BorderLayout.WEST);
		titleBar.add(refreshBtn, BorderLayout.EAST);
		JPanel filler = new JPanel();
		filler.setOpaque(false);
		titleBar.add(filler, BorderLayout.CENTER);

		center.add(titleBar, BorderLayout.NORTH);
		center.add(recommendGrid(), BorderLayout.CENTER);

		center.setBackground(color1);

		JPanel right = chatPanel();
		right.setPreferredSize(new Dimension(380, 0)); // '채팅' 패널 너비 증가

		JPanel middle = new JPanel(new BorderLayout(24, 0)); // 컴포넌트 간 간격 증가
		middle.add(left, BorderLayout.WEST);
		middle.add(center, BorderLayout.CENTER);

		middle.setBackground(color1);

		root.add(middle, BorderLayout.CENTER);
		return root;
	}

	private JComponent recommendGrid() {
		// 간격 증가
		JPanel grid = new JPanel(new GridLayout(2, 4, 18, 18));
		grid.setOpaque(false);

		for (int i = 0; i < cards.length; i++) {
			cards[i] = new ProfileCard(cardBackground); // 💡 흰색 배경 전달
			grid.add(cards[i]);
		}

		loadProfilesFromServer();

		return grid;
	}

	private JPanel chatPanel() {
		JPanel wrap = new JPanel(new BorderLayout());
		// 둥근 모서리 적용을 위해 RoundPanel 사용
		wrap = new RoundPanel(20, color3, subtleBorder, 1);
		wrap.setLayout(new BorderLayout());
		wrap.setBorder(new EmptyBorder(16, 16, 16, 16));
		wrap.setOpaque(false);

		JLabel h = new JLabel("최근 채팅");
		h.setFont(h.getFont().deriveFont(Font.BOLD, 16f));
		h.setBorder(new EmptyBorder(0, 0, 10, 0));
		h.setForeground(defaultFontColor);
		wrap.add(h, BorderLayout.NORTH);

		chatBubbleArea = new BubbleArea(color3);
		chatBubbleArea.addLeft("안녕하세요! 매칭을 축하드립니다.");


		JScrollPane sp = new JScrollPane(chatBubbleArea);

		sp.setBorder(null);
		// 스크롤 패널 배경색을 채팅 패널 배경색과 일치
		sp.getViewport().setBackground(color3);
		wrap.add(sp, BorderLayout.CENTER);

		JTextField input = new JTextField();
		// 폰트 크기 및 색상 조정
		input.setFont(input.getFont().deriveFont(14f));
		input.setForeground(defaultFontColor);
		input.setBorder(new CompoundBorder(new LineBorder(subtleBorder, 1, true), new EmptyBorder(4, 8, 4, 8)));

		JButton send = new JButton("보내기"); // 아이콘 대신 텍스트로 변경
		send.setPreferredSize(new Dimension(60, 36));
		send.setBackground(color4);
		send.setForeground(Color.WHITE); // 버튼 폰트 흰색
		send.setBorderPainted(false);
		send.setOpaque(true);
		send.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		JPanel bottom = new JPanel(new BorderLayout(8, 0));
		bottom.setBorder(new EmptyBorder(12, 0, 0, 0));
		bottom.setOpaque(false);
		bottom.add(input, BorderLayout.CENTER);
		bottom.add(send, BorderLayout.EAST);

		wrap.add(bottom, BorderLayout.SOUTH);
		loadChatRooms();    // ← 추가

		return wrap;
		
	}

	// ========================== 왼쪽 내 정보 패널 ==========================
	class InfoPanel extends JPanel {

		private JLabel avatarLabel; // 🔥 아바타 라벨을 필드로 선언
		private final JLabel idValue = new JLabel("-");
		private final JLabel mbtiValue = new JLabel("-");
		private final JLabel genderValue = new JLabel("-");
		private final JLabel ageValue = new JLabel("-");
		private final JLabel userNameValue = new JLabel("-");

		InfoPanel() {
			super(new BorderLayout());

			JPanel wrapper = new RoundPanel(20, color2, subtleBorder, 1);
			wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
			wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
			wrapper.setOpaque(false);

			JLabel title = new JLabel("내 정보");
			title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
			title.setAlignmentX(Component.CENTER_ALIGNMENT);
			wrapper.add(title);

			// 🔥 placeholder 아바타 먼저 넣기
			avatarLabel = avatarLabel("/images/default_profile.png", 100);
			avatarLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			avatarLabel.setBorder(new LineBorder(color4, 2, true));
			wrapper.add(avatarLabel);
			wrapper.add(Box.createVerticalStrut(24));

			JPanel infoContainer = new JPanel();
			infoContainer.setLayout(new BoxLayout(infoContainer, BoxLayout.Y_AXIS));
			infoContainer.setOpaque(false);
			infoContainer.add(infoLine("아이디", idValue));
			infoContainer.add(infoLine("닉네임", userNameValue));
			infoContainer.add(infoLine("MBTI", mbtiValue));
			infoContainer.add(infoLine("성별", genderValue));
			infoContainer.add(infoLine("나이", ageValue));
			wrapper.add(infoContainer);
			wrapper.add(Box.createVerticalStrut(16)); // 여백 약간 추가

			JButton edit = new JButton("프로필 수정");
			edit.setBackground(color4);
			edit.setForeground(Color.WHITE);
			edit.setBorderPainted(false);
			edit.setOpaque(true);
			edit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			edit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
			edit.setAlignmentX(Component.CENTER_ALIGNMENT);

			edit.addActionListener(e -> {
				User user = mainApp.getLoggedInUser();
				if (user == null) {
					JOptionPane.showMessageDialog(InfoPanel.this, "로그인 정보가 없습니다.");
					return;
				}

				Window owner = SwingUtilities.getWindowAncestor(HomeView.this);
				ProfileEditDialog dialog = new ProfileEditDialog(owner, user);
				dialog.setLocationRelativeTo(HomeView.this);
				dialog.setVisible(true);

				update(user); // 수정 후 정보 갱신
			});

			wrapper.add(edit);

			wrapper.add(Box.createVerticalGlue());

			add(wrapper, BorderLayout.CENTER);
			setOpaque(false);
		}

		// 🔥 user 정보가 갱신될 때 아바타도 갱신하도록
		void update(User user) {

			if (user == null)
				return;

			idValue.setText(user.getId());
			userNameValue.setText(user.getUserName());
			mbtiValue.setText(buildMbti(user.getMbti()));
			genderValue.setText(buildGender(user.getGender()));
			ageValue.setText(user.getAge() + "세");

			// 🔥 프로필 이미지 적용
			String profileNum = user.getProfileImg();

			// default면 랜덤 1번만 적용하고 user에 저장
			if (profileNum == null || profileNum.equals("default.jpg") || profileNum.isEmpty()) {
			    profileNum = String.valueOf(1 + (int) (Math.random() * 5));
			    user.setProfileImg(profileNum);   // 한 번만 랜덤 적용
			}


			String avatarPath = "/images/profile" + profileNum + ".png";

			ImageIcon icon = new ImageIcon(new ImageIcon(getClass().getResource(avatarPath)).getImage()
					.getScaledInstance(100, 100, Image.SCALE_SMOOTH));

			avatarLabel.setIcon(icon);

			revalidate();
			repaint();
		}
	}

	private JComponent infoLine(String label, JLabel valueLabel) {
		JPanel p = new JPanel(new BorderLayout());
		p.setOpaque(false);

		JLabel l = new JLabel(label + " ");
		l.setFont(l.getFont().deriveFont(Font.BOLD, 13f));
		l.setForeground(defaultFontColor);

		JSeparator sep = new JSeparator();
		sep.setForeground(new Color(200, 200, 200));
		sep.setBorder(new EmptyBorder(0, 8, 0, 8)); // 구분선 좌우 여백

		valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		valueLabel.setForeground(defaultFontColor.darker());

		p.add(l, BorderLayout.WEST);
		p.add(sep, BorderLayout.CENTER);
		p.add(valueLabel, BorderLayout.EAST);

		p.setBorder(new EmptyBorder(8, 0, 8, 0)); // 상하 여백 증가
		return p;
	}

	// MBTI, Gender 빌드 로직은 변경 없이 유지
	private String buildMbti(Map<String, String> mbti) {
		if (mbti == null)
			return "-";
		String[] keys = { "EI", "SN", "TF", "JP" };
		StringBuilder sb = new StringBuilder();
		for (String k : keys) {
			String v = mbti.get(k);
			if (v != null)
				sb.append(v);
		}
		return sb.length() == 0 ? "-" : sb.toString();
	}

	private String buildGender(String g) {
		if (g == null)
			return "-";
		g = g.toLowerCase();
		if (g.startsWith("m"))
			return "남자";
		if (g.startsWith("f"))
			return "여자";
		return g;
	}

	// ========================== 공통 유틸: 아바타 ==========================
	private JLabel avatarLabel(String pathOrClasspath, int size) {
		Image img;
		URL url = getClass().getResource(pathOrClasspath);
		if (url != null)
			img = new ImageIcon(url).getImage();
		else
			img = new ImageIcon(pathOrClasspath).getImage();

		Image scaled = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
		JLabel label = new JLabel(new ImageIcon(scaled));
		label.setPreferredSize(new Dimension(size, size));
		label.setMinimumSize(new Dimension(size, size));
		label.setMaximumSize(new Dimension(size, size));
		// 아바타 테두리 삭제 및 원형 효과를 위해 별도 처리 (여기서는 단순 사각형으로만 처리)
		label.setBorder(null);
		return label;
	}

	// 추천 후보 1명을 나타내는 작은 클래스
	static class Candidate {
		JSONObject obj;
		String mbti;
		int score;

		Candidate(JSONObject obj, String mbti, int score) {
			this.obj = obj;
			this.mbti = mbti;
			this.score = score;
		}
	}

	// ========================== 공통 버튼 스타일 ==========================
	static class PrettyButton extends JButton {

		public PrettyButton(String text) {
			super(text);
			setFocusPainted(false);
			setForeground(Color.WHITE);
			setBackground(new Color(190, 150, 210));
			setBorder(new EmptyBorder(8, 16, 8, 16));
			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			// 둥근 모서리
			setContentAreaFilled(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// 배경
			Color bg = getModel().isRollover() ? new Color(210, 170, 230) // hover 색
					: new Color(190, 150, 210); // 기본색

			g2.setColor(bg);
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

			// 텍스트 그리기
			FontMetrics fm = g2.getFontMetrics();
			int x = (getWidth() - fm.stringWidth(getText())) / 2;
			int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;

			g2.setColor(Color.WHITE);
			g2.drawString(getText(), x, y);

			g2.dispose();
		}
	}

	// ========================== 추천 카드 (개선) ==========================
	// ========================== 추천 카드 (개선 완성본) ==========================
	class ProfileCard extends JPanel {

		private JLabel nameLabel;
		private JLabel mbtiLabel;
		private JLabel genderAgeLabel;
		private JLabel imageLabel;
		private Color cardBackground;
		private AnimatedLabel matchLabel;
		private RoundPanel panel;

		private String currentProfileNum = "1"; // 이미지 번호 기억용
		private String profileUserId;

		// 점수에 따른 색상
		private Color getMatchColor(int percent) {
			if (percent >= 80)
				return new Color(255, 60, 150);
			if (percent >= 60)
				return new Color(255, 120, 0);
			if (percent >= 40)
				return new Color(255, 180, 0);
			return new Color(120, 120, 120);
		}

		// ------------------ AnimatedLabel ------------------
		class AnimatedLabel extends JLabel {
			float scale = 1.0f;

			AnimatedLabel(String text) {
				super(text);
				setAlignmentX(Component.CENTER_ALIGNMENT);
			}

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				int w = getWidth();
				int h = getHeight();
				int x = (int) ((w - w * scale) / 2);
				int y = (int) ((h - h * scale) / 2);

				g2.translate(x, y);
				g2.scale(scale, scale);

				super.paintComponent(g2);
				g2.dispose();
			}
		}

		// ------------------ 생성자 ------------------
		ProfileCard(Color cardBackground) {
			this.cardBackground = cardBackground;

			setLayout(new BorderLayout());
			setOpaque(false);

			// 부드러운 연보라 배경
			Color softPurple = new Color(248, 245, 255);

			panel = new RoundPanel(16, softPurple, new Color(200, 200, 200), 1);
			panel.setOpaque(false);
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setBorder(new EmptyBorder(12, 12, 12, 12));

			// 이미지 라벨
			imageLabel = new JLabel(new ImageIcon("images/default_profile.png"));
			imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			imageLabel.setBorder(new LineBorder(new Color(200, 120, 180), 2, true));
			panel.add(imageLabel);
			panel.add(Box.createVerticalStrut(12));

			// 이름
			nameLabel = new JLabel("이름");
			nameLabel.setFont(new Font("Dialog", Font.BOLD, 15));
			nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			panel.add(nameLabel);

			// MBTI
			mbtiLabel = new JLabel("MBTI");
			mbtiLabel.setForeground(new Color(190, 150, 210));
			mbtiLabel.setFont(new Font("Dialog", Font.BOLD, 13));
			mbtiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			panel.add(mbtiLabel);

			// 성별 / 나이
			genderAgeLabel = new JLabel("성별 / 나이");
			genderAgeLabel.setForeground(new Color(100, 100, 100));
			genderAgeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			panel.add(genderAgeLabel);

			// 궁합 점수
			matchLabel = new AnimatedLabel("궁합 점수: -점");
			matchLabel.setFont(new Font("Dialog", Font.BOLD, 14));
			matchLabel.setPreferredSize(new Dimension(130, 26));
			matchLabel.setMaximumSize(new Dimension(130, 26));
			matchLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
			panel.add(matchLabel);

			panel.add(Box.createVerticalStrut(8));

			// 채팅하기 버튼
			PrettyButton chatBtn = new PrettyButton("채팅하기");
			chatBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

			chatBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
			chatBtn.setBackground(new Color(190, 150, 210));
			chatBtn.setForeground(Color.WHITE);
			chatBtn.setBorder(new EmptyBorder(6, 12, 6, 12));
			chatBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			chatBtn.addActionListener(e -> {

			    User me = mainApp.getLoggedInUser();
			    if (me == null) {
			        JOptionPane.showMessageDialog(this, "로그인이 필요합니다.");
			        return;
			    }

			    String myId = me.getId();
			    String myName = me.getUserName();
			    String targetName = nameLabel.getText();
			    String targetId = profileUserId;

			    try {
			        // 🔥 여기 추가 — 토큰 가져오기
			        String token = mainApp.getJwtToken();

			        JSONObject json = new JSONObject();
			        json.put("user1", myId);
			        json.put("user1Name", myName);
			        json.put("user2", targetId);
			        json.put("user2Name", targetName);

			        ApiClient.HttpResult res = ApiClient.post("/api/chat/rooms", json.toString(), token);

			        if (!res.isOk()) {
			            JOptionPane.showMessageDialog(this, "채팅방 생성 실패: " + res.body);
			            return;
			        }

			        JSONObject obj = new JSONObject(res.body);
			        String roomId = obj.getString("roomId");

			        ChatView chatView = mainApp.getChatView();
			        chatView.startChat(roomId, myId, myName, targetId, targetName);
			        mainApp.showView(MainApp.CHAT);

			    } catch (Exception ex) {
			        ex.printStackTrace();
			        JOptionPane.showMessageDialog(this, "채팅 오류: " + ex.getMessage());
			    }
			});


			panel.add(chatBtn);

			// -------- hover 안정화 적용 --------
			installHoverSystem(chatBtn);

			add(panel, BorderLayout.CENTER);
		}

		// ------------------ Hover 시스템 ------------------
		private void installHoverSystem(JButton chatBtn) {

			MouseAdapter hover = new MouseAdapter() {

				@Override
				public void mouseEntered(MouseEvent e) {
					expandCard();
				}

				@Override
				public void mouseExited(MouseEvent e) {

					// 마우스가 진짜 카드 밖으로 나갔는지 확인
					Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), ProfileCard.this);

					if (!ProfileCard.this.contains(p)) {
						shrinkCard();
					}
				}
			};

			// ProfileCard 전체
			this.addMouseListener(hover);

			// 모든 자식 컴포넌트에도 hover 유지 지원
			addHoverSupport(this);
			addHoverSupport(panel);
			addHoverSupport(imageLabel);
			addHoverSupport(nameLabel);
			addHoverSupport(mbtiLabel);
			addHoverSupport(genderAgeLabel);
			addHoverSupport(matchLabel);
			addHoverSupport(chatBtn);
		}

		private void addHoverSupport(JComponent comp) {
			comp.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					expandCard();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), ProfileCard.this);
					if (!ProfileCard.this.contains(p))
						shrinkCard();
				}
			});
		}

		private void expandCard() {
			panel.setBackground(new Color(240, 230, 255));
			panel.setBorder(new LineBorder(new Color(180, 120, 210), 2, true));
			updateProfileImage(100);
			panel.repaint();
		}

		private void shrinkCard() {
			panel.setBackground(cardBackground);
			panel.setBorder(new LineBorder(new Color(200, 200, 200), 1, true));
			updateProfileImage(80);
			panel.repaint();
		}

		// ------------------ 프로필 세팅 ------------------
		public void setUserId(String userId) {
		    this.profileUserId = userId;
		}

		
		public void setProfile(String name, String mbti, String gender, int age,
                String profileNum, int matchPercent, String userId) {

		    // userId는 필요하면 나중에 따로 set 해주는 방식으로
			this.profileUserId = userId;

		    nameLabel.setText(name);
		    mbtiLabel.setText(mbti);
		    genderAgeLabel.setText(gender + " / " + age + "세");

		    currentProfileNum = profileNum;

		    matchLabel.setText("궁합 점수: " + matchPercent + "점 ❤️");
		    matchLabel.setForeground(getMatchColor(matchPercent));

		    animateMatchLabel();
		    updateProfileImage(80);
		}


		// ------------------ 점수 애니메이션 ------------------
		private void animateMatchLabel() {
			Timer timer = new Timer(20, null);

			timer.addActionListener(new java.awt.event.ActionListener() {
				float scale = 1.0f;
				boolean growing = true;

				@Override
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (growing) {
						scale += 0.05f;
						if (scale >= 1.25f) {
							scale = 1.25f;
							growing = false;
						}
					} else {
						scale -= 0.05f;
						if (scale <= 1.0f) {
							scale = 1.0f;
							timer.stop();
						}
					}

					matchLabel.scale = scale;
					matchLabel.repaint();
				}
			});

			timer.start();
		}

		// ------------------ 이미지 변경 ------------------
		private void updateProfileImage(int size) {
			String imgPath = "/images/profile" + currentProfileNum + ".png";
			URL url = getClass().getResource(imgPath);
			if (url != null) {
				Image img = new ImageIcon(url).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
				ImageIcon icon = new ImageIcon(img);
				imageLabel.setIcon(icon);

				imageLabel.setPreferredSize(new Dimension(size, size));
				imageLabel.setMaximumSize(new Dimension(size, size));
			}
		}
	}

	// ========================== 채팅 말풍선 영역 (개선) ==========================
	static class BubbleArea extends JPanel {
		private final List<Msg> msgs = new ArrayList<>();
		private Color bgColor;

		static class Msg {
			String text;
			boolean right;

			Msg(String t, boolean r) {
				text = t;
				right = r;
			}
		}

		BubbleArea(Color bgColor) {
			this.bgColor = bgColor;
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			setBackground(bgColor);
			msgs.clear(); // 초기화
		}

		void addLeft(String t) {
			addMsg(new Msg(t, false));
		}

		private void addMsg(Msg m) {
			msgs.add(m);
			JPanel row = new JPanel(new BorderLayout());
			row.setOpaque(false);
			Bubble b = new Bubble(m.text, m.right);

			if (m.right) {
				row.add(b, BorderLayout.EAST);
			} else {
				row.add(b, BorderLayout.WEST);
			}

			row.setBorder(new EmptyBorder(6, 6, 6, 6));
			add(row);
			revalidate();
			repaint();
			// 스크롤을 맨 아래로 이동하는 로직이 필요하면 여기에 추가
		}
	}

	// ========================== 말풍선 컴포넌트 (개선) ==========================
	static class Bubble extends JComponent {
		private final String text;
		private final boolean right;

		Bubble(String text, boolean right) {
			this.text = text;
			this.right = right;
			// 텍스트 길이에 따라 크기를 동적으로 조절
			int lineCount = (int) Math.ceil(text.length() / 20.0);
			int prefHeight = 24 + lineCount * 18;
			int prefWidth = Math.min(240, 60 + text.length() * 10); // 최대 너비 240
			setPreferredSize(new Dimension(prefWidth, prefHeight));
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int w = getWidth(), h = getHeight();
			int arc = 12; // 둥근 정도 감소

			// 말풍선 배경
			Color bubbleColor = right ? new Color(255, 255, 255) : new Color(255, 230, 240); // 내가 보낸 메시지: 흰색, 상대방: 연한
																								// 핑크
			g2.setColor(bubbleColor);
			Shape r = new RoundRectangle2D.Float(0, 0, w - 1, h - 1, arc, arc);
			g2.fill(r);

			// 말풍선 테두리
			g2.setColor(new Color(220, 220, 220));
			g2.draw(r);

			g2.setColor(Color.DARK_GRAY);
			g2.setFont(new Font("Dialog", Font.PLAIN, 13));
			FontMetrics fm = g2.getFontMetrics();
			int pad = 10, y = pad + fm.getAscent();

			// 텍스트 줄바꿈 개선
			String[] words = text.split(" ");
			StringBuilder line = new StringBuilder();
			for (String word : words) {
				String candidate = (line.length() == 0 ? word : line + " " + word);
				if (fm.stringWidth(candidate) > w - pad * 2) {
					g2.drawString(line.toString(), pad, y);
					line = new StringBuilder(word);
					y += fm.getHeight();
				} else {
					line = new StringBuilder(candidate);
				}
			}
			g2.drawString(line.toString(), pad, y);
			g2.dispose();
		}
	}

	// ========================== 둥근 모서리 패널 클래스 추가 ==========================
	// HomeView 내부에 정의
	static class RoundPanel extends JPanel {
		private int cornerRadius = 15;
		private Color bgColor;
		private Color borderColor;
		private int borderThickness;

		public RoundPanel(int radius, Color bgColor, Color borderColor, int thickness) {
			this.cornerRadius = radius;
			this.bgColor = bgColor;
			this.borderColor = borderColor;
			this.borderThickness = thickness;
			setOpaque(false); // 배경을 투명하게 만듦
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int width = getWidth();
			int height = getHeight();

			// 1. 배경 채우기
			g2.setColor(bgColor);
			g2.fill(new RoundRectangle2D.Double(0, 0, width - 1, height - 1, cornerRadius, cornerRadius));

			// 2. 테두리 그리기
			if (borderThickness > 0) {
				g2.setColor(borderColor);
				// 테두리 두께를 고려하여 외곽선을 그림
				g2.draw(new RoundRectangle2D.Double(0.5, 0.5, width - 1, height - 1, cornerRadius, cornerRadius));
			}
		}
	}

	// ========================== 프로필 수정 다이얼로그 (생략 및 유지) ==========================
	// ProfileEditDialog 클래스는 기능적 요소가 많으므로 디자인 변경 없이 기존 코드를 유지했습니다.
	class ProfileEditDialog extends JDialog {

	    private final User user;

	    private final JTextField tfId = new JTextField();
	    private final JTextField tfUserName = new JTextField();
	    private final JComboBox<String> cbMbti = new JComboBox<>(MBTI_TYPES);
	    private int selectedAvatar = 1;

	    private final JComboBox<String> cbGender = new JComboBox<>(new String[]{"남자", "여자"});
	    private final JSpinner spAge = new JSpinner(new SpinnerNumberModel(20, 1, 100, 1));

	    ProfileEditDialog(Window owner, User user) {
	        super(owner, "프로필 수정", ModalityType.APPLICATION_MODAL);
	        this.user = user;

	        setLayout(new BorderLayout(10, 10));
	        ((JComponent) getContentPane()).setBackground(new Color(255, 240, 245));
	        ((JComponent) getContentPane()).setBorder(new EmptyBorder(14, 14, 14, 14));

	        // ---------------------------
	        // 1) 상단 아바타 선택 (가운데)
	        // ---------------------------
	        JPanel avatarTop = new JPanel();
	        avatarTop.setOpaque(false);
	        avatarTop.setLayout(new BoxLayout(avatarTop, BoxLayout.Y_AXIS));

	        JLabel avatarTitle = new JLabel("캐릭터 선택");
	        avatarTitle.setFont(new Font("맑은 고딕", Font.BOLD, 14));
	        avatarTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

	        JPanel selector = buildAvatarSelector();
	        selector.setAlignmentX(Component.CENTER_ALIGNMENT);

	        avatarTop.add(avatarTitle);
	        avatarTop.add(Box.createVerticalStrut(8));
	        avatarTop.add(selector);
	        avatarTop.add(Box.createVerticalStrut(12));

	        add(avatarTop, BorderLayout.NORTH);

	        // ---------------------------
	        // 2) 중앙 입력 폼 (2열 GridLayout)
	        // ---------------------------
	        JPanel form = new JPanel(new GridLayout(5, 2, 10, 10));
	        form.setOpaque(false);

	        tfId.setEditable(false);

	        form.add(label("아이디"));
	        form.add(tfId);

	        form.add(label("닉네임"));
	        form.add(tfUserName);

	        form.add(label("MBTI"));
	        form.add(cbMbti);

	        form.add(label("성별"));
	        form.add(cbGender);

	        form.add(label("나이"));
	        form.add(spAge);

	        add(form, BorderLayout.CENTER);

	        // ---------------------------
	        // 3) 하단 버튼
	        // ---------------------------
	        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
	        btnPanel.setOpaque(false);

	        JButton cancel = new JButton("취소");
	        JButton save = new JButton("저장");

	        cancel.setBackground(new Color(200, 200, 200));
	        save.setBackground(new Color(190, 150, 210));
	        save.setForeground(Color.WHITE);

	        cancel.setPreferredSize(new Dimension(90, 32));
	        save.setPreferredSize(new Dimension(90, 32));

	        btnPanel.add(cancel);
	        btnPanel.add(save);

	        add(btnPanel, BorderLayout.SOUTH);

	        cancel.addActionListener(e -> dispose());
	        save.addActionListener(e -> onSave());

	        initFields();

	        pack();
	        setResizable(false);
	    }

	    // ---------------------------
	    // 라벨 생성 유틸
	    // ---------------------------
	    private JLabel label(String text) {
	        JLabel l = new JLabel(text);
	        l.setFont(new Font("맑은 고딕", Font.BOLD, 12));
	        return l;
	    }

	    // ---------------------------
	    // 아바타 선택 UI
	    // ---------------------------
	    private JPanel buildAvatarSelector() {

	        JPanel box = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
	        box.setOpaque(false);

	        ButtonGroup group = new ButtonGroup();

	        for (int i = 1; i <= 5; i++) {
	            final int num = i;

	            String path = "/images/profile" + num + ".png";
	            ImageIcon icon;

	            URL url = getClass().getResource(path);
	            if (url != null) {
	                Image img = new ImageIcon(url).getImage()
	                        .getScaledInstance(60, 60, Image.SCALE_SMOOTH);
	                icon = new ImageIcon(img);
	            } else {
	                icon = new ImageIcon();
	            }

	            JToggleButton btn = new JToggleButton(icon);
	            btn.setPreferredSize(new Dimension(65, 65));
	            btn.setFocusPainted(false);
	            btn.setBackground(Color.WHITE);
	            btn.setBorder(new LineBorder(Color.LIGHT_GRAY, 2));
	            btn.setContentAreaFilled(true);

	            if (num == selectedAvatar) {
	                btn.setSelected(true);
	                btn.setBorder(new LineBorder(new Color(255, 128, 128), 3));
	                btn.setBackground(new Color(255, 240, 240));
	            }

	            btn.addActionListener(e -> {
	                selectedAvatar = num;

	                for (Component c : box.getComponents()) {
	                    if (c instanceof JToggleButton b) {
	                        if (b == btn) {
	                            b.setBorder(new LineBorder(new Color(255, 128, 128), 3));
	                            b.setBackground(new Color(255, 240, 240));
	                        } else {
	                            b.setBorder(new LineBorder(Color.LIGHT_GRAY, 2));
	                            b.setBackground(Color.WHITE);
	                        }
	                    }
	                }
	            });

	            group.add(btn);
	            box.add(btn);
	        }

	        return box;
	    }

	    // ---------------------------
	    // 초기 값 채우기
	    // ---------------------------
	    private void initFields() {
	        tfId.setText(user.getId());
	        tfUserName.setText(user.getUserName());

	        // avatar
	        try {
	            selectedAvatar = Integer.parseInt(user.getProfileImg());
	        } catch (Exception e) {
	            selectedAvatar = 1;
	        }

	        // MBTI 맵 → 문자열
	        if (user.getMbti() != null) {
	            StringBuilder sb = new StringBuilder();
	            String[] keys = {"EI", "SN", "TF", "JP"};
	            for (String k : keys) {
	                String v = user.getMbti().get(k);
	                if (v != null) sb.append(v);
	            }
	            String mbtiStr = sb.toString();
	            if(!mbtiStr.isEmpty()) {
	            	 cbMbti.setSelectedItem(mbtiStr);   
	            }
	        }

	        if ("m".equalsIgnoreCase(user.getGender()))
	            cbGender.setSelectedItem("남자");
	        else
	            cbGender.setSelectedItem("여자");

	        if (user.getAge() != null)
	            spAge.setValue(user.getAge());
	    }

	    // ---------------------------
	    // User 객체에 반영 + 서버 전달
	    // ---------------------------
	    private void onSave() {

	        user.setUserName(tfUserName.getText().trim());
	        user.setProfileImg(String.valueOf(selectedAvatar));

	        String genderKor = (String) cbGender.getSelectedItem();
	        user.setGender("남자".equals(genderKor) ? "m" : "f");

	        user.setAge((Integer) spAge.getValue());

	        String mbtiStr = (String) cbMbti.getSelectedItem();
	        if (mbtiStr != null && mbtiStr.length() == 4) {
	            Map<String, String> map = new HashMap<>();
	            map.put("EI", "" + mbtiStr.charAt(0));
	            map.put("SN", "" + mbtiStr.charAt(1));
	            map.put("TF", "" + mbtiStr.charAt(2));
	            map.put("JP", "" + mbtiStr.charAt(3));
	            user.setMbti(map);
	        }

	        try {
	            String token = mainApp.getJwtToken();
	            String path = "/api/users/" + user.getId();     // api prefix 추가


	            JSONObject json = new JSONObject();
	            json.put("userName", user.getUserName());
	            json.put("gender", user.getGender());
	            json.put("age", user.getAge());
	            json.put("profileImg", selectedAvatar);

	            JSONObject mbtiJson = new JSONObject(user.getMbti());
	            json.put("mbti", mbtiJson);

	            ApiClient.HttpResult res =
	                    ApiClient.put(path, json.toString(), token);

	            if (!res.isOk()) {
	                JOptionPane.showMessageDialog(this, "서버 저장 실패");
	            } else {
	                JOptionPane.showMessageDialog(this, "프로필이 저장되었습니다.");
	            }

	        } catch (Exception ex) {
	            ex.printStackTrace();
	            JOptionPane.showMessageDialog(this, "서버 오류: " + ex.getMessage());
	        }

	        dispose();
	    }
	}

}