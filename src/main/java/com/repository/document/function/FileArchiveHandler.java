package com.repository.document.function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.cloud.function.adapter.azure.FunctionInvoker;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * @author Rohit Rakshit
 */
public class FileArchiveHandler extends FunctionInvoker<Message<Map<String, List<String>>>, String> {

	@FunctionName("archiveFiles")
	public String execute(@HttpTrigger(name = "req", methods = {HttpMethod.GET,
			HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
		ExecutionContext context) {
		String requestString = request.getBody().get();
		System.out.println("File archive function request received. Payload="+requestString);
		Map<String, List<String>> zipFilesMap = new HashMap<String, List<String>>();
		String errorMessage = "";
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			zipFilesMap = objectMapper.readValue(request.getBody().get(), Map.class);
			System.out.println("zipFilesMap=" + zipFilesMap.toString());
			if(zipFilesMap.size()==0) {
				errorMessage = "Payload object preparation failed";
			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			errorMessage = "Failed to parse payload json value. expected json payload body format {zipFilename:[file1,file2]}.";
			errorMessage = errorMessage + " " +e.getMessage();
		}
		
		if(zipFilesMap.size() == 0 || errorMessage.length() > 0) {
			return errorMessage;
		}
		
		Message<Map<String, List<String>>> message = MessageBuilder.withPayload(zipFilesMap).copyHeaders(request.getHeaders()).build();
		System.out.println("Received zip file request. Message=" + message.getPayload());
		
		return handleRequest(message, context);
	}

}
