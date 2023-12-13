package gov.cdc.dhcs.hehr.dcp.direct;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.microsoft.azure.functions.ExecutionContext;

import gov.cdc.dhcs.hehr.dcp.direct.function.AzureBlobService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.function.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

/**
 * @author Sai Valluripalli
 */
@SpringBootApplication
public class DocumentRepositoryApplicationMain extends SpringBootServletInitializer {
	
	private static final Logger logger = LoggerFactory.getLogger(DocumentRepositoryApplicationMain.class);

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(DocumentRepositoryApplicationMain.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(DocumentRepositoryApplicationMain.class, args);
	}

	@Autowired
	private AzureBlobService azureBlobService;

	@Bean
	public Function<Message<Map<String, List<String>>>, String> archiveFiles() {
		return message -> {
			Map<String, List<String>> fileArchivePayload = message.getPayload();
			String response = azureBlobService.downloadFilesAndZipUpload(fileArchivePayload);
			logger.info("Azure blob file zip operation successful");
			return response;
		};
	}

	@Bean
	public Function<String, String> echo() {
		return payload -> payload;
	}
}
