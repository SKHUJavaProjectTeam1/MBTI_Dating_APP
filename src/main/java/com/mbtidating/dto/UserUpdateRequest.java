package com.mbtidating.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UserUpdateRequest {
    private String gender;
    private Integer age;
    private Map<String, String> mbti; // EI, SN, TF, JP
}
