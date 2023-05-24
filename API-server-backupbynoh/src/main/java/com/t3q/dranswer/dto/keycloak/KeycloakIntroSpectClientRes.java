package com.t3q.dranswer.dto.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class KeycloakIntroSpectClientRes {

	@JsonProperty("realm_access")
	private String realmAccess;
	
	@JsonProperty("active")
	private KeycloakIntroSpectRoleRes	active;
}
