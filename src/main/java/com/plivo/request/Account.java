/**
 * 
 */
package com.plivo.request;

/**
 * @author Dhawal Patel
 *
 */
public final class Account {

	private String userName;

	private byte[] password;

	private long accountId;

	public Account(String userName, byte[] password, long accountId) {
		super();
		this.userName = userName;
		this.password = password;
		this.accountId = accountId;
	}

	public String getUserName() {
		return userName;
	}

	public byte[] getPassword() {
		return password;
	}

	public long getAccountId() {
		return accountId;
	}

}
