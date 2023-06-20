package com.t3q.dranswer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "env")
public class ApplicationProperties {

	private String authUrl;

	private String userRealm;
	private String userClient;
	private String userSecret;
	private String userAuthUrl;
	private String userTokenUrl;
	private String userSpecUrl;

	private String systemRealm;
	private String systemClient;
	private String systemSecret;
	private String systemAuthUrl;
	private String systemTokenUrl;
	private String systemSpecUrl;

	private String cmanUrl;
	private String callbackUrl;

	private String accessTokenName;
	private String refreshTokenName;

}
