package com.repository.document.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.repository.document.service.DocumentRepositoryEndpoint;

import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import jakarta.xml.bind.JAXBElement;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;

import java.util.Optional;

import javax.xml.namespace.QName;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.function.adapter.azure.FunctionInvoker;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @author Rohit Rakshit
 */
public class EchoHandler extends FunctionInvoker<Message<String>, String> {
	
	@Autowired
	DocumentRepositoryEndpoint dre = new DocumentRepositoryEndpoint();

	@FunctionName("echo")
	public String execute(@HttpTrigger(name = "req", methods = {HttpMethod.GET,
			HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
		ExecutionContext context) {
		Message<String> message = MessageBuilder.withPayload(request.getBody().get()).copyHeaders(request.getHeaders()).build();
		
		ProvideAndRegisterDocumentSetRequestType p = new ProvideAndRegisterDocumentSetRequestType();
		p.setSubmitObjectsRequest(null);
		QName qname = new QName("provideAndRegisterDocumentSetRequest"); 
		JAXBElement<ProvideAndRegisterDocumentSetRequestType> jaxbEl = new JAXBElement<ProvideAndRegisterDocumentSetRequestType>(qname, ProvideAndRegisterDocumentSetRequestType.class, p);
		
		JAXBElement<RegistryResponseType> jaxbRes = dre.provideAndRegisterDocumentSetRequestRequest(jaxbEl);
		RegistryResponseType rrt = jaxbRes.getValue();
		System.out.println(rrt.getStatus());
		message = message = MessageBuilder.withPayload(rrt.getStatus()).copyHeaders(request.getHeaders()).build();
		return handleRequest(message, context);
	}

}
