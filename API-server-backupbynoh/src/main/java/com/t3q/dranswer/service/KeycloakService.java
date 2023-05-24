package com.t3q.dranswer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.OAuth2Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.t3q.dranswer.config.Constants;
import com.t3q.dranswer.dto.db.LoginHistory;
import com.t3q.dranswer.dto.keycloak.KeycloakIntroSpectRes;
import com.t3q.dranswer.dto.keycloak.KeycloakIntroSpectRoleRes;
import com.t3q.dranswer.dto.keycloak.KeycloakTokenRes;
import com.t3q.dranswer.mapper.LoginHistoryMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class KeycloakService {
	
	@Value("${auth.callback-url}")
	String CALLBACK_URL;
	
	@Autowired
	LoginHistoryMapper loginHistoryMapper;

	// keycloak login page
	public String getRedirectUrl(HttpServletRequest request) {
		String authUrl		= Constants.KEYCLOAK_BASE_URL + Constants.KEYCLOAK_REALM + Constants.KEYCLOAK_AUTH_URL;
		String clientId		= Constants.KEYCLOAK_CLIENT;
		String redirectUri	= CALLBACK_URL + Constants.KEYCLOAK_CALLBACK_URL;
		String responseType	= OAuth2Constants.CODE;
		String url = String.format("%s?client_id=%s&redirect_uri=%s&response_type=%s&scope=openid", 
                					authUrl, clientId, redirectUri, responseType);
		return url;
	}
	
	// 인증코드로 토큰발급
	public boolean getTokenByAuthorizationCode(HttpServletRequest request, String code) {

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(OAuth2Constants.CLIENT_ID, 		Constants.KEYCLOAK_CLIENT);
		map.add(OAuth2Constants.CLIENT_SECRET, 	Constants.KEYCLOAK_SECRET);
		map.add(OAuth2Constants.GRANT_TYPE, 	OAuth2Constants.AUTHORIZATION_CODE);
		map.add(OAuth2Constants.REDIRECT_URI, 	CALLBACK_URL + Constants.KEYCLOAK_CALLBACK_URL);
		map.add(OAuth2Constants.CODE, 			code);

		HttpEntity<MultiValueMap<String, String>> keycloakRequest = new HttpEntity<>(map, headers);
		String url = Constants.KEYCLOAK_BASE_URL + Constants.KEYCLOAK_REALM + Constants.KEYCLOAK_TOKEN_URL;
		try {
			ResponseEntity<KeycloakTokenRes> response = restTemplate.postForEntity(url, keycloakRequest, KeycloakTokenRes.class);
			if (response.getStatusCode() == HttpStatus.OK) {
				request.getSession().setAttribute(Constants.ACCESS_TOKEN_NAME, response.getBody().getAccessToken());
				request.getSession().setAttribute(Constants.REFRESH_TOKEN_NAME, response.getBody().getRefreshToken());
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
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		Object accessToken = request.getSession().getAttribute(Constants.ACCESS_TOKEN_NAME);
		Object refreshToken = request.getSession().getAttribute(Constants.REFRESH_TOKEN_NAME);
		
		if (accessToken == null || refreshToken == null) {
			log.error("no token");
			return false;
		}

		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(OAuth2Constants.CLIENT_ID, 		Constants.KEYCLOAK_CLIENT);
		map.add(OAuth2Constants.CLIENT_SECRET, 	Constants.KEYCLOAK_SECRET);
		map.add(OAuth2Constants.TOKEN_TYPE,		OAuth2Constants.ACCESS_TOKEN);
		map.add(OAuth2Constants.TOKEN,			accessToken.toString());

		HttpEntity<MultiValueMap<String, String>> keycloakRequest = new HttpEntity<>(map, headers);
		String url = Constants.KEYCLOAK_BASE_URL + Constants.KEYCLOAK_REALM + Constants.KEYCLOAK_SPEC_URL;
		try {
			ResponseEntity<KeycloakIntroSpectRes> response = restTemplate.postForEntity(url, keycloakRequest, KeycloakIntroSpectRes.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				log.info("active : " + response.getBody().isActive());
				active = response.getBody().isActive();
				
				if (active == true) {
					Map<String, KeycloakIntroSpectRoleRes> jsonMap = response.getBody().getResourceAccess();
					for (Map.Entry<String, KeycloakIntroSpectRoleRes> entry : jsonMap.entrySet()) {
						String clientId = entry.getKey();
						List<String> roles = entry.getValue().getRoles();
						log.info("clientId : " + clientId);
						log.info("roles : " + roles.toString());
					}
					
					LoginHistory obj = new LoginHistory();
					obj.setUserId(response.getBody().getUsername());
					obj.setAccessToken(accessToken.toString());
					obj.setRefreshToken(refreshToken.toString());
					loginHistoryMapper.setLoginHistory(obj);
					
				} else {
					// clientToken 발급
					String clientToken = null;
					MultiValueMap<String, String> getTokenMap = new LinkedMultiValueMap<>();
					getTokenMap.add(OAuth2Constants.CLIENT_ID, 		Constants.KEYCLOAK_CLIENT);
					getTokenMap.add(OAuth2Constants.CLIENT_SECRET, 	Constants.KEYCLOAK_SECRET);
					getTokenMap.add(OAuth2Constants.GRANT_TYPE,		OAuth2Constants.CLIENT_CREDENTIALS);

					HttpEntity<MultiValueMap<String, String>> getTokenRequest = new HttpEntity<>(getTokenMap, headers);
					url = Constants.KEYCLOAK_BASE_URL + Constants.KEYCLOAK_REALM + Constants.KEYCLOAK_TOKEN_URL;
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
					getNewTokenMap.add(OAuth2Constants.CLIENT_ID, 		Constants.KEYCLOAK_CLIENT);
					getNewTokenMap.add(OAuth2Constants.CLIENT_SECRET, 	Constants.KEYCLOAK_SECRET);
					getNewTokenMap.add(OAuth2Constants.REFRESH_TOKEN, 	refreshToken.toString());
					getNewTokenMap.add(OAuth2Constants.GRANT_TYPE,		OAuth2Constants.REFRESH_TOKEN);

					headers.setBearerAuth(clientToken);
					HttpEntity<MultiValueMap<String, String>> getNewTokenRequest = new HttpEntity<>(getNewTokenMap, headers);
					url = Constants.KEYCLOAK_BASE_URL + Constants.KEYCLOAK_REALM + Constants.KEYCLOAK_TOKEN_URL;
					try {
						ResponseEntity<KeycloakTokenRes> getNewTokenResponse = restTemplate.postForEntity(url, getNewTokenRequest, KeycloakTokenRes.class);
						if (getNewTokenResponse.getStatusCode() == HttpStatus.OK) {
							log.info("new_token_success!");
							request.getSession().setAttribute(Constants.ACCESS_TOKEN_NAME, getNewTokenResponse.getBody().getAccessToken());
							request.getSession().setAttribute(Constants.REFRESH_TOKEN_NAME, getNewTokenResponse.getBody().getRefreshToken());
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

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		List<LoginHistory> loginHistory = new ArrayList<>();
		
		Object accessToken = request.getSession().getAttribute(Constants.ACCESS_TOKEN_NAME);
		Object refreshToken = request.getSession().getAttribute(Constants.REFRESH_TOKEN_NAME);
		
		if (accessToken == null || refreshToken == null) {
			log.error("no token");
			return null;
		}

		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add(OAuth2Constants.CLIENT_ID, 		Constants.KEYCLOAK_CLIENT);
		map.add(OAuth2Constants.CLIENT_SECRET, 	Constants.KEYCLOAK_SECRET);
		map.add(OAuth2Constants.TOKEN_TYPE,		OAuth2Constants.ACCESS_TOKEN);
		map.add(OAuth2Constants.TOKEN,			accessToken.toString());

		HttpEntity<MultiValueMap<String, String>> keycloakRequest = new HttpEntity<>(map, headers);
		String url = Constants.KEYCLOAK_BASE_URL + Constants.KEYCLOAK_REALM + Constants.KEYCLOAK_SPEC_URL;
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
