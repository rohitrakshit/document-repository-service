package com.repository.document.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.repository.document.constants.DocumentRepositoryMessages;
import com.repository.document.function.AzureBlobService;
import com.repository.document.helper.HttpRequestHelper;
import com.repository.document.util.DocumentRepositoryUtil;

import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType.Document;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.DocumentResponse;
import io.netty.util.internal.StringUtil;
import jakarta.activation.DataHandler;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Marshaller;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotListType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryErrorList;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

@Endpoint
public class DocumentRepositoryEndpoint {

	private static final String NAMESPACE_URI = "urn:ihe:iti:xds-b:2007";
	private static final DateTimeFormatter FILENAME_DATETIME_FORMAT = DateTimeFormatter
			.ofPattern("dd-MMM-yyyy_HH-mm-ss").withZone(ZoneId.of("UTC"));
	private static final DateTimeFormatter REQUEST_ID_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyyHHmmss")
			.withZone(ZoneId.of("UTC"));
	private static final String PROVIDE_AND_REGISTER_FILENAME_PREFIX = "ProvideAndRegisterDocument_";
	private static final String RETRIEVE_DOCUMENT_FILENAME_PREFIX = "RetrieveDocument_";

	@Value("${blob.conn.string}")
	private String blobConnString;

	@Value("${blob.store-container-name}")
	private String storeContainerName;

	@Value("${blob.archive-container-name}")
	private String archiveContainerName;
	
	@Value("${file-archive.api.url}")
	private String fileArchiveApiUrl;

	private BlobContainerClient storeContainerClient;
	private BlobContainerClient archiveContainerClient;

	@PostConstruct
	public void initialize() {
		storeContainerClient = new BlobContainerClientBuilder().connectionString(blobConnString)
				.containerName(storeContainerName).buildClient();

		archiveContainerClient = new BlobContainerClientBuilder().connectionString(blobConnString)
				.containerName(archiveContainerName).buildClient();
	}

	@Autowired
	private DocumentRepository documentRepository;

	@Autowired
	private AzureBlobService abs;

	@Autowired
	private DocumentRepositoryUtil documentRepositoryUtil;
	
	@Autowired
	private HttpRequestHelper httpRequestHelper;

