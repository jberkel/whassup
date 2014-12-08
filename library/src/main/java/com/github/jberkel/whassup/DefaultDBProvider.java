package com.github.jberkel.whassup;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import com.stericson.RootTools.RootTools;
import static com.github.jberkel.whassup.Whassup.TAG;

public class DefaultDBProvider implements DBProvider {
    private static final String CURRENT_DB_FAKE   = "msgstore.db";
    private static final String CURRENT_DB   = "msgstore.db.crypt";
    private static final String CURRENT_DB_5 = "msgstore.db.crypt5";

    private static final File DB_PATH = new File(Environment.getExternalStorageDirectory(), 
        "Whatsapp/Databases");

    @Override
    public File getDBFile() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
        	//check if device is rooted. move the unencrypted db
        	if (RootTools.isRootAvailable()) {
        		RootTools.debugMode = false; 
        		if (RootTools.isAccessGiven()) {
        		    // your app has been granted root access
        			Log.d(TAG, "root access given");
        			String waPackage = "com.whatsapp";
            		String dataDBPath = "/data/data/"+waPackage+"/databases/";
            		// Primary physical SD-CARD (not emulated)
            	    String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
            	    Log.w(TAG, "EXTERNAL_STORAGE: "+rawExternalStorage);
            	    // All Secondary SD-CARDs (all exclude primary) separated by ":"
//            	    String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
            	    // Primary emulated SD-CARD
//            	    String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
            		
            	    String source = dataDBPath+CURRENT_DB_FAKE;
            		String dest = rawExternalStorage+"/WhatsApp/Databases/"+CURRENT_DB_FAKE;

            		File destFile = new File(dest);
            		if(RootTools.exists(source))
            		{
            			//TODO copy only if newer
//                		File sourceFile = new File(source);
// 		     			if(!RootTools.exists(dest) || (RootTools.exists(dest) && ( sourceFile.lastModified() > destFile.lastModified() ))  )
//    					{
		            		if(RootTools.copyFile(source, dest, true, false))
		            		return destFile;
//	    				}
            		}
        		}else{
        			Log.d(TAG, "root access NOT given");
        		}
        		
        	}
            for (String name : new String[] { CURRENT_DB_FAKE, CURRENT_DB_5, CURRENT_DB }) {
                File db = new File(DB_PATH, name);
                if (db.exists() && db.canRead()) {
                    return db;
                }
            }
            Log.d(TAG, "could not find db");
            return null;
        } else {
            Log.w(TAG, "external storage not mounted");
            return null;
        }
    }
}
