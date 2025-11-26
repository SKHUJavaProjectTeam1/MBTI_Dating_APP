package com.mbtidating.view;

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.json.JSONArray;
import org.json.JSONObject;

public class MatchPanelView extends JPanel {

    // MBTIHome.ProfileCard 배열 (8개 카드)
    private HomeView.ProfileCard[] cards = new HomeView.ProfileCard[8];

    // ★ 서버 UserController에서 제공하는 실제 동작하는 API!
    private static final String MATCH_API_URL =
            "http://localhost:8080/api/users";

    public MatchPanelView() {
        setLayout(new GridLayout(2, 4, 16, 16));

        // 기본 카드 생성
        for (int i = 0; i < 8; i++) {
            cards[i] = new HomeView.ProfileCard("카드 " + (i + 1));
            add(cards[i]);
        }

        // 서버 데이터 불러오기
        loadMatchesFromServer();
    }

    private void loadMatchesFromServer() {
        new SwingWorker<Void, Void>() {

            private String responseText = null;

            @Override
            protected Void doInBackground() {
                try {
                    System.out.println("[MatchPanel] 서버 호출 시작");

                    URL url = new URL(MATCH_API_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    int status = conn.getResponseCode();
                    System.out.println("[MatchPanel] HTTP 응답 코드: " + status);

                    if (status == 200) {
                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(conn.getInputStream(), "UTF-8"));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            sb.append(line);
                        }
                        in.close();
                        responseText = sb.toString();
                        System.out.println("[MatchPanel] 응답 내용: " + responseText);
                    } else {
                        System.out.println("[MatchPanel] 서버 오류 status=" + status);
                    }

                    conn.disconnect();
                } catch (Exception e) {
                    System.out.println("[MatchPanel] 예외 발생");
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                if (responseText == null || responseText.isEmpty()) {
                    System.out.println("[MatchPanel] 응답이 비었음");
                    return;
                }

                try {
                    JSONArray arr = new JSONArray(responseText);

                    for (int i = 0; i < arr.length() && i < cards.length; i++) {
                        JSONObject obj = arr.getJSONObject(i);

                        String userName = obj.optString("userName", "이름 없음");
                        String gender = obj.optString("gender", "");
                        Integer age = obj.has("age") ? obj.optInt("age") : null;
                        String region = obj.optString("region", "");

                        // MBTI 조합
                        String mbtiText = "-";
                        JSONObject mbtiObj = obj.optJSONObject("mbti");
                        if (mbtiObj != null) {
                            String ei = mbtiObj.optString("EI", "");
                            String sn = mbtiObj.optString("SN", "");
                            String tf = mbtiObj.optString("TF", "");
                            String jp = mbtiObj.optString("JP", "");
                            String tmp = ei + sn + tf + jp;
                            if (!tmp.isBlank()) mbtiText = tmp;
                        }

                        // ⭐ 궁합 퍼센트 추출
                        int matchRate = obj.optInt("matchRate", -1);

                        System.out.println("[MatchPanel] 카드 " + (i + 1) +
                                " -> name=" + userName +
                                ", mbti=" + mbtiText +
                                ", gender=" + gender +
                                ", age=" + age +
                                ", region=" + region +
                                ", matchRate=" + matchRate);

                        // 카드에 전체 정보 세팅 (궁합 포함)
                        cards[i].setProfile(userName, mbtiText, gender, age, region, matchRate);
                    }

                } catch (Exception e) {
                    System.out.println("[MatchPanel] JSON 파싱 오류");
                    e.printStackTrace();
                }
            }


        }.execute();
    }
}
