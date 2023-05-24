package com.t3q.dranswer.config;

public class Constants {

	public static final String ACCESS_TOKEN_NAME		= "dr_access_token";
	public static final String REFRESH_TOKEN_NAME		= "dr_refresh_token";
	
	
	// naver cloud
	public static final String KEYCLOAK_REALM 			= "service-user-dev";
	public static final String KEYCLOAK_CLIENT 			= "login";
	public static final String KEYCLOAK_SECRET 			= "JG6eaVSJVzbUp5Sgp7MAyByrraZX7xNC";
	public static final String KEYCLOAK_SERVER 			= "http://27.96.130.179:5010/";
	
	public static final String KEYCLOAK_URL 			= KEYCLOAK_SERVER + "auth/";
	public static final String KEYCLOAK_BASE_URL 		= KEYCLOAK_URL + "realms/";
	public static final String KEYCLOAK_CALLBACK_URL 	= "/callback";
	
	public static final String KEYCLOAK_AUTH_URL 		= "/protocol/openid-connect/auth";
	public static final String KEYCLOAK_TOKEN_URL 		= "/protocol/openid-connect/token";
	public static final String KEYCLOAK_SPEC_URL 		= "/protocol/openid-connect/token/introspect";
	
	public static final String KEYCLOAK_TOKEN_TYPE_HINT	= "token_type_hint";
	
	public static final String KEYCLOAK_USER_ID 		= "asan";
	public static final String KEYCLOAK_USER_PW 		= "123";

//	// keycloak grant_type
//	public static final String CLIENT_CREDENTIALS 	= "client_credentials";
//	// keycloak scope
//	public static final String SCOPE_OPENID 		= "openid";
//	public static final String SCOPE_PROFILE 		= "profile";
//	public static final String SCOPE_EMAIL 			= "email";
//	public static final String SCOPE_ADDRESS 		= "address";
//	public static final String SCOPE_PHONE 			= "phone";

}
