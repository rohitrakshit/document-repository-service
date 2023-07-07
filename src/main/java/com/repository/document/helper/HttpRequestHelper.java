package com.repository.document.helper;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class HttpRequestHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(HttpRequestHelper.class);

	private final RestTemplate restTemplate;
	
	private ObjectMapper mapper = new ObjectMapper();

	public HttpRequestHelper() {
		this.restTemplate = new RestTemplate();
	}

	public void postRequest(String url, Map<String,List<String>> parameter) {
		try {
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, mapper.writeValueAsString(parameter), String.class);
		String response = responseEntity.getBody();
		logger.debug("Response from json file archive Azure function="+ response);
		} catch(Exception e) {
			String errMsg = "Exception invoking Azure function to archive xml files. Message="+ e.getMessage();
			logger.error(errMsg, e);
			throw new RuntimeException(errMsg);
		}
	}
}
