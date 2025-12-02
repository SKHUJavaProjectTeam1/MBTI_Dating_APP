package com.mbtidating.network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ApiClient {

	private static final String BASE_URL = "http://localhost:8080";

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;
    
    // ✅ 공통 요청 처리 메서드
    private static HttpResult sendRequest(String method, String urlStr, String json, String jwtToken) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            if (jwtToken != null && !jwtToken.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
            }

            if (json != null && !json.isEmpty() && ("POST".equals(method) || "PUT".equals(method))) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(json.getBytes(StandardCharsets.UTF_8));
                }
            }

            int code = conn.getResponseCode();
            String body = readAll(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
            return new HttpResult(code, body);

        } finally {
            if (conn != null) {
                conn.disconnect(); // 연결 명시적 종료
            }
        }
    }

    // ✅ 기본 POST  (BASE_URL + path)
    public static HttpResult post(String path, String json) throws IOException {
        return sendRequest("POST", BASE_URL + path, json, null);
    }

    // ✅ JWT 토큰 포함 POST (인증 필요할 때)
    public static HttpResult post(String path, String json, String jwtToken) throws IOException {
        return sendRequest("POST", BASE_URL + path, json, jwtToken);
    }

    // ✅ 전체 URL을 직접 넘기는 POST (기존 LoginView 호환용)
    public static HttpResult postJson(String url, String json) throws IOException {
        return sendRequest("POST", url, json, null);
    }

    // ✅ 기본 GET  (BASE_URL + path)
    public static HttpResult get(String path) throws IOException {
        return sendRequest("GET", BASE_URL + path, null, null);
    }
    
    //삭제
    public static HttpResult delete(String path, String jwtToken) throws IOException {
        return sendRequest("DELETE", BASE_URL + path, null, jwtToken);
    }


    // ✅ JWT 토큰 포함 GET (필요하면 사용)
    public static HttpResult get(String path, String jwtToken) throws IOException {
        return sendRequest("GET", BASE_URL + path, null, jwtToken);
    }

    // ✅ NEW: 기본 PUT (BASE_URL + path, JWT 포함 버전만 사용)
    public static HttpResult put(String path, String json, String jwtToken) throws IOException {
        return sendRequest("PUT", BASE_URL + path, json, jwtToken);
    }

    // ✅ (선택) 전체 URL을 직접 넘기는 PUT
    //   필요 없으면 안 써도 되는데, 혹시 HomeView 쪽에서 full URL 쓰고 싶으면 사용
    public static HttpResult putJson(String url, String json, String jwtToken) throws IOException {
        return sendRequest("PUT", url, json, jwtToken);
    }

    // ✅ 공통 응답 바디 읽기
    private static String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    // ✅ 응답 구조체
    public static class HttpResult {
        public final int code;
        public final String body;

        public HttpResult(int code, String body) {
            this.code = code;
            this.body = body;
        }

        public boolean isOk() { return code >= 200 && code < 300; }
    }
    
}