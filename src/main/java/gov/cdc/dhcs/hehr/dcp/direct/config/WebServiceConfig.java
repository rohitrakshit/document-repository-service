package gov.cdc.dhcs.hehr.dcp.direct.config;

import java.util.List;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapVersion;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.soap.server.endpoint.interceptor.PayloadRootSmartSoapEndpointInterceptor;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

/**
 * 
 * @author Sai Valluripalli
 *
 */
@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {
	
	@Bean
	public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
		MessageDispatcherServlet servlet = new MessageDispatcherServlet();
		servlet.setApplicationContext(applicationContext);
		servlet.setTransformWsdlLocations(true);
		return new ServletRegistrationBean<>(servlet, "/services/*", "/DocumentRepository.wsdl");
	}

	@Bean(name = "DocumentRepository")
	public Wsdl11Definition defaultWsdl11Definition() {
//		DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
//		wsdl11Definition.setCreateSoap12Binding(true);
//		wsdl11Definition.setPortTypeName("port");
//		wsdl11Definition.setLocationUri("/services");
//		wsdl11Definition.setTargetNamespace("urn:ihe:iti:xds-b:2007");
//		wsdl11Definition.setSchema(documentRepositorySchema);
		return new SimpleWsdl11Definition(new ClassPathResource("static/HISP_DocumentRepository_Service.wsdl"));
	}

	@Bean
	public SaajSoapMessageFactory messageFactory() {
	    SaajSoapMessageFactory messageFactory = new SaajSoapMessageFactory();
	    messageFactory.setSoapVersion(SoapVersion.SOAP_12);
	    return messageFactory;
	}

	
//	@Bean
//	public XsdSchema documentRepositorySchema() {
//		return new SimpleXsdSchema(new ClassPathResource("static/services/XDS.b_DocumentRepositoryMTOM.xsd"));
//	}
	
	@Bean
	public TaskExecutor taskExecutor() {
	    return new SimpleAsyncTaskExecutor(); 
	}
	
	/**
	 * Add our own interceptor for the specified WS endpoint.
	 * @param interceptors
	 */
	@Override
	public void addInterceptors(List<EndpointInterceptor> interceptors) {
	    interceptors.add(new PayloadRootSmartSoapEndpointInterceptor(
	            new NoContentInterceptor(),
	            "NAMESPACE",
	            "LOCAL_PART"
	    ));
	 }
}
