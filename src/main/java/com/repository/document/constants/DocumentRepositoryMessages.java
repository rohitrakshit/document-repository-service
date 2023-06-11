package com.repository.document.constants;

public class DocumentRepositoryMessages {
	public static final String DOCUMENT_UNIQUE_ID_NOT_PROVIDED = "Document unique id not provided. Please check your request payload for the 'id' attribute inside <Document> tag.";
	public static final String DOCUMENT_NOT_PROVIDED = "No document object provided. Please check your request payload and make sure to have atleast one <Document> tag under <ProvideAndRegisterDocumentSetRequest> tag.";
	public static final String DOCUMENT_NOT_FOUND = "Document not found. Requested document ID=";
}
