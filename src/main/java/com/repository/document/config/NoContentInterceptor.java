package com.repository.document.config;

import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.interceptor.EndpointInterceptorAdapter;
import org.springframework.ws.soap.SoapBody;
import org.springframework.ws.soap.SoapEnvelope;
import org.springframework.ws.soap.SoapMessage;

/**
 * If a web service has no response, this handler returns: 204 No Content
 */
@Component
public class NoContentInterceptor extends EndpointInterceptorAdapter {

	@Override
	public void afterCompletion(MessageContext messageContext, Object o, Exception e) throws Exception {
//		SoapBody soapBody = getSoapBody(messageContext);
//		SoapFault soapFault = soapBody.getFault();
//		String fault = soapFault.getFaultStringOrReason();
//		int statusCode = 200;
//		if (StringUtils.hasText(fault)) {
//			System.out.println("Soap request processing fault=" + fault);
//			if (fault.contains(DocumentRepositoryMessages.DOCUMENT_NOT_FOUND)
//					|| fault.contains(DocumentRepositoryMessages.DOCUMENT_NOT_PROVIDED)
//					|| fault.contains(DocumentRepositoryMessages.DOCUMENT_UNIQUE_ID_NOT_PROVIDED)) {
//				statusCode = 400;
//			}
//		}
//
//		TransportContext tc = TransportContextHolder.getTransportContext();
//		if (tc != null && tc.getConnection() instanceof HttpServletConnection) {
//			HttpServletConnection connection = ((HttpServletConnection) tc.getConnection());
//			// First we force the 'statusCodeSet' boolean to true:
//			//connection.setFault(true);
//			connection.setFaultCode(SOAPConstants.SOAP_SENDER_FAULT);
//			// Next we can set our custom status code:
//			connection.getHttpServletResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
//		}
	}

	@Override
	public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
		// SoapBody soapBody = getSoapBody(messageContext);

//		SoapMessage message = (SoapMessage) messageContext.getRequest();
//	    SoapBody soapBody = message.getSoapBody();
//	    Source bodySource = soapBody.getSource();
//	    DOMSource bodyDomSource = (DOMSource) bodySource;
//
//	    JAXBContext context = JAXBContext.newInstance(ProvideAndRegisterDocumentSetRequestType.class);
//	    Unmarshaller unmarshaller = context.createUnmarshaller();
//	    
//	    Object unmarshalledObject = unmarshaller.unmarshal(bodyDomSource);
//
//	    ProvideAndRegisterDocumentSetRequestType request = (ProvideAndRegisterDocumentSetRequestType) unmarshalledObject;
		return true;
	}
	
	private SoapBody getSoapBody(MessageContext messageContext) {
		SoapMessage soapMessage = (SoapMessage) messageContext.getResponse();
		SoapEnvelope soapEnvelope = soapMessage.getEnvelope();
		return soapEnvelope.getBody();
	}
}
