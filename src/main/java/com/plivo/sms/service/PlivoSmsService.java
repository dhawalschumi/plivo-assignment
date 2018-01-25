/**
 * 
 */
package com.plivo.sms.service;

import com.plivo.request.PlivoSms;
import com.plivo.request.Status;

import rx.Observable;

/**
 * @author Dhawal Patel
 *
 */
public interface PlivoSmsService {

	Observable<Status> serveInboundSms(PlivoSms plivoSms, String authToken);

	Observable<Status> serveOutBoundSms(PlivoSms plivoSms, String authToken);

}
