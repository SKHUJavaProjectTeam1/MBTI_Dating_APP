package com.mbtidating.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignupRequest {
	@NotBlank String id;
	@NotBlank String userName;
	@NotBlank String pwd;
	String gender;
	Integer age;
	String mbti;
  
}
