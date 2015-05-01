package ru.seits.simplebackup;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Backup {

	static private final int intCountArgs = 4;
	
	//static private final int intArgsIdType = 0;
	static private final int intArgsIdSource = 0;
	static private final int intArgsIdDestination = 1;
	static private final int intArgsIdSettings = 2;
	//static private final int intArgsIdPrefixName = 3;
	static private final int intArgsIdCheckDest = 3;
	
	//static private final int intTypeDisk = 1;
	//static private final int intTypeFlush = 2;
	
	static private final String strPrefixName = "arch";
	//static private final boolean booleanCheckDestination = true;

	public static void main(String[] args) throws Exception {
		//String[] args2 = {"d:\\1", "d:\\2", "d:\\1.txt", "tmp"};
		String[] args2 = args;
		
		PrintWriter pw = new PrintWriter(System.out, true);
		
		// checks and init
		if(args2.length < intCountArgs){
			pw.println("count args less then " + intCountArgs);
			return;
		}
		/*
		Integer intType = null;
		try{
			intType = Integer.parseInt(args2[intArgsIdType]);
		}catch(Exception e){
			pw.println("type not set");
			e.printStackTrace();
			return;
		}
		*/
		
		// init main vars
		String strSource = args2[intArgsIdSource];
		String strDestination = args2[intArgsIdDestination];
		String strSettings = args2[intArgsIdSettings];
		String strPrefixTmpFolderName = strPrefixName;//args2[intArgsIdPrefixName];
		boolean bCheckDest = Boolean.parseBoolean(args2[intArgsIdCheckDest]);
		
		// checks
		File fileSource = new File(strSource);
		if(!fileSource.exists() || !fileSource.isDirectory()){
			pw.println("source folder not exist " + strSource);
			return;
		}
		File fileDestination = new File(strDestination);
		if(!fileDestination.exists() || !fileSource.isDirectory()){
			pw.println("destination folder not exist " + strDestination);
			return;
		}
		
		/*
		File fileDestinationMainFolder = null; 
		for(File fileSubDest: fileDestination.listFiles()){
			if(fileSubDest.getName().equals(strMainFolderName)){
				fileDestinationMainFolder = fileSubDest;
				break;
			}
		}
		*/

		Collection<FileAttr> newFACollection = null;
		File fileSettings = new File(strSettings);
		if(!fileSettings.exists() || !(new File(strDestination, fileSource.getName())).exists()){
			pw.println("create new archive " + strDestination + " for source " + strSource);
			newFACollection = createNew(fileSource, new File(fileDestination, fileSource.getName()));
			if(fileSettings.exists())
				fileSettings.delete();
		}else{
			pw.println("update exist archive=" + strDestination + " for source=" + strSource + " setting=" + strSettings + " checkDest=" + bCheckDest);
			newFACollection = updateExist(fileSource, fileDestination, fileSettings, strPrefixTmpFolderName, bCheckDest);
		}
		// write fileattr to file settings
		//writeToFileFileAtrrs(fileSettings, mapNewSettings);
		if(newFACollection==null || newFACollection.isEmpty())
			return;
		
		// write fileattr to file settings
		try(PrintWriter pwSettings = new PrintWriter(new FileWriter(fileSettings, true))){
			//print current date
			pwSettings.println(new Date());
			//print data
			for(FileAttr fa: newFACollection)
				pwSettings.println(Utils.exportFileAttr(fa));
		}

		newFACollection.stream().forEach(t->pw.println(Utils.exportFileAttr(t)));
	}
	
	static private Collection<FileAttr> createNew(File fileSource, File fileDestination) throws IOException {
		Collection<FileAttr> result = null;
		
		// get fileattr from source
		Map<String, FileAttr> mapSettings = getFileAttrsFromFolder(fileSource, true);
		if(mapSettings==null || mapSettings.isEmpty())
			return result;

		// copy folder
		Utils.copyFolder(fileSource, fileDestination);
		
		result = mapSettings.values();
		return result;
	}
	
	static private Collection<FileAttr> updateExist(File fileSource, File fileDestination, File fileSettings, String strPrefixFolderName, boolean bCheckDest) throws Exception{
		Collection<FileAttr> result = null;

		// get fileattr from setting file
		Map<String, FileAttr> mapSettings = Utils.getFileAttrsFromSettings(fileSettings);
		if(mapSettings==null)
			mapSettings = new HashMap<String, FileAttr>();

		//System.out.println("values of mapSettings " + mapSettings.size());
		//mapSettings.values().stream().forEach(t->System.out.println(Utils.exportFileAttr(t)));
		
		// get fileattr from source
		Map<String, FileAttr> mapSourceSettings = getFileAttrsFromFolder(fileSource, false);
		if(mapSourceSettings==null || mapSourceSettings.isEmpty())
			return result;
		
		//System.out.println("values of mapSourceSettings " + mapSettings.size());
		//mapSourceSettings.values().stream().forEach(t->System.out.println(Utils.exportFileAttr(t)));

		//create map of new and changes files
		Map<String, FileAttr> mapNewSettingsNewFiles = new HashMap<String, FileAttr>();
		for(Map.Entry<String, FileAttr> entry: mapSourceSettings.entrySet()){
			FileAttr fa = mapSettings.get(entry.getKey());
			if(fa==null || fa.getDateChang() != entry.getValue().getDateChang() || fa.getSize()!=entry.getValue().getSize()){
				mapNewSettingsNewFiles.put(entry.getKey(), entry.getValue());
				//System.out.println(Utils.exportFileAttr(entry.getValue()) + " --- " + Utils.exportFileAttr(fa));
				continue;
			}
		}
		// add to mapNewSettingsNewFiles files, if they not exist in destination
		if(bCheckDest){
			for(Map.Entry<String, FileAttr> entry: mapSettings.entrySet()){
				if(mapNewSettingsNewFiles.containsKey(entry.getKey()))
					continue;
				
				File fileTmpDest = new File(fileDestination.getAbsolutePath() + File.separator + entry.getValue().getPath());
				if(fileTmpDest.exists())
					continue;

				FileAttr fa = getFileAttrFromFile(fileSource, entry.getKey(), true);
				if(fa==null)
					continue;

				mapNewSettingsNewFiles.put(entry.getKey(), fa);
			}
		}
		//System.out.println("values of mapNewSettingsNewFiles " + mapNewSettingsNewFiles.size());
		//mapNewSettingsNewFiles.values().stream().forEach(t->System.out.println(Utils.exportFileAttr(t)));
		
		//create map of not exist files
		List<String> listNewSettingsRemoveFiles = new ArrayList<String>();
		for(Map.Entry<String, FileAttr> entry: mapSettings.entrySet()){
			if(!mapSourceSettings.containsKey(entry.getKey())){
				listNewSettingsRemoveFiles.add(entry.getKey());
				continue;
			}
		}
		
		if(mapNewSettingsNewFiles.isEmpty() && listNewSettingsRemoveFiles.isEmpty())
			return result;
		
		// copy new files to archive
		List<FileAttr> lstTmp = new ArrayList<FileAttr>();
		Path pathNewFolder = Files.createTempDirectory(fileDestination.toPath(), strPrefixFolderName);
		if(pathNewFolder==null)
			return result;
			
		for(Map.Entry<String, FileAttr> entry: mapNewSettingsNewFiles.entrySet()){
			// get fa
			FileAttr fa = getFileAttrFromFile(fileSource, entry.getKey(), true);
			if(fa==null)
				continue;
			fa.setPath(pathNewFolder.getFileName() + File.separator + entry.getKey());
			
			// if older file
			FileAttr faTmp = mapSettings.get(entry.getKey());
			if(faTmp!=null && faTmp.getHash().equals(fa.getHash())){
				//mapNewSettings.put(entry.getKey(), faTmp);
				continue;
			}
				
			File fileTmpNewSrc = new File(fileSource.getAbsolutePath() + File.separator + entry.getKey());
			File fileTmpNewDest = new File(pathNewFolder.toAbsolutePath().toString() + File.separator + entry.getKey());
			if(!fileTmpNewSrc.exists())
				continue;
			
			if(!fileTmpNewDest.getParentFile().exists())
				fileTmpNewDest.getParentFile().mkdirs();
			
			try {
				Files.copy(fileTmpNewSrc.toPath(), fileTmpNewDest.toPath());
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			//System.out.println(printFileAttr(fa));
			lstTmp.add(fa);
		}
		
		// add removed fa to result list
		for(String tmpStr: listNewSettingsRemoveFiles)
			lstTmp.add(new FileAttr(tmpStr, 0, -1, ""));
		
		if(!lstTmp.isEmpty())
			result = lstTmp;
		return result;
	}
	
	static private Map<String, FileAttr> getFileAttrsFromFolder(File folder, boolean getHash) throws IOException {
		Map<String, FileAttr> result = null;
		
		if(folder==null || !folder.exists() || !folder.isDirectory())
			return result;
		
		Map<String, FileAttr> map = new HashMap<String, FileAttr>();

		int intTmp = folder.getAbsolutePath().length() + 1;
		Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {
		    @Override
		    public FileVisitResult visitFile(Path file,
		    		BasicFileAttributes attrs) throws IOException {
		    	String path = file.toAbsolutePath().toString().substring(intTmp);
    			//b = Utils.createChecksum(new File(file.toUri()));
		    	if(getHash){
			    	FileAttr fa = null;
		    		try {
		    			fa = getFileAttrFromFile(folder, path, getHash);
		    		} catch (Exception e) {}
			    	if(fa!=null)
						map.put(path, fa);
		    	}else{
			    	map.put(path, new FileAttr(folder.getName() + File.separator + path, attrs.lastModifiedTime().toMillis(), attrs.size(), ""));
		    	}
		    	return FileVisitResult.CONTINUE;
		    }
		});
	    
	    if(!map.isEmpty())
	    	result = map;

	    return result;
	}
	
	static private FileAttr getFileAttrFromFile(File fileSource, String path, boolean getHash) throws Exception{
		FileAttr result = null;
		
		File fileTmp = new File(fileSource.getAbsolutePath() + File.separator + path);
		if(!fileTmp.exists())
			return result;
		
		String hash = null;
		if(getHash){
			hash = Utils.buildHexStringFromByteArray(Utils.createChecksum(fileTmp));
			if(hash==null)
				return result;
		}

		result = new FileAttr(fileSource.getName() + File.separator + path, fileTmp.lastModified(), fileTmp.length(), hash);
		//System.out.println(Utils.exportFileAttr(result));
		
		return result;
	}
	
	
}
