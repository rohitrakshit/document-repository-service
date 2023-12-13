package gov.cdc.dhcs.hehr.dcp.direct.executor;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;

/**
 * 
 * @author Sai Valluripalli
 *
 */
public class AzureBlobFileUploadExecutor implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(AzureBlobFileUploadExecutor.class);
	
	private Map<String, List<String>> fileArchiveApiPayload;
	
	private String documentFilename;
	
	private byte[] documentData;
	
	private BlobContainerClient storeContainerClient;

	public AzureBlobFileUploadExecutor(BlobContainerClient storeContainerClient, String documentFilename, byte[] documentData) {
		super();
		this.storeContainerClient = storeContainerClient;
		this.documentFilename = documentFilename;
		this.documentData = documentData;
	}

	@Override
	public void run() {
		logger.info("Trying to upload document file to Azure Blob");
		logger.debug("DocumentFilename=", documentFilename);
		logger.debug("FileArchiveApiPayload=", fileArchiveApiPayload);
		BlobClient blob = storeContainerClient.getBlobClient(documentFilename);
		blob.upload(new ByteArrayInputStream(documentData), true);
		logger.info("File upload completed");
	}

	public String toString() {
		return documentFilename;
	}

}
