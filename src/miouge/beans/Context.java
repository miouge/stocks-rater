package miouge.beans;

import java.io.File;

public class Context {

	// use environment variable SRT_ROOT_FOLDER to define this path
	public String rootFolder;
	
	public String settingsFilePath;
	
	private void init() throws Exception {

		String rootFolderEnv = System.getenv( "SRT_SCRATCH_FOLDER" );
		if( rootFolderEnv == null ) {
			throw new Exception( "SRT_SCRATCH_FOLDER environment variable is undefined !" );
		}
		this.rootFolder = rootFolderEnv;
		System.out.println( "workspace folder will be : " + this.rootFolder );
		
		settingsFilePath = this.rootFolder + "/settings.ini";
		File settingFile = new File( settingsFilePath );
		if( settingFile.exists() == false ) {
			
			System.err.println( settingFile + " was not found : will use default settings." );
		}
	}
	
	public Context() throws Exception {
		
		init();		
	}
}
