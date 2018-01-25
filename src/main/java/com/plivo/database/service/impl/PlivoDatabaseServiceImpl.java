/**
 * 
 */
package com.plivo.database.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.plivo.database.service.PlivoDatabaseService;

/**
 * @author Dhawal Patel
 *
 */
@Service
public class PlivoDatabaseServiceImpl implements PlivoDatabaseService {

	private static final Logger logger = LoggerFactory.getLogger(PlivoDatabaseServiceImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public byte[] getUserPassword(String userId) {
		byte[] password = null;
		try {
			password = jdbcTemplate.execute((Connection connection) -> {
				PreparedStatement statement = connection
						.prepareStatement("select auth_id from account where username = ? ");
				statement.setString(1, userId);
				return statement;
			}, (PreparedStatement statement) -> {
				byte[] pass = null;
				ResultSet rs = statement.executeQuery();
				while (rs.next()) {
					String passString = rs.getString(1);
					if (!StringUtils.isEmpty(passString)) {
						pass = passString.getBytes();
					}
				}
				return pass;
			});
		} catch (Exception e) {
			logger.error("Exception occured while calling database for auth service", e);
		}
		return password;
	}

	@Override
	public long getUserAccountId(String userId) {
		long result = 0;
		try {
			result = jdbcTemplate.execute((Connection connection) -> {
				PreparedStatement statement = connection.prepareStatement("select id from account where username = ? ");
				statement.setString(1, userId);
				return statement;
			}, (PreparedStatement ps) -> {
				long dbResult = 0;
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					dbResult = rs.getLong(1);
				}
				return dbResult;
			});
		} catch (Exception e) {
			logger.error("Exception occured while getting account from database", e);
		}
		return result;
	}

	@Override
	public Set<String> getPhoneNumbersForAnAccount(long accountId) {
		Set<String> phoneNumberSet = new HashSet<>();
		try {
			phoneNumberSet = jdbcTemplate.execute((Connection connection) -> {
				PreparedStatement stmt = connection
						.prepareStatement("select number from phone_number where account_id=?");
				stmt.setLong(1, accountId);
				return stmt;
			}, (PreparedStatement ps) -> {
				Set<String> set = new HashSet<>();
				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					set.add(rs.getString(1));
				}
				return set;
			});
		} catch (Exception e) {
			logger.error("Exception occured while fetching phone numbers for account = " + accountId, e);
		}
		return phoneNumberSet;
	}
}
