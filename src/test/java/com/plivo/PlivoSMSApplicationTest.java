package com.plivo;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.plivo.config.PlivoSmsAppConfiguration;
import com.plivo.request.PlivoSms;
import com.plivo.request.Status;

import ratpack.server.RatpackServer;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
		"server.port=8888" }, classes = PlivoSmsAppConfiguration.class, webEnvironment = WebEnvironment.NONE)
public class PlivoSMSApplicationTest {

	private static final String URL_HTTP_PREFIX = "http://localhost:";

	private static final String URL_INBOUND_SMS = "/inbound/sms";

	@Autowired
	private RatpackServer ratpackServer;

	private RestTemplate restTemplate = new RestTemplate();

	@Test
	public void testInboundSms() throws URISyntaxException {
		PlivoSms plivoSms = new PlivoSms("12344565", "4567834535", "Hi I am here");
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.add(HttpHeaders.AUTHORIZATION, getAuthHeader());
		URI uri = new URI(getInboundSmsUrl());
		RequestEntity<PlivoSms> requestEntity = new RequestEntity<>(plivoSms, headers, HttpMethod.POST, uri);
		ResponseEntity<Status> responseEntity = restTemplate.exchange(requestEntity, Status.class);
		assertTrue("Failed to get the Response as 200", responseEntity.getStatusCode().is2xxSuccessful());
		assertTrue("Dint recieved Expected Response",
				"To parameter not found".equals(responseEntity.getBody().getError()));
	}

	@Test
	public void testInboundSmsAuthentication() throws URISyntaxException {
		PlivoSms plivoSms = new PlivoSms("12344565", "4567834535", "Hi I am here");
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.add(HttpHeaders.AUTHORIZATION, getBadAuthHeader());
		URI uri = new URI(getInboundSmsUrl());
		RequestEntity<PlivoSms> requestEntity = new RequestEntity<>(plivoSms, headers, HttpMethod.POST, uri);
		try {
			restTemplate.exchange(requestEntity, Status.class);
		} catch (HttpClientErrorException e) {
			assertTrue("Failed to get the Response as 4xx", e.getStatusCode().is4xxClientError());
			assertTrue("Dint recieved Expected Response for Authentication",
					e.getResponseBodyAsString().contains("Authentication Failed"));
		}
	}

	@Test
	public void testInboundSmsForStopService() throws URISyntaxException {
		PlivoSms plivoSms = new PlivoSms("31221445", "4924195509193", "STOP");
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.add(HttpHeaders.AUTHORIZATION, getAuthHeader());
		URI uri = new URI(getInboundSmsUrl());
		RequestEntity<PlivoSms> requestEntity = new RequestEntity<>(plivoSms, headers, HttpMethod.POST, uri);
		ResponseEntity<Status> responseEntity = restTemplate.exchange(requestEntity, Status.class);
		assertTrue("Failed to get the Response as 200", responseEntity.getStatusCode().is2xxSuccessful());
		assertTrue("Dint recieved Expected Response for Authentication",
				"Service Stopped for 4 hours".equals(responseEntity.getBody().getMessage()));
	}
	
	@Test
	public void testInboundSmsForPhoneNotPresent() throws URISyntaxException {
		PlivoSms plivoSms = new PlivoSms("31221445", "4924195509193234", "STOP");
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		headers.add(HttpHeaders.AUTHORIZATION, getAuthHeader());
		URI uri = new URI(getInboundSmsUrl());
		RequestEntity<PlivoSms> requestEntity = new RequestEntity<>(plivoSms, headers, HttpMethod.POST, uri);
		ResponseEntity<Status> responseEntity = restTemplate.exchange(requestEntity, Status.class);
		assertTrue("Failed to get the Response as 200", responseEntity.getStatusCode().is2xxSuccessful());
		assertTrue("Dint recieved Expected Response for Authentication",
				"To parameter not found".equals(responseEntity.getBody().getError()));
	}
	
	private String getInboundSmsUrl() {
		return URL_HTTP_PREFIX + ratpackServer.getBindPort() + URL_INBOUND_SMS;
	}

	private String getAuthHeader() {
		return Base64.getEncoder().encodeToString("plivo1:20S0KPNOIM".getBytes());
	}

	private String getBadAuthHeader() {
		return Base64.getEncoder().encodeToString("plivo1:20S0KPNOIM123".getBytes());
	}
}
