package gov.cdc.dhcs.hehr.dcp.direct.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * 
 * @author Sai Valluripalli
 *
 */
@Component
public class DocumentRepositoryUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(DocumentRepositoryUtil.class);
	
	/**
	 * @param base64EncStr
	 * @return
	 */
	public static byte[] decodeBase64StringToByteArray(String base64EncStr) {
		Assert.notNull(base64EncStr, "String value must not be null");
		return Base64.getDecoder().decode(base64EncStr);
	}

	public byte[] zipFiles(Map<String, Map<String, byte[]>> zipFiles) {
		byte[] responseBytes = null;
		try {
			for (Entry<String, Map<String, byte[]>> zipFileEntry : zipFiles.entrySet()) {
				Map<String, byte[]> fileContents = zipFileEntry.getValue();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zipOut = new ZipOutputStream(baos);
				for (Entry<String, byte[]> fileEntry : fileContents.entrySet()) {

					ByteArrayInputStream bais = new ByteArrayInputStream(fileEntry.getValue());
					logger.debug(
							"fileName " + fileEntry.getKey() + " fileEntry.getValue() " + fileEntry.getValue());

					ZipEntry zipEntry = new ZipEntry(fileEntry.getKey());
					zipOut.putNextEntry(zipEntry);

					byte[] bytes = new byte[1024];
					int length;
					while ((length = bais.read(bytes)) >= 0) {
						zipOut.write(bytes, 0, length);
					}
					bais.close();
				}
				zipOut.close();
				baos.close();
				byte[] zipBytes = baos.toByteArray();
				responseBytes = zipBytes;
			}
		} catch (IOException e) {
			logger.error("IOException while creating .zip file", e);
		}
		return responseBytes;
	}
}
