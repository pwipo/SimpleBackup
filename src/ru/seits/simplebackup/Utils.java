package ru.seits.simplebackup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

class Utils {

	static private final String strSplitterValue = ";;";

	static private int intCountByte = 1000;
	static private String strAlgoritm = "MD5";
	
	static byte[] createChecksum(File file) throws Exception {
		byte[] result = null;

		try(InputStream fis =  new FileInputStream(file)){
			byte[] buffer = new byte[intCountByte];
			MessageDigest complete = MessageDigest.getInstance(strAlgoritm);
			int numRead;
			do {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					complete.update(buffer, 0, numRead);
				}
			} while (numRead != -1);
			result = complete.digest();
		}
		
		return result;
	}

	// see this How-to for a faster way to convert
	// a byte array to a HEX string
	static String buildHexStringFromByteArray(byte b[]) {
		String result = "";
		if(b==null)
			return result;

		for (int i=0; i < b.length; i++){ 
			String str = Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
			//System.out.println(str.length() + " " + str);
			result += str;
		}

		return result;
	}
	
	static byte[] parseHexStringToByteArray(String strDM5) {
		byte result[] = null;
		if(strDM5==null)
			return result;
		
		byte b[] = new byte[strDM5.length()/2];
		for(int i=0; i<b.length; i++){
			try{
				b[i] = (byte)Integer.parseInt(strDM5.substring(i*2, i*2+2), 16);
			}catch(Exception e){}
		}
		
		result = b;

		return result;
	}

	static void copyFolder(File src, File dest) throws IOException {
		// checks
		if(src==null || dest==null)
			return;
		if(!src.isDirectory())
			return;
		if(dest.exists()){
			if(!dest.isDirectory()){
				//System.out.println("destination not a folder " + dest);
				return;
			}
		} else {
            dest.mkdir();
		}
		
        if(src.listFiles()==null || src.listFiles().length==0)
        	return;

		String strAbsPathSrc = src.getAbsolutePath();
		String strAbsPathDest = dest.getAbsolutePath();

	    //try {
			Files.walkFileTree(src.toPath(), new SimpleFileVisitor<Path>() {
			    @Override
			    public FileVisitResult visitFile(Path file,
			    		BasicFileAttributes attrs) throws IOException {
			    	File dstFile = new File(strAbsPathDest + file.toAbsolutePath().toString().substring(strAbsPathSrc.length()));
			    	if(dstFile.exists())
			    		return FileVisitResult.CONTINUE;
			    	
			    	if(!dstFile.getParentFile().exists())
			    		dstFile.getParentFile().mkdirs();
			    	
					//System.out.println(file + " " + dstFile.getAbsolutePath());
			    	Files.copy(file, dstFile.toPath());
			    	
			        return FileVisitResult.CONTINUE;
			    }
			});
			/*
		} catch (IOException e) {
			//e.printStackTrace();
			return;
		}
		*/
	    
		return;
	}
	
	static Map<String, FileAttr> getFileAttrsFromSettings(File fileSettings) throws FileNotFoundException, IOException {
		Map<String, FileAttr> result = null;
		//get fileattr from settings
		Map<String, FileAttr> mapSettings = new HashMap<String, FileAttr>();
		try(BufferedReader brSettings = new BufferedReader(new FileReader(fileSettings))){
			String strLine = null;
			while((strLine=brSettings.readLine())!=null){
				FileAttr fa = importFileAttr(strLine);
				if(fa==null)
					continue;
				mapSettings.put(fa.getPath().substring(fa.getPath().indexOf(File.separator)+1), fa);
			}
		}
		
		if(!mapSettings.isEmpty())
			result = mapSettings;
		
		return result;
	}

	static private FileAttr importFileAttr(String str){
		FileAttr result = null;
		if(str==null)
			return result;

		StringTokenizer st = new StringTokenizer(str, strSplitterValue);
		//System.out.println(str);
		if(st.countTokens()<4)
			return result;
		FileAttr fa = new FileAttr();
		fa.setPath(st.nextToken());
		try{
			fa.setSize(Long.parseLong(st.nextToken()));
			fa.setDateChang(new Date(Long.parseLong(st.nextToken())));
		}catch(Exception e){
			//e.printStackTrace();
			return result;
		}
		fa.setHash(st.nextToken());
		result = fa;
		
		return result;
	}

	static String exportFileAttr(FileAttr fa){
		String result = null;
		if(fa==null)
			return result;
		result = fa.getPath() + strSplitterValue + fa.getSize() + strSplitterValue + fa.getDateChang().getTime() + strSplitterValue + fa.getHash();
		return result;
	}
	
	
	   
}
