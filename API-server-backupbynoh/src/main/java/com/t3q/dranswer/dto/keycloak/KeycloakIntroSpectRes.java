package com.t3q.dranswer.dto.keycloak;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class KeycloakIntroSpectRes {

	private int	exp;
	private int	iat;
	private String jti;
	private String iss;
	private String aud;
	private String sub;
	private String typ;
	private String azp;
	
	@JsonProperty("session_state")
	private String sessionState;
	
	@JsonProperty("preferred_username")
	private String	preferredUsername;
	
	@JsonProperty("email_verified")
	private String emailVerified;
	
	private String acr;
	
	@JsonProperty("realm_access")
	private KeycloakIntroSpectRoleRes realmAccess;
	
	@JsonProperty("resource_access")
	//private KcIntroSpectRoleRes resourceAccess;
	private Map<String, KeycloakIntroSpectRoleRes> resourceAccess;
	
	private String scope;
	private String sid;

	@JsonProperty("client_id")
	private String clientId;
	
	private String username;

	@JsonProperty("active")
	private boolean	active;
}
