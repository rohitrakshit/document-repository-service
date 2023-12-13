package gov.cdc.dhcs.hehr.dcp.direct.config;

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

	/**
	 * 
	 * @param messageContext
	 * @param o
	 * @param e
	 * @throws Exception
	 */
	@Override
	public void afterCompletion(MessageContext messageContext, Object o, Exception e) throws Exception {
	}

	/**
	 * 
	 * @param messageContext
	 * @param endpoint
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean handleResponse(MessageContext messageContext, Object endpoint) throws Exception {
		return true;
	}

	/**
	 * 
	 * @param messageContext
	 * @return SoapBody
	 */
	@SuppressWarnings("unused")
	private SoapBody getSoapBody(MessageContext messageContext) {
		SoapMessage soapMessage = (SoapMessage) messageContext.getResponse();
		SoapEnvelope soapEnvelope = soapMessage.getEnvelope();
		return soapEnvelope.getBody();
	}
}
