/**
 * 
 */
package com.plivo.request;

/**
 * @author Dhawal Patel
 *
 */
public class Status {

	private String message;

	private String error;

	public Status(String messsage, String error) {
		this.error = error;
		this.message = messsage;
	}

	public String getMessage() {
		return message;
	}

	public String getError() {
		return error;
	}
}
