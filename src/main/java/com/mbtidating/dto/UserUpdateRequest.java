package com.mbtidating.dto;

import lombok.Data;

import java.util.Map;

@Data
public class UserUpdateRequest {
	private String userName;
    private String gender;
    private String profileImg;
    private Integer age;
    private Map<String, String> mbti; // EI, SN, TF, JP
}
