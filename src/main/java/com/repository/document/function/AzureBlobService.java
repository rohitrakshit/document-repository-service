package com.repository.document.function;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.repository.document.util.DocumentRepositoryUtil;

import jakarta.annotation.PostConstruct;

@Component
public class AzureBlobService {
	
	private static final Logger logger = LoggerFactory.getLogger(AzureBlobService.class);

	@Autowired
	private DocumentRepositoryUtil documentRepositoryUtil;

	@Value("${blob.conn.string}")
	private String blobConnString;

	@Value("${blob.store-container-name}")
	private String storeContainerName;

	@Value("${blob.archive-container-name}")
	private String archiveContainerName;

	private BlobContainerClient storeContainerClient;
	private BlobContainerClient archiveContainerClient;

	@PostConstruct
	public void initialize() {
		storeContainerClient = new BlobContainerClientBuilder().connectionString(blobConnString)
				.containerName(storeContainerName).buildClient();

		archiveContainerClient = new BlobContainerClientBuilder().connectionString(blobConnString)
				.containerName(archiveContainerName).buildClient();
	}

	public String downloadFilesAndZipUpload(Map<String, List<String>> fileArchivePayload) {
		String response = "";
		try {
			Map<String, Map<String, byte[]>> zipFiles = new HashMap<String, Map<String, byte[]>>();

			for (Entry<String, List<String>> zipFileEntry : fileArchivePayload.entrySet()) {
				List<String> fileList = zipFileEntry.getValue();

				Map<String, byte[]> fileContents = new HashMap<String, byte[]>();

				for (String filename : fileList) {
					byte[] fileBytes = getFile(storeContainerClient, filename);
					fileContents.put(filename, fileBytes);
				}
				zipFiles.put(zipFileEntry.getKey(), fileContents);

				byte[] zipBytes = documentRepositoryUtil.zipFiles(zipFiles);

				BlobClient blob = archiveContainerClient.getBlobClient(zipFileEntry.getKey());
				blob.upload(new ByteArrayInputStream(zipBytes), true);
				response = "Successfully archived files";
			}

		} catch (Exception e) {
			logger.error("Exception while file download zipping and uploading the file", e);
			response = "Failed to archive files. "+ e.getMessage();
		}
		return response;
	}

	public String upload(BlobContainerClient blobContainerClient, MultipartFile multipartFile) throws IOException {
		BlobClient blob = blobContainerClient.getBlobClient(multipartFile.getOriginalFilename());
		blob.upload(multipartFile.getInputStream(), multipartFile.getSize(), true);

		return multipartFile.getOriginalFilename();
	}

	public byte[] getFile(BlobContainerClient blobContainerClient, String fileName) throws URISyntaxException {
		BlobClient blob = blobContainerClient.getBlobClient(fileName);

		String content = blob.downloadContent().toString();
		logger.debug("Blob contents: %s%n", content);

//	    
//		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//		blob.download(outputStream);
//		final byte[] bytes = outputStream.toByteArray();
		return content.getBytes();

	}

	public List<String> listBlobs(BlobContainerClient blobContainerClient) {
		PagedIterable<BlobItem> items = blobContainerClient.listBlobs();
		List<String> names = new ArrayList<String>();
		for (BlobItem item : items) {
			names.add(item.getName());
		}
		return names;
	}

	public Boolean deleteBlob(BlobContainerClient blobContainerClient, String blobName) {

		BlobClient blob = blobContainerClient.getBlobClient(blobName);
		blob.delete();
		return true;
	}
	
    /**
     * Check blob exists or not
     * 
     * @param    containerName    Target container
     * @param    fileName        Blob name
     * @return    true/false        flag
     */
    public boolean blobExists(BlobContainerClient blobContainerClient, String fileName) {
        BlobClient blob= blobContainerClient.getBlobClient(fileName);
        return blob.exists();
    }


}
