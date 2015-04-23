package ru.seits.simplebackup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Map;

public class Restore {

	static private final int intCountArgs = 3;
	
	static private final int intArgsIdSource = 0;
	static private final int intArgsIdDestination = 1;
	static private final int intArgsIdSettings = 2;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		//String[] args2 = {"d:\\1", "d:\\2", "d:\\1.txt", "tmp"};
		//String[] args2 = {"d:\\archive\\info", "d:\\archive2", "d:\\archive\\archInfo\\arch_info.txt"};
		String[] args2 = args;
		
		PrintWriter pw = new PrintWriter(System.out, true);
		
		// checks and init
		if(args2.length < intCountArgs){
			pw.println("count args less then " + intCountArgs);
			return;
		}
		
		// init main vars
		String strSource = args2[intArgsIdSource];
		String strDestination = args2[intArgsIdDestination];
		String strSettings = args2[intArgsIdSettings];
		
		// checks
		File fileSource = new File(strSource);
		if(!fileSource.exists() || !fileSource.isDirectory()){
			pw.println("source folder not exist " + strSource);
			return;
		}
		
		File fileDestination = new File(strDestination);
		if(fileDestination.exists() && !fileSource.isDirectory()){
			pw.println("destination folder not a directory " + strDestination);
			return;
		}
		
		File fileSettings = new File(strSettings);
		if(!fileSettings.exists() || !fileSettings.isFile()){
			pw.println("settings not exist " + strSettings);
			return;
		}

		// get fileattr from source
		Map<String, FileAttr> mapSettings = Utils.getFileAttrsFromSettings(fileSettings);
		if(mapSettings==null){
			pw.println("wrong file settings");
			return;
		}
		
		//copy files to new location
		pw.println("restoring files frome archive " + strSource + " to " + strDestination);
		if(!fileDestination.exists())
			fileDestination.mkdirs();
		for(Map.Entry<String, FileAttr> entry: mapSettings.entrySet()){
			File fileTmpSrc = new File(fileSource.getAbsolutePath() + File.separator + entry.getValue().getPath());
			if(!fileTmpSrc.exists()){
				pw.println("file not exist: " + Utils.exportFileAttr(entry.getValue()));
				continue;
			}
			File fileTmpDst = new File(fileDestination.getAbsolutePath() + File.separator + entry.getKey());
			if(!fileTmpDst.getParentFile().exists()){
				try{
					fileTmpDst.getParentFile().mkdirs();
				}catch(Exception e){
					pw.println("parent folders making error: " + e + " " + Utils.exportFileAttr(entry.getValue()));
					continue;
				}
			}
			try{
				Files.copy(fileTmpSrc.toPath(), fileTmpDst.toPath());
			}catch(Exception e){
				pw.println("copy file error: " + e + Utils.exportFileAttr(entry.getValue()));
				continue;
			}
			
			//pw.println(Utils.exportFileAttr(entry.getValue()));
		}
		
	}

}
