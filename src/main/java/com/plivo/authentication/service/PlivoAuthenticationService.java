/**
 * 
 */
package com.plivo.authentication.service;

import ratpack.handling.Handler;

/**
 * @author Dhawal
 *
 */
public interface PlivoAuthenticationService extends Handler {
	
	public boolean authenticate(String authToken);
	
	public String getUserNameFromAuthToken(String authToken);
}
