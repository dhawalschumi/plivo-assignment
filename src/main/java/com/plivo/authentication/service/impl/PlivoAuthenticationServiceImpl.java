/**
 * 
 */
package com.plivo.authentication.service.impl;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.plivo.authentication.service.PlivoAuthenticationService;
import com.plivo.database.service.PlivoDatabaseService;
import com.plivo.request.Status;

import ratpack.handling.Context;
import ratpack.jackson.Jackson;

/**
 * @author Dhawal Patel
 *
 */
@Service
public class PlivoAuthenticationServiceImpl implements PlivoAuthenticationService {

	private static final Logger logger = LoggerFactory.getLogger(PlivoAuthenticationServiceImpl.class);

	private static final String AUTH_TOKEN_SEPERATOR = ":";

	@Autowired
	private PlivoDatabaseService plivoDatabaseService;

	@Override
	public boolean authenticate(String authToken) {
		if (StringUtils.isEmpty(authToken)) {
			return false;
		}
		boolean isAuthSuccessful = false;
		byte[] authString = null;
		try {
			authString = Base64.getDecoder().decode(authToken);
			String auth = new String(authString);
			String[] authArray = auth.split(AUTH_TOKEN_SEPERATOR);
			if (StringUtils.isEmpty(authArray[0])) {
				logger.error("Empty User Name for Authorization");
				return false;
			}
			if (StringUtils.isEmpty(authArray[1])) {
				logger.error("Empty Auth Password for Authorization");
				return false;
			}
			String userName = authArray[0];
			String password = authArray[1];
			isAuthSuccessful = validateUserAndPassword(userName, password);
		} catch (Exception e) {
			logger.error("Exception occured while Authorization", e);
			return false;
		}
		return isAuthSuccessful;
	}

	private boolean validateUserAndPassword(String userName, String password) {
		byte[] pass = plivoDatabaseService.getUserPassword(userName);
		if (pass != null) {
			if (password.equals(new String(pass))) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void handle(Context ctx) throws Exception {
		if (!authenticate(ctx.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION))) {
			ctx.getResponse().status(403);
			ctx.render(Jackson.json(new Status("Authentication Failed", "Invalid/Missing AuthToken")));
		} else {
			ctx.next();
		}
	}

	@Override
	public String getUserNameFromAuthToken(String authToken) {
		byte[] authTokenString = Base64.getDecoder().decode(authToken);
		String authTokenDecoded = new String(authTokenString);
		String userName = null;
		if (!StringUtils.isEmpty(authTokenString)) {
			String[] authArray = authTokenDecoded.split(AUTH_TOKEN_SEPERATOR);
			userName = authArray[0];
		}
		return userName;
	}
}