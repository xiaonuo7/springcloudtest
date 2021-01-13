package com.csii.pe.gateway.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExceptionAdpController {
	@RequestMapping("/login_auth_fail")
	public void loginAuthFail(HttpServletRequest request) throws ValidationException {
	  // 此处构造一个合适的异常并抛出即可
	  String code = (String) request.getAttribute("code");
	  throw new ValidationException(code);
	}
	public class ValidationException extends Exception{

		public ValidationException(String code) {
			
		}
		
	}
}