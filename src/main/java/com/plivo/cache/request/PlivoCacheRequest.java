/**
 * 
 */
package com.plivo.cache.request;

/**
 * @author Dhawal Patel
 *
 */
public class PlivoCacheRequest {

	private String key;

	private String value;

	private Integer ttl;

	private PlivoCacheRequest(String key, String value, Integer ttl) {
		super();
		this.key = key;
		this.value = value;
		this.ttl = ttl;
	}

	public static PlivoCacheRequest getRequest(String key) {
		return new PlivoCacheRequest(key, null, null);
	}

	public static PlivoCacheRequest saveRequest(String key, String value, int ttl) {
		return new PlivoCacheRequest(key, value, ttl);
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public Integer getTtl() {
		return ttl;
	}

}
