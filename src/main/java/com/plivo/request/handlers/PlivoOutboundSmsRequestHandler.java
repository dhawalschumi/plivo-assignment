/**
 * 
 */
package com.plivo.request.handlers;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.plivo.request.PlivoSms;
import com.plivo.request.Status;

import ratpack.handling.Context;
import ratpack.jackson.Jackson;
import ratpack.rx.RxRatpack;

/**
 * @author Dhawal Patel
 *
 */
@Service
public class PlivoOutboundSmsRequestHandler extends PlivoBaseRequestHandler {

	private static final Logger logger = LoggerFactory.getLogger(PlivoOutboundSmsRequestHandler.class);

	@Override
	public void handle(Context context) throws Exception {
		context.getRequest().getBody().then(body -> {
			PlivoSms plivoSms = extractSmsObject(body);
			if (plivoSms == null) {
				logger.error("Sms Object is null. Malformed Json in request");
				context.getResponse().status(400);
				context.render(Jackson.json(new Status("Malformed Json in Request", "Bad Request")));
			}
			List<Status> list = plivoSms.validate();
			if (!CollectionUtils.isEmpty(list)) {
				context.getResponse().status(400);
				context.render(Jackson.json(list));
			} else {
				RxRatpack
						.promiseSingle(plivoSmsService.serveOutBoundSms(plivoSms,
								context.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION)))
						.then(status -> context.render(Jackson.json(status)));
			}
		});
	}
}
