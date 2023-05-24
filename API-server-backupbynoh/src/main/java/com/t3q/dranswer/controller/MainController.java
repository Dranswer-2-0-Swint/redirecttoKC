package com.t3q.dranswer.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.t3q.dranswer.config.Constants;
import com.t3q.dranswer.dto.db.LoginHistory;
import com.t3q.dranswer.service.KeycloakService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
public class MainController {

	@Autowired
	private KeycloakService keycloakService;
	
	@Value("${auth.callback-url}")
	String CALLBACK_URL;

	@RequestMapping(value = "/")
	public String mainPage(HttpServletRequest request, Model model) {
		System.out.println("Mapping to keycloak_login() method");
		boolean active = keycloakService.authorization(request);
		if (active == false) {
			String keycloakRedirectUrl = keycloakService.getRedirectUrl(request);
			return "redirect:" + keycloakRedirectUrl;
		}
		List<LoginHistory> loginHistory = new ArrayList<>();
		loginHistory = keycloakService.getLoginHistory(request);
		model.addAttribute("loginHistory", loginHistory);
		return "main";
	}

	@RequestMapping(value = "/callback")
	public String callbackPage(HttpServletRequest request, @RequestParam("code") String code) {
		System.out.println("Mapping to keycloak_login() method");
		boolean result = false;
		try {
			result = keycloakService.getTokenByAuthorizationCode(request, code);
			if (result == false) {
				log.error("getKeycloakTokenByAuthorizationCode()");
				return "login_fail";
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return "login_fail";
		}
		//return "redirect:http://localhost:8888/callback/callbacktest";
		return "redirect:" + CALLBACK_URL + Constants.KEYCLOAK_CALLBACK_URL + "/callbacktest";
	}

	@RequestMapping(value = "/callback/callbacktest")
	public String callbackTestPage(HttpServletRequest request) {
		System.out.println("Mapping to keycloak_login() method");
		boolean active = false;
		
		try {
			active = keycloakService.authorization(request);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return "login_fail";
		}

		if (active == false) {
			return "login_fail";
		}
		return "login_success";
	}
}
