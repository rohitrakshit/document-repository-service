package com.repository.document.config;


import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import jakarta.activation.*;

public class SystemDataHandlerConfigurator {

    public void setupCustomDataContentHandlers() {

        DataHandler.setDataContentHandlerFactory(new CustomDCHFactory());
    }

    private class CustomDCHFactory implements DataContentHandlerFactory {

        @Override
        public DataContentHandler createDataContentHandler(String mimeType) {

            return new BinaryDataHandler();
        }
    }

    private class BinaryDataHandler implements DataContentHandler {

        /** Creates a new instance of BinaryDataHandler */
        public BinaryDataHandler() {

        }

        /** This is the key, it just returns the data uninterpreted. */
        public Object getContent(DataSource dataSource) throws java.io.IOException {

                return dataSource.getInputStream();
        }

        @Override
		public Object getTransferData(ActivationDataFlavor df, DataSource ds) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

        public ActivationDataFlavor[] getTransferDataFlavors() {

            return new ActivationDataFlavor[0];
        }

        public void writeTo(Object obj, String mimeType, java.io.OutputStream outputStream) 
         throws java.io.IOException {

                if (mimeType == "text/plain") {
                    byte[] stringByte = (byte[]) ((String) obj).getBytes("UTF-8");
                    outputStream.write(stringByte);
                }
                else {
                    throw new IOException("Unsupported Data Type: " + mimeType);
                }
        }
    }  
}