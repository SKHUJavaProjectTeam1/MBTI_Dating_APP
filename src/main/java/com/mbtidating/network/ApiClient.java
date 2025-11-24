package com.mbtidating.network;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";

    // ✅ 기본 POST  (BASE_URL + path)
    public static HttpResult post(String path, String json) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        String body = readAll(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
        return new HttpResult(code, body);
    }

    // ✅ JWT 토큰 포함 POST (인증 필요할 때)
    public static HttpResult post(String path, String json, String jwtToken) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        if (jwtToken != null && !jwtToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        String body = readAll(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
        return new HttpResult(code, body);
    }

    // ✅ 전체 URL을 직접 넘기는 POST (기존 LoginView 호환용)
    public static HttpResult postJson(String url, String json) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        String body = readAll(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
        return new HttpResult(code, body);
    }

    // ✅ 기본 GET  (BASE_URL + path)
    public static HttpResult get(String path) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int code = conn.getResponseCode();
        String body = readAll(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
        return new HttpResult(code, body);
    }

    // ✅ JWT 토큰 포함 GET (필요하면 사용)
    public static HttpResult get(String path, String jwtToken) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (jwtToken != null && !jwtToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }

        int code = conn.getResponseCode();
        String body = readAll(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
        return new HttpResult(code, body);
    }

    // ✅ NEW: 기본 PUT (BASE_URL + path, JWT 포함 버전만 사용)
    public static HttpResult put(String path, String json, String jwtToken) throws IOException {
        URL url = new URL(BASE_URL + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        if (jwtToken != null && !jwtToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        String body = readAll(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
        return new HttpResult(code, body);
    }

    // ✅ (선택) 전체 URL을 직접 넘기는 PUT
    //   필요 없으면 안 써도 되는데, 혹시 HomeView 쪽에서 full URL 쓰고 싶으면 사용
    public static HttpResult putJson(String url, String json, String jwtToken) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        if (jwtToken != null && !jwtToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + jwtToken);
        }
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        String body = readAll(code >= 400 ? conn.getErrorStream() : conn.getInputStream());
        return new HttpResult(code, body);
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
