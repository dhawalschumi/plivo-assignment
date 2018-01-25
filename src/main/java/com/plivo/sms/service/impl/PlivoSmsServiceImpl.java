/**
 * 
 */
package com.plivo.sms.service.impl;

import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.plivo.cache.request.PlivoCacheRequest;
import com.plivo.request.PlivoSms;
import com.plivo.request.Status;

import rx.Observable;

/**
 * @author Dhawal Patel
 *
 */
@Service
public class PlivoSmsServiceImpl extends PlivoAbstractSmsService {

	private static final Logger logger = LoggerFactory.getLogger(PlivoSmsServiceImpl.class);

	@Override
	public Observable<Status> serveInboundSms(PlivoSms plivoSms, String authToken) {
		return Observable.just(plivoSms).flatMap(plivoSmsObj -> {
			long accountId = getAccountIdFromAuthToken(authToken);
			if (accountId <= 0) {
				return Observable.just(new Status("Invalid User Account", "User Account not present"));
			}
			Set<String> phoneNumbers = getPhoneNumbers(accountId);
			if (!CollectionUtils.isEmpty(phoneNumbers)) {
				if (phoneNumbers.contains(plivoSmsObj.getTo())) {
					if (checkIfStopTextPresent(plivoSmsObj.getText())) {
						logger.info("Stopping Service for from:{} and to:{}", plivoSms.getFrom(), plivoSms.getTo());
						saveToCache(PlivoCacheRequest.saveRequest(getStopServiceKey(plivoSms), "STOP", redisTTL));
						return Observable.just(new Status("Service Stopped for 4 hours", ""));
					}
					logger.info("Inbound SMS is sent from:{} and to: ", plivoSms.getFrom(), plivoSms.getTo());
					return Observable.just(new Status("Inbound sms ok", ""));
				} else {
					logger.info("To Number {} is not mapped", plivoSms.getTo());
					return Observable.just(new Status("", "To parameter not found"));
				}
			}
			return Observable.just(new Status("", "No Phone Numbers Mapped to Account"));
		}).onErrorReturn((Throwable e) -> {
			return new Status("", "unknown failure");
		});
	}

	@Override
	public Observable<Status> serveOutBoundSms(PlivoSms plivoSms, String authToken) {
		long accountId = getAccountIdFromAuthToken(authToken);
		if (accountId <= 0) {
			return Observable.just(new Status("Invalid User Account", "User Account not present"));
		}
		return checkIfServiceStopped(plivoSms).flatMap(isServiceStopped -> {
			if (isServiceStopped) {
				logger.info("Service for From:{} to To:{} is stopped", plivoSms.getFrom(), plivoSms.getTo());
				return Observable.just(new Status("",
						"Sms from " + plivoSms.getFrom() + " to " + plivoSms.getTo() + " blocked by STOP request"));
			}
			logger.info("Service is active from {} to {}", plivoSms.getFrom(), plivoSms.getTo());
			return Observable.empty();
		}).switchIfEmpty(getCounterValue(getCounterKey(plivoSms.getFrom())).flatMap(counterValue -> {
			if (counterValue == -1) {
				return Observable.just(new Status("", "Unknown Error Occured"));
			} else if (counterValue < maxCounterValue) {
				return Observable.just(new Status("outbound sms ok", ""));
			} else {
				return Observable.just(new Status("", "limit reached for from : " + plivoSms.getFrom()));
			}
		}));
	}
}
