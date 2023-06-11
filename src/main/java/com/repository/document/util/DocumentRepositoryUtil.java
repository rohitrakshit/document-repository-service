package com.repository.document.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Component;

@Component
public class DocumentRepositoryUtil {

	public byte[] zipFiles(Map<String, Map<String, byte[]>> zipFiles) {
		byte[] responseBytes = null;
		try {
			for (Entry<String, Map<String, byte[]>> zipFileEntry : zipFiles.entrySet()) {
				Map<String, byte[]> fileContents = zipFileEntry.getValue();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ZipOutputStream zipOut = new ZipOutputStream(baos);
				for (Entry<String, byte[]> fileEntry : fileContents.entrySet()) {

					ByteArrayInputStream bais = new ByteArrayInputStream(fileEntry.getValue());
					System.out.println(
							"fileName " + fileEntry.getKey() + " testString.getBytes() " + fileEntry.getValue());

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

				// just a test to see if can be opened in the operating system
//				OutputStream os = new FileOutputStream(new File("/home/osboxes/zipTestDir/"+zipFileEntry.getKey()+".zip"));
//				os.write(zipBytes);
//				os.close();
				responseBytes = zipBytes;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return responseBytes;
	}
}
