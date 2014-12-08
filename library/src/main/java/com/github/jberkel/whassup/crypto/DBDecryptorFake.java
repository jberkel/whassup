package com.github.jberkel.whassup.crypto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.GeneralSecurityException;

public class DBDecryptorFake implements Decryptor {

	@Override
	public void decryptDB(File input, File output) throws IOException,
			GeneralSecurityException {
		copyFile(input,output);
	}
	
	public void copyFile(File sourceFile, File destFile) throws IOException {
		
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	    	destination = new FileOutputStream(destFile).getChannel();	        
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }

	}

}
