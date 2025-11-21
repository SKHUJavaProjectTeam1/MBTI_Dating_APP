package com.mbtidating.view;


import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;
import javax.swing.*;

public class MatchView {

    public static List<String> loadMatches() {
        List<String> matches = new ArrayList<>();

        try {
            // 서버 API 주소
            URL url = new URL("http://localhost:8080/api/match");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // 응답 코드 확인
            if (conn.getResponseCode() != 200) {
                System.out.println("서버 연결 실패: " + conn.getResponseCode());
                return matches;
            }

            // 결과 읽기
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            // JSON 파싱
            JSONArray array = new JSONArray(sb.toString());
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String user1 = obj.getString("user1Id");
                String user2 = obj.getString("user2Id");
                String display = user1 + " ❤️ " + user2;
                matches.add(display);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return matches;
    }
}
