package gov.cdc.dhcs.hehr.dcp.direct.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import jakarta.activation.DataSource;

/**
 * 
 * @author Sai Valluripalli
 *
 */
public class DRByteArrayDataSource implements DataSource {

	private final byte[] data;

	private final String contentType;

	private int offset;

	private int length;

	public DRByteArrayDataSource(byte[] data, String contentType) {
		this.contentType = contentType;
		this.data = data;
//		this.offset = offset;
		this.length = data.length;
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(this.data, this.offset, this.length);
	}

	@Override
	public OutputStream getOutputStream() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public String getName() {
		return "ByteArrayDataSource";
	}
}