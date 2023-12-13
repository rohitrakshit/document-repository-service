package gov.cdc.dhcs.hehr.dcp.direct.function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.function.adapter.azure.FunctionInvoker;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * @author Sai Valluripalli
 */
public class FileArchiveHandler extends FunctionInvoker<Message<Map<String, List<String>>>, String> {
	private static final Logger logger = LoggerFactory.getLogger(FileArchiveHandler.class);
	@SuppressWarnings("unused")
	private static final String CONNECTION_NAME = "AzureWebJobsStorage";

	@FunctionName("archiveFiles")
	public String execute(@HttpTrigger(name = "req", methods = { HttpMethod.GET,
			HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
			ExecutionContext context) {
		String requestString = request.getBody().get();
		logger.debug("File archive function request received. Payload=" + requestString);
		Map<String, List<String>> zipFilesMap = new HashMap<String, List<String>>();
		String errorMessage = "";
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			zipFilesMap = objectMapper.readValue(request.getBody().get(), Map.class);
			logger.debug("zipFilesMap=" + zipFilesMap.toString());
			if (zipFilesMap.size() == 0) {
				errorMessage = "Payload object preparation failed";
			}
		} catch (JsonProcessingException e) {
			logger.error("JsonProcessingException while executing FileArchive Function App HttpTrigger", e);
			errorMessage = "Failed to parse payload json value. expected json payload body format {zipFilename:[file1,file2]}.";
			errorMessage = errorMessage + " " + e.getMessage();
		}

		if (zipFilesMap.size() == 0 || errorMessage.length() > 0) {
			return errorMessage;
		}

		Message<Map<String, List<String>>> message = MessageBuilder.withPayload(zipFilesMap)
				.copyHeaders(request.getHeaders()).build();
		logger.debug("Received zip file request. Message=" + message.getPayload());

		return handleRequest(message, context);
	}
}
