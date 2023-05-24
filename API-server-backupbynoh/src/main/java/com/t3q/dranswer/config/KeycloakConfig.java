package com.t3q.dranswer.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

	/*
	*  keycloak.json 대신에 Spring Boot yml 파일을 이용하도록 돕는다.
	* */
	@Bean
	public KeycloakSpringBootConfigResolver KeycloakConfigResolver() {
		return new KeycloakSpringBootConfigResolver();
	}

	/*
	*  Keycloak 서버와 통신하기 위한 클라이언트 빌더
	* */
	@Bean
	public Keycloak keycloak() {
		return KeycloakBuilder.builder()
			.serverUrl(Constants.KEYCLOAK_URL)
			.realm(Constants.KEYCLOAK_REALM)
			.grantType(OAuth2Constants.CLIENT_CREDENTIALS)
			.clientId(Constants.KEYCLOAK_CLIENT)
			.clientSecret(Constants.KEYCLOAK_SECRET)
			.build();
	}
}