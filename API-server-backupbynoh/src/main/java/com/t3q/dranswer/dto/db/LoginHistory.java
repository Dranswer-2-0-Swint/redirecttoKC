package com.t3q.dranswer.dto.db;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LoginHistory {

	@JsonProperty("id")
	private int id;
	
	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("access_token")
	private String accessToken;
	
	@JsonProperty("refresh_token")
	private String refreshToken;
	
	@JsonProperty("login_time")
	private String loginTime;
}
