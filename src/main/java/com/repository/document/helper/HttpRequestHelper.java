package com.repository.document.helper;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class HttpRequestHelper {

	private final RestTemplate restTemplate;
	
	private ObjectMapper mapper = new ObjectMapper();

	public HttpRequestHelper() {
		this.restTemplate = new RestTemplate();
	}

	public void postRequest(String url, Map<String,List<String>> parameter) {
		try {
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, mapper.writeValueAsString(parameter), String.class);
		String response = responseEntity.getBody();
		System.out.println("Response from json file archive Azure function="+ response);
		} catch(Exception e) {
			String errMsg = "Exception invoking Azure function to archive xml files. Message="+ e.getMessage();
			System.err.println(errMsg);
			throw new RuntimeException(errMsg);
		}
	}
}
