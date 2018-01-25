/**
 * 
 */
package com.plivo.database.service;

import java.util.Set;

/**
 * @author Dhawal Patel
 *
 */
public interface PlivoDatabaseService {

	public byte[] getUserPassword(String userId);

	public long getUserAccountId(String userId);

	public Set<String> getPhoneNumbersForAnAccount(long accountId);
}
