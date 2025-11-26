package com.mbtidating.model;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Pattern(
        regexp = "^[a-zA-Z0-9_]{4,20}$",
        message = "아이디는 4~20자의 영문, 숫자, 밑줄(_)만 사용할 수 있습니다."
    )
    private String id;

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(max = 20, message = "닉네임은 최대 20자까지 가능합니다.")
    private String userName;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String pwd;

    @NotBlank(message = "성별은 필수입니다.")
    @Pattern(
        regexp = "^(m|f|o)$",
        message = "성별은 m, f, o 중 하나여야 합니다."
    )
    private String gender;

    @NotNull(message = "나이는 필수입니다.")
    @Min(value = 18, message = "나이는 18세 이상이어야 합니다.")
    @Max(value = 80, message = "나이는 80세 이하여야 합니다.")
    private Integer age;

    @NotBlank(message = "MBTI는 필수입니다.")
    @Pattern(
        regexp = "^(INTJ|INTP|INFJ|INFP|ISTJ|ISFJ|ISTP|ISFP|"
               + "ENTJ|ENTP|ENFJ|ENFP|ESTJ|ESFJ|ESTP|ESFP)$",
        message = "유효하지 않은 MBTI 유형입니다."
    )
    private String mbti;
}