	/**
	 * @param request
	 * @return
	 */
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "ProvideAndRegisterDocumentSetRequest")
	@ResponsePayload
	public JAXBElement<RegistryResponseType> provideAndRegisterDocumentSetRequestRequest(
			@RequestPayload JAXBElement<ProvideAndRegisterDocumentSetRequestType> request) {

		String fullReqStr = toXml(request);
		// System.out.println("Full Request String= " + fullReqStr);
		RegistryResponseType response = new RegistryResponseType();
		QName qname = new QName(NAMESPACE_URI, "Success", "env");

		Temporal temporal = Instant.now();
		// String dateTime = FILENAME_DATETIME_FORMAT.format(temporal);
		String reqId = REQUEST_ID_FORMAT.format(temporal);
		String requestId = reqId + getAlphaNumericString(8);
		String documentUniqueId = StringUtil.EMPTY_STRING;
		List<String> fileList = new ArrayList<String>();
		System.out.println("ProvideAndRegisterDocumentSetRequest received. RequestID=" + requestId);

		SlotListType slotListType = new SlotListType();
		RegistryErrorList registryErrorList = new RegistryErrorList();

		ProvideAndRegisterDocumentSetRequestType pardsrt = request.getValue();

		List<Document> documentList = pardsrt.getDocument();
		try {
			if (documentList != null && documentList.size() > 0) {
				for (int i = 0; i < documentList.size(); i++) {
					Document document = documentList.get(i);
					String documentId = document.getId();
					// Use only the first documentId for the azure blob file name
					if (!StringUtils.hasText(documentUniqueId)) {
						documentUniqueId = documentId;
					}
					if (!StringUtils.hasText(documentId)) {
						throw new RuntimeException(DocumentRepositoryMessages.DOCUMENT_UNIQUE_ID_NOT_PROVIDED);
					}
				}
				
				documentList.stream().forEach(document -> {
					String documentId = document.getId();
					DataHandler dataHandler = document.getValue();
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					try {
						dataHandler.writeTo(output);
					} catch (IOException e) {
						e.printStackTrace();
					}

					String documentData = new String(Base64.getEncoder().encode(output.toByteArray()));
					documentRepository.storeDocument(documentId, documentData);
					
					String dataDocumentFileName = new StringBuilder(PROVIDE_AND_REGISTER_FILENAME_PREFIX).append(documentId.replace(":", "_")).append("_").append(requestId).toString() + "_req_document.xml";
					BlobClient blob = storeContainerClient.getBlobClient(dataDocumentFileName);
					blob.upload(new ByteArrayInputStream(decodeBase64StringToByteArray(documentData)), true);
					System.out.println("Successfully uploaded data document file " + dataDocumentFileName + " to Azure blob");
					fileList.add(dataDocumentFileName);
					
					SlotType1 slotType1 = new SlotType1();
					slotType1.setName(documentId);
					slotType1.setSlotType("SUCCESS");
					slotListType.getSlot().add(slotType1);
				});
			} else {
				throw new RuntimeException(DocumentRepositoryMessages.DOCUMENT_NOT_PROVIDED);
			}

			response.setRequestId(requestId);
			response.setStatus("SUCCESS: Documents stored successfully");

		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = e.getMessage();
//			RegistryError registryError = new RegistryError();
//			registryError.setCodeContext("");
//			registryError.setErrorCode("400");
//			registryError.setLocation("");
//			registryError.setSeverity("");
//			registryError.setValue(errorMessage);
//			registryErrorList.getRegistryError().add(registryError);
//			response.setRegistryErrorList(registryErrorList);
			response.setStatus("ERROR: Failed to store all documents");
			throw new RuntimeException("ERROR: Failed to store documents. " + errorMessage);
		}
		response.setResponseSlotList(slotListType);
		response.setRegistryErrorList(registryErrorList);

		JAXBElement<RegistryResponseType> jaxbEl = new JAXBElement<RegistryResponseType>(qname,
				RegistryResponseType.class, response);
		
		String blobFilename = new StringBuilder(PROVIDE_AND_REGISTER_FILENAME_PREFIX).append(documentUniqueId.replace(":", "_")).append("_").append(requestId).toString();

		String requestBlobFilename = blobFilename + "_request.xml";
		BlobClient blob = storeContainerClient.getBlobClient(requestBlobFilename);
		blob.upload(new ByteArrayInputStream(fullReqStr.getBytes(StandardCharsets.UTF_8)), true);
		System.out.println("Successfully uploaded request xml file " + requestBlobFilename + " to Azure blob");

		String responseBlobFilename = blobFilename + "_response.xml";
		blob = storeContainerClient.getBlobClient(responseBlobFilename);
		blob.upload(new ByteArrayInputStream(toXml(jaxbEl).getBytes(StandardCharsets.UTF_8)), true);
		System.out.println("Successfully uploaded response xml file " + responseBlobFilename + " to Azure blob");
		System.out.println("ProvideAndRegisterDocumentSetRequest Completed. RequestID=" + requestId);

//		try {
//			Map<String, Map<String, byte[]>> zipFiles = new HashMap<String, Map<String, byte[]>>();
//
//			Map<String, byte[]> fileContents = new HashMap<String, byte[]>();
//
//			byte[] fileBytes = abs.getFile(storeContainerClient, requestBlobFilename);
//			fileContents.put(requestBlobFilename, fileBytes);
//
//			fileBytes = abs.getFile(storeContainerClient, responseBlobFilename);
//			fileContents.put(responseBlobFilename, fileBytes);
//
//			blobFilename = blobFilename + ".zip";
//			zipFiles.put(blobFilename, fileContents);
//
//			byte[] zipBytes = documentRepositoryUtil.zipFiles(zipFiles);
//
//			blob = archiveContainerClient.getBlobClient(blobFilename);
//			blob.upload(new ByteArrayInputStream(zipBytes), true);
//
//		} catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
		
		Map<String, List<String>> fileArchiveApiPayload = new HashMap<String, List<String>>();
		
		fileList.add(requestBlobFilename);
		fileList.add(responseBlobFilename);
		
		fileArchiveApiPayload.put(blobFilename+".zip", fileList);
		
		httpRequestHelper.postRequest(fileArchiveApiUrl, fileArchiveApiPayload);
		
		return jaxbEl;
	}

	/**
	 * @param request
	 * @return
	 */
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "RetrieveDocumentSetRequest")
	@ResponsePayload
	public JAXBElement<RetrieveDocumentSetResponseType> retrieveDocumentSetRequest(
			@RequestPayload JAXBElement<RetrieveDocumentSetRequestType> request) {

		String fullReqStr = toXml(request);
		// System.out.println("Full Request String= " + fullReqStr);
		RetrieveDocumentSetResponseType response = new RetrieveDocumentSetResponseType();
		QName qname = new QName(NAMESPACE_URI, "Success", "env");

		Temporal temporal = Instant.now();
		// String dateTime = FILENAME_DATETIME_FORMAT.format(temporal);
		String reqId = REQUEST_ID_FORMAT.format(temporal);
		String requestId = reqId + getAlphaNumericString(8);
		String documentUniqueId = StringUtil.EMPTY_STRING;
		List<String> fileList = new ArrayList<String>();
		System.out.println("RetrieveDocumentSetRequest received. RequestID=" + requestId);

		Set<String> documentIds = new HashSet<String>();

		RetrieveDocumentSetRequestType rdsrt = request.getValue();
		// System.out.println("Request=" +
		// rdsrt.getDocumentRequest().get(0).getDocumentUniqueId());
		if (null != rdsrt.getDocumentRequest() && rdsrt.getDocumentRequest().size() > 0) {
			rdsrt.getDocumentRequest().stream().forEach(docReq -> {
				if (docReq != null && StringUtils.hasText(docReq.getDocumentUniqueId())
						&& !docReq.getDocumentUniqueId().trim().equals("?")) {
					documentIds.add(docReq.getDocumentUniqueId().trim());
				}
			});
		}

		System.out.println("Requested document IDs=" + documentIds.toString());

		if (documentIds.size() == 0) {
			documentIds.addAll(documentRepository.getDocumentNameList());
		} else {
			// Use only the first item name as the azure blob xml filename
			documentUniqueId = documentIds.iterator().next();
		}

		documentIds.stream().forEach(documentId -> {
			String documentString = documentRepository.findDocument(null, null, documentId);
			if (StringUtils.hasText(documentString)) {
				response.getDocumentResponse().add(addToDocumentSetArray(documentId, documentString));
				
				String dataDocumentFileName = new StringBuilder(RETRIEVE_DOCUMENT_FILENAME_PREFIX)
						.append(documentId.replace(":", "_")).append("_").append(requestId).toString()+"_res_document.xml";
				BlobClient blob = storeContainerClient.getBlobClient(dataDocumentFileName);
				blob.upload(new ByteArrayInputStream(decodeBase64StringToByteArray(documentString)), true);
				System.out.println("Successfully uploaded response data document file " + dataDocumentFileName + " to Azure blob");
				fileList.add(dataDocumentFileName);
				
			} else {
				throw new RuntimeException(DocumentRepositoryMessages.DOCUMENT_NOT_FOUND + documentId);
			}
		});

		RegistryResponseType registryResponse = new RegistryResponseType();
		registryResponse.setStatus("SUCCESS");
		registryResponse.setRequestId(requestId);
		response.setRegistryResponse(registryResponse);
		JAXBElement<RetrieveDocumentSetResponseType> jaxbEl = new JAXBElement<RetrieveDocumentSetResponseType>(qname,
				RetrieveDocumentSetResponseType.class, response);
		
		String blobFilename = new StringBuilder(RETRIEVE_DOCUMENT_FILENAME_PREFIX)
				.append(documentUniqueId.replace(":", "_")).append("_").append(requestId).toString();

		String requestBlobFilename = blobFilename + "_request.xml";
		BlobClient blob = storeContainerClient.getBlobClient(requestBlobFilename);
		blob.upload(new ByteArrayInputStream(fullReqStr.getBytes(StandardCharsets.UTF_8)), true);
		System.out.println("Successfully uploaded RetrieveDocumentSetRequest request xml file " + requestBlobFilename
				+ " to Azure blob");

		String responseBlobFilename = blobFilename + "_response.xml";
		blob = storeContainerClient.getBlobClient(responseBlobFilename);
		blob.upload(new ByteArrayInputStream(toXml(jaxbEl).getBytes(StandardCharsets.UTF_8)), true);
		System.out.println("Successfully uploaded RetrieveDocumentSetRequest response xml file " + responseBlobFilename
				+ " to Azure blob");
		System.out.println("RetrieveDocumentSetRequest Completed. RequestID=" + requestId);
		
		Map<String, List<String>> fileArchiveApiPayload = new HashMap<String, List<String>>();
		fileList.add(requestBlobFilename);
		fileList.add(responseBlobFilename);
		fileArchiveApiPayload.put(blobFilename+".zip", fileList);
		
		httpRequestHelper.postRequest(fileArchiveApiUrl, fileArchiveApiPayload);

		return jaxbEl;
	}

	/**
	 * @param documentId
	 * @param documentString
	 * @return
	 */
	private DocumentResponse addToDocumentSetArray(String documentId, String documentString) {
		DocumentResponse dr = new DocumentResponse();
		byte[] bytes = decodeBase64StringToByteArray(documentString);
		DRByteArrayDataSource barrds = new DRByteArrayDataSource(bytes, "application/octet-stream");
		DataHandler result = new DataHandler(barrds);
		dr.setDocument(result);
		dr.setRepositoryUniqueId("localInMemoryRepository");
		dr.setHomeCommunityId("local");
		dr.setDocumentUniqueId(documentId);
		dr.setMimeType("text/xml");
		return dr;
	}

	/**
	 * @param normalString
	 * @return
	 */
	private byte[] encodeStringToBase64ByteArray(String normalString) {
		Assert.notNull(normalString, "String value must not be null");
		return Base64.getEncoder().encode(normalString.getBytes());
	}

	/**
	 * @param base64EncStr
	 * @return
	 */
	private byte[] decodeBase64StringToByteArray(String base64EncStr) {
		Assert.notNull(base64EncStr, "String value must not be null");
		return Base64.getDecoder().decode(base64EncStr);
	}

	/**
	 * @param element
	 * @return
	 */
	private String toXml(JAXBElement element) {
		try {
			JAXBContext jc = JAXBContext.newInstance(element.getValue().getClass());
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			marshaller.marshal(element, baos);
			return baos.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	static String getAlphaNumericString(int n) {
		String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "0123456789" + "abcdefghijklmnopqrstuvxyz";
		StringBuilder sb = new StringBuilder(n);
		for (int i = 0; i < n; i++) {
			int index = (int) (AlphaNumericString.length() * Math.random());
			sb.append(AlphaNumericString.charAt(index));
		}

		return sb.toString();
	}
}
