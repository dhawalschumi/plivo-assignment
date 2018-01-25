/**
 * 
 */
package com.plivo.sms.service.impl;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;

import com.plivo.authentication.service.PlivoAuthenticationService;
import com.plivo.cache.request.PlivoCacheRequest;
import com.plivo.cache.service.PlivoCacheService;
import com.plivo.database.service.PlivoDatabaseService;
import com.plivo.request.PlivoSms;
import com.plivo.sms.service.PlivoSmsService;

import rx.Observable;

/**
 * @author Dhawal Patel
 *
 */
public abstract class PlivoAbstractSmsService implements PlivoSmsService {

	private static final Logger logger = LoggerFactory.getLogger(PlivoAbstractSmsService.class);

	private static String SMS_STOP_VALUE = "STOP";

	@Value("${plivo.sms.service.stop.value}")
	protected String stopString;

	@Value("${plivo.sms.service.stop.redis.ttl.seconds}")
	protected int redisTTL;

	@Value("${plivo.sms.service.counter.ttl}")
	protected int counterTTL;

	@Value("${plivo.sms.service.counter.value}")
	protected int maxCounterValue;

	@Autowired
	private PlivoDatabaseService plivoDatabaseService;

	@Autowired
	private PlivoCacheService plivoCacheService;

	@Autowired
	private PlivoAuthenticationService plivoAuthenticationService;

	protected void saveToCache(PlivoCacheRequest cacheRequest) {
		plivoCacheService.save(cacheRequest).subscribe();
	}

	protected Set<String> getPhoneNumbers(final long accountId) {
		return plivoDatabaseService.getPhoneNumbersForAnAccount(accountId);
	}

	protected Observable<Boolean> checkIfServiceStopped(PlivoSms plivoSms) {
		return plivoCacheService.get(PlivoCacheRequest.getRequest(getStopServiceKey(plivoSms)))
				.flatMap((String value) -> {
					if (SMS_STOP_VALUE.equals(value)) {
						logger.info("Service is stopped for From-To : {}", getStopServiceKey(plivoSms));
						return Observable.just(true);
					}
					return Observable.just(false);
				});
	}

	protected String getStopServiceKey(PlivoSms plivoSms) {
		return plivoSms.getFrom() + "-" + plivoSms.getTo();
	}

	protected boolean checkIfStopTextPresent(String text) {
		if (!StringUtils.isEmpty(text)) {
			text = text.trim();
			text = text.replaceAll("(\r\n|\n\r|\r|\n)", "");
			return stopString.contentEquals(text);
		}
		return false;
	}

	protected Observable<Long> getCounterValue(String key) {
		return plivoCacheService.incrementCounter(PlivoCacheRequest.getRequest(key)).onErrorReturn((ex) -> -1L);
	}

	protected void incrementCounterForKey(String counterKey) {
		plivoCacheService.incrementCounter(PlivoCacheRequest.getRequest(counterKey)).subscribe();
	}

	protected long getAccountIdFromAuthToken(String authToken) {
		String userName = plivoAuthenticationService.getUserNameFromAuthToken(authToken);
		if (StringUtils.isEmpty(userName)) {
			return 0;
		} else {
			return plivoDatabaseService.getUserAccountId(userName);
		}
	}

	protected String getCounterKey(String from) {
		return "COUNTER-" + from;
	}

}
