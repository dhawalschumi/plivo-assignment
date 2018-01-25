/**
 * 
 */
package com.plivo.request;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;

/**
 * @author Dhawal Patel
 *
 */
public class PlivoSms {

	private String from;

	private String to;

	private String text;

	public PlivoSms() {
	}

	public PlivoSms(String from, String to, String text) {
		this.from = from;
		this.to = to;
		this.text = text;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getText() {
		return text;
	}

	public List<Status> validate() {
		List<Status> errorList = new ArrayList<>();
		if (StringUtils.isEmpty(from)) {
			errorList.add(new Status("Missing From Data", "'from' parameter is missing"));
		} else if ((from.length() < 6) || (from.length() > 16)) {
			errorList.add(new Status("Invalid From Data", "'from' parameter is invalid"));
		}
		if (StringUtils.isEmpty(to)) {
			errorList.add(new Status("Missing To Data", "'to' parameter is missing"));
		} else if ((to.length() < 6) || (to.length() > 16)) {
			errorList.add(new Status("Invalid To Data", "'to' parameter is invlida"));
		}
		if (StringUtils.isEmpty(text)) {
			errorList.add(new Status("Missing Text Data", "'text' parameter is missing"));
		} else if ((text.length() < 1) || (text.length() > 120)) {
			errorList.add(new Status("Inva lid From Data", "'text' parameter is invalid"));
		}

		return errorList;
	}

}
