/**
 * 
 */
package com.plivo.request.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plivo.request.PlivoSms;
import com.plivo.sms.service.PlivoSmsService;

import ratpack.handling.Handler;
import ratpack.http.TypedData;

/**
 * @author Dhawal Patel
 *
 */
public abstract class PlivoBaseRequestHandler implements Handler {

	private static final Logger logger = LoggerFactory.getLogger(PlivoBaseRequestHandler.class);

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	protected PlivoSmsService plivoSmsService;

	protected PlivoSms extractSmsObject(TypedData body) {
		PlivoSms plivoSms = null;
		try {
			plivoSms = mapper.readValue(body.getText(), PlivoSms.class);
		} catch (Exception e) {
			logger.error("Exception Occured while transforming json to object", e);
		}
		return plivoSms;
	}

}
