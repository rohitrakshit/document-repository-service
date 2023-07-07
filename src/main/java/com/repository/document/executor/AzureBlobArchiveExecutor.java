package com.repository.document.executor;

import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.repository.document.helper.HttpRequestHelper;

import org.slf4j.Logger;

public class AzureBlobArchiveExecutor implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(AzureBlobArchiveExecutor.class);
	
	private Map<String, List<String>> fileArchiveApiPayload;
	
	private String fileArchiveApiUrl;
	
	private HttpRequestHelper httpRequestHelper;

	public AzureBlobArchiveExecutor(HttpRequestHelper httpRequestHelper, String fileArchiveApiUrl, Map<String, List<String>> fileArchiveApiPayload) {
		super();
		this.httpRequestHelper = httpRequestHelper;
		this.fileArchiveApiUrl = fileArchiveApiUrl;
		this.fileArchiveApiPayload = fileArchiveApiPayload;
	}

	@Override
	public void run() {
		logger.info("Trying to invoke file archiver Function App");
		logger.debug("FileArchiveApiUrl=", fileArchiveApiUrl);
		logger.debug("FileArchiveApiPayload=", fileArchiveApiPayload);
		httpRequestHelper.postRequest(fileArchiveApiUrl, fileArchiveApiPayload);
		logger.info("File archive request to Function App completed");
	}
	
	public String toString() {
		return "File .zip archive";
	}

}
