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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Backup {

	static private final int intCountArgs = 3;
	
	//static private final int intArgsIdType = 0;
	static private final int intArgsIdSource = 0;
	static private final int intArgsIdDestination = 1;
	static private final int intArgsIdSettings = 2;
	static private final int intArgsIdFolder = 3;
	
	//static private final int intTypeDisk = 1;
	//static private final int intTypeFlush = 2;
	
	static private final String strPrefixFolder = "arch";

	public static void main(String[] args) throws Exception {
		//String[] args2 = {/*Integer.toString(intTypeDisk), */"d:\\1", "d:\\2", "d:\\1.txt", "tmp"};
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
		
		String strFolder = strPrefixFolder;
		if(args2.length > intCountArgs)
			strFolder = args2[intArgsIdFolder];
		
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
			newFACollection = createNew(fileSource, new File(fileDestination, fileSource.getName()), fileSettings);
		}else{
			pw.println("update exist archive " + strDestination + " for source " + strSource);
			newFACollection = updateExist(fileSource, fileDestination, fileSettings, strFolder);
		}

		newFACollection.stream().forEach(t->System.out.println(Utils.exportFileAttr(t)));
	}
	
	static private Collection<FileAttr> createNew(File fileSource, File fileDestination, File fileSettings) throws IOException {
		Collection<FileAttr> result = null;
		
		// get fileattr from source
		Map<String, FileAttr> mapSettings = getFileAttrsFromFolder(fileSource, true);
		if(mapSettings==null || mapSettings.isEmpty())
			return result;

		// copy folder
		Utils.copyFolder(fileSource, fileDestination);
		
		// write fileattr to file settings
		writeToFileFileAtrrs(fileSettings, mapSettings);

		result = mapSettings.values();
		return result;
	}
	
	static private Collection<FileAttr> updateExist(File fileSource, File fileDestination, File fileSettings, String strFolderName) throws Exception{
		Collection<FileAttr> result = null;
		//long time = System.currentTimeMillis();
		Map<String, FileAttr> mapSettings = Utils.getFileAttrsFromSettings(fileSettings);
		if(mapSettings==null)
			mapSettings = new HashMap<String, FileAttr>();
		
		//map.keySet().stream().forEach(t->System.out.println(t));
		//map.values().stream().forEach(t->System.out.println(printFileAttr(t)));

		//System.out.println("get fileattr from settings " + (System.currentTimeMillis() - time)/1000);
		//time = System.currentTimeMillis();
		
		// update map of all settings fileattr - remove all not exist file in dest folder
		Set<String> setSettingsTmp = new HashSet<String>();
		for(File file: fileDestination.listFiles()){
			Map<String, FileAttr> mapTmp = getFileAttrsFromFolder(file, false);
			if(mapTmp==null || mapTmp.isEmpty())
				continue;
			for(String strKey: mapTmp.keySet()){
				FileAttr fa = mapSettings.get(strKey);
				if(fa!=null)
					setSettingsTmp.add(strKey);
			}
		}
		Map<String, FileAttr> mapSettings2 = new HashMap<String, FileAttr>();
		for(Map.Entry<String, FileAttr> entry: mapSettings.entrySet()){
			if(setSettingsTmp.contains(entry.getKey()))
				mapSettings2.put(entry.getKey(), entry.getValue());
		}
		mapSettings = mapSettings2;
		//System.out.println("update map of all settings fileattr " + (System.currentTimeMillis() - time)/1000);
		//time = System.currentTimeMillis();
		
		// get fileattr from source
		Map<String, FileAttr> mapSourceSettings = getFileAttrsFromFolder(fileSource, false);
		if(mapSourceSettings==null || mapSourceSettings.isEmpty())
			return result;
		
		//System.out.println("get fileattr from source " + (System.currentTimeMillis() - time)/1000);
		//time = System.currentTimeMillis();

		//compare maps
		Map<String, FileAttr> mapNewSettings = new HashMap<String, FileAttr>();
		Map<String, FileAttr> mapNewSettingsNewFiles = new HashMap<String, FileAttr>();
		for(Map.Entry<String, FileAttr> entry: mapSourceSettings.entrySet()){
			FileAttr fa = mapSettings.get(entry.getKey());
			if(fa==null || !fa.getDateChang().equals(entry.getValue().getDateChang()) || fa.getSize()!=entry.getValue().getSize()){
				mapNewSettingsNewFiles.put(entry.getKey(), entry.getValue());
				continue;
			}
			mapNewSettings.put(entry.getKey(), fa);
		}
		
		// copy new files to archive
		List<FileAttr> lstTmp = new ArrayList<FileAttr>();
		if(!mapNewSettingsNewFiles.isEmpty()){
			Path pathNewFolder = Files.createTempDirectory(fileDestination.toPath(), strFolderName);
			if(pathNewFolder==null)
				return result;
			
			for(Map.Entry<String, FileAttr> entry: mapNewSettingsNewFiles.entrySet()){
				String strPathAbsSrc = fileSource.getAbsolutePath() + File.separator +  entry.getKey();
				String hash = Utils.buildHexStringFromByteArray(Utils.createChecksum(new File(strPathAbsSrc)));
				if(hash==null)
					continue;
			
				FileAttr faTmp = mapSettings.get(entry.getKey());
				if(faTmp!=null && faTmp.getHash().equals(hash)){
					mapNewSettings.put(entry.getKey(), faTmp);
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
				FileAttr fa = new FileAttr(pathNewFolder.getFileName() + File.separator + entry.getKey(), entry.getValue().getDateChang(), entry.getValue().getSize(), hash);
				//System.out.println(printFileAttr(fa));
				mapNewSettings.put(entry.getKey(), fa);
				lstTmp.add(fa);
			}
		}
		
		//System.out.println("copy new files to archive " + (System.currentTimeMillis() - time)/1000);
		//time = System.currentTimeMillis();

		// write fileattr to file settings
		writeToFileFileAtrrs(fileSettings, mapNewSettings);
		
		result = lstTmp;
		return result;
	}
	
	static private Map<String, FileAttr> getFileAttrsFromFolder(File folder, boolean getHash) throws IOException {
		Map<String, FileAttr> result = null;
		
		if(folder==null || !folder.exists() || !folder.isDirectory())
			return result;
		
		Map<String, FileAttr> map = new HashMap<String, FileAttr>();
	    //try {
			Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {
			    @Override
			    public FileVisitResult visitFile(Path file,
			    		BasicFileAttributes attrs) throws IOException {
			    	String path = file.toAbsolutePath().toString().substring(folder.getAbsolutePath().length() + 1);
			    	byte[] b = null;
			    	try {
						b = Utils.createChecksum(new File(file.toUri()));
					} catch (Exception e) {
						return FileVisitResult.CONTINUE;
					}
			    	String hash = getHash ? Utils.buildHexStringFromByteArray(b) : null;
			    	map.put(path, new FileAttr(folder.getName() + File.separator + path, new Date(attrs.lastModifiedTime().toMillis()), attrs.size(), hash));
			        return FileVisitResult.CONTINUE;
			    }
			});
		/*} catch (IOException e) {
			e.printStackTrace();
			return result;
		}*/
	    
	    if(!map.isEmpty())
	    	result = map;

	    return result;
	}
	
	static private void writeToFileFileAtrrs(File file, Map<String, FileAttr> map) throws IOException{
		if(file==null)
			return;
		if(map==null || map.isEmpty())
			return;
		
		// write fileattr to file settings
		try(PrintWriter pwSettings = new PrintWriter(new FileWriter(file))){
			for(FileAttr fa: map.values())
				pwSettings.println(Utils.exportFileAttr(fa));
		}/*catch(Exception e){
			e.printStackTrace();
			return;
		}*/
	}

	
}
