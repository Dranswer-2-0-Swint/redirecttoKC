package com.t3q.dranswer.service;

import com.t3q.dranswer.config.ApplicationProperties;
import com.t3q.dranswer.dto.db.LoginHistory;
import com.t3q.dranswer.dto.keycloak.KeycloakIntroSpectRes;
import com.t3q.dranswer.dto.keycloak.KeycloakIntroSpectRoleRes;
import com.t3q.dranswer.dto.keycloak.KeycloakTokenRes;
import com.t3q.dranswer.mapper.LoginHistoryMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class KeycloakService {
	
	@Autowired
	LoginHistoryMapper loginHistoryMapper;

	private final RestTemplate restTemplate;
	private final ApplicationProperties applicationProperties;

	@Autowired
	public KeycloakService(ApplicationProperties applicationProperties, RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
		this.applicationProperties = applicationProperties;
	}

	// keycloak login page
	public String getRedirectUrl(HttpServletRequest request) {
		String authUrl		= applicationProperties.getUserAuthUrl();
		String clientId		= applicationProperties.getUserClient();
		String redirectUri	= applicationProperties.getCallbackUrl();
		//String clientId		= "servpot";
		//String redirectUri	= "http://jdi.iptime.org:18080";
		String responseType	= "code";
		String url = String.format("%s?client_id=%s&redirect_uri=%s&response_type=%s&scope=openid", 
                					authUrl, clientId, redirectUri, responseType);
		return url;
	}
	
	// 인증코드로 토큰발급
	public boolean getTokenByAuthorizationCode(HttpServletRequest request, String code) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", 		applicationProperties.getUserClient());
		map.add("client_secret", 	applicationProperties.getUserSecret());
		map.add("grant_type", 		"authorization_code");
		map.add("redirect_uri", 	applicationProperties.getCallbackUrl());
		map.add("code", 			code);

		HttpEntity<MultiValueMap<String, String>> keycloakRequest = new HttpEntity<>(map, headers);
		String url = applicationProperties.getUserTokenUrl();
		try {
			ResponseEntity<KeycloakTokenRes> response = restTemplate.postForEntity(url, keycloakRequest, KeycloakTokenRes.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				request.getSession().setAttribute(applicationProperties.getAccessTokenName(), response.getBody().getAccessToken());
				request.getSession().setAttribute(applicationProperties.getRefreshTokenName(), response.getBody().getRefreshToken());
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return false;
		}
		return true;
	}

	// 토큰검증
	public boolean authorization(HttpServletRequest request) {

		boolean active = true;
		HttpHeaders headers = new HttpHeaders();
		Object accessToken = request.getSession().getAttribute(applicationProperties.getAccessTokenName());
		Object refreshToken = request.getSession().getAttribute(applicationProperties.getRefreshTokenName());
		
		if (accessToken == null || refreshToken == null) {
			log.error("no token");
			return false;
		}

		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", 		applicationProperties.getUserClient());
		map.add("client_secret", 	applicationProperties.getUserSecret());
		map.add("token_type",		"access_token");
		map.add("token",			accessToken.toString());

		HttpEntity<MultiValueMap<String, String>> keycloakRequest = new HttpEntity<>(map, headers);
		String url = applicationProperties.getUserSpecUrl();
		try {
			ResponseEntity<KeycloakIntroSpectRes> response = restTemplate.postForEntity(url, keycloakRequest, KeycloakIntroSpectRes.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				log.info("active : " + response.getBody().isActive());
				active = response.getBody().isActive();
				
//				if (active == true) {
//					Map<String, KeycloakIntroSpectRoleRes> jsonMap = response.getBody().getResourceAccess();
//					for (Map.Entry<String, KeycloakIntroSpectRoleRes> entry : jsonMap.entrySet()) {
//						String clientId = entry.getKey();
//						List<String> roles = entry.getValue().getRoles();
//						log.info("clientId : " + clientId);
//						log.info("roles : " + roles.toString());
//					}
//
//					LoginHistory obj = new LoginHistory();
//					obj.setUserId(response.getBody().getUsername());
//					obj.setAccessToken(accessToken.toString());
//					obj.setRefreshToken(refreshToken.toString());
//					loginHistoryMapper.setLoginHistory(obj);
//
//				} else {
				if (active == false) {
					// clientToken 발급
					String clientToken = null;
					MultiValueMap<String, String> getTokenMap = new LinkedMultiValueMap<>();
					getTokenMap.add("client_id", 		applicationProperties.getUserClient());
					getTokenMap.add("client_secret", 	applicationProperties.getUserSecret());
					getTokenMap.add("grant_type",		"client_credentials");

					HttpEntity<MultiValueMap<String, String>> getTokenRequest = new HttpEntity<>(getTokenMap, headers);
					url = applicationProperties.getUserTokenUrl();
					try {
						ResponseEntity<KeycloakTokenRes> getTokenResponse = restTemplate.postForEntity(url, getTokenRequest, KeycloakTokenRes.class);
						if (response.getStatusCode() == HttpStatus.OK) {
							clientToken = getTokenResponse.getBody().getAccessToken();
						}
					} catch (Exception e) {
						e.printStackTrace();
						log.error(e.getMessage());
						return false;
					}
					
					// refreshToken 발급
					MultiValueMap<String, String> getNewTokenMap = new LinkedMultiValueMap<>();
					getNewTokenMap.add("client_id", 		applicationProperties.getUserClient());
					getNewTokenMap.add("client_secret", 	applicationProperties.getUserSecret());
					getNewTokenMap.add("refresh_token", 	refreshToken.toString());
					getNewTokenMap.add("grant_type",		"refresh_token");

					headers.setBearerAuth(clientToken);
					HttpEntity<MultiValueMap<String, String>> getNewTokenRequest = new HttpEntity<>(getNewTokenMap, headers);
					url = applicationProperties.getUserTokenUrl();
					try {
						ResponseEntity<KeycloakTokenRes> getNewTokenResponse = restTemplate.postForEntity(url, getNewTokenRequest, KeycloakTokenRes.class);
						if (getNewTokenResponse.getStatusCode() == HttpStatus.OK) {
							log.info("new_token_success!");
							request.getSession().setAttribute(applicationProperties.getAccessTokenName(), getNewTokenResponse.getBody().getAccessToken());
							request.getSession().setAttribute(applicationProperties.getRefreshTokenName(), getNewTokenResponse.getBody().getRefreshToken());
							active = true;
						}
					} catch (Exception e) {
						e.printStackTrace();
						log.error(e.getMessage());
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return false;
		}
		return active; 
	}

	public List<LoginHistory> getLoginHistory(HttpServletRequest request) {

		HttpHeaders headers = new HttpHeaders();
		List<LoginHistory> loginHistory = new ArrayList<>();
		
		Object accessToken = request.getSession().getAttribute(applicationProperties.getAccessTokenName());
		Object refreshToken = request.getSession().getAttribute(applicationProperties.getRefreshTokenName());
		
		if (accessToken == null || refreshToken == null) {
			log.error("no token");
			return null;
		}

		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("client_id", 		applicationProperties.getUserClient());
		map.add("client_secret", 	applicationProperties.getUserSecret());
		map.add("token_type",		"access_token");
		map.add("token",			accessToken.toString());

		HttpEntity<MultiValueMap<String, String>> keycloakRequest = new HttpEntity<>(map, headers);
		String url = applicationProperties.getUserSpecUrl();
		try {
			ResponseEntity<KeycloakIntroSpectRes> response = restTemplate.postForEntity(url, keycloakRequest, KeycloakIntroSpectRes.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				if (response.getBody().isActive() == true) {
					loginHistory = loginHistoryMapper.getLoginHistoryByUserId(response.getBody().getUsername());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return null;
		}
		return loginHistory; 
	}
}
