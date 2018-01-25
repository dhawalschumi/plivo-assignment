/**
 * 
 */
package com.plivo.logger;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.net.HostAndPort;

import ratpack.handling.RequestLogger;
import ratpack.handling.RequestOutcome;
import ratpack.http.HttpMethod;
import ratpack.http.Request;
import ratpack.http.SentResponse;
import ratpack.http.Status;
import ratpack.http.internal.HttpHeaderConstants;

/**
 * @author Dhawal Patel
 *
 */
@Service
public class PlivoNcsaRequestLogger implements RequestLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(PlivoNcsaRequestLogger.class);

	@Override
	public void log(RequestOutcome outcome) {
		if (!LOGGER.isInfoEnabled()) {
			return;
		}
		Request request = outcome.getRequest();
		SentResponse response = outcome.getResponse();
		String responseSize = "-";
		String contentLength = response.getHeaders().get(HttpHeaderConstants.CONTENT_LENGTH);
		if (contentLength != null) {
			responseSize = contentLength;
		}
		StringBuilder logLine = new StringBuilder().append(formatLog(request.getRemoteAddress(), request.getMethod(),
				"/" + request.getPath(), request.getProtocol(), outcome.getResponse().getStatus(), responseSize));

		long responseTime = TimeUnit.MILLISECONDS.convert(outcome.getDuration().getSeconds(), TimeUnit.SECONDS)
				+ TimeUnit.MILLISECONDS.convert(outcome.getDuration().getNano(), TimeUnit.NANOSECONDS);
		logLine.append(" RT=").append(responseTime).append("ms");
		LOGGER.info(logLine.toString());
	}

	String formatLog(HostAndPort client, HttpMethod method, String uri, String httpProtocol, Status status,
			String responseSize) {
		return String.format("%s \"%s %s %s\" %d %s", client.getHost(), method.getName(), uri, httpProtocol,
				status.getCode(), responseSize);
	}

}
