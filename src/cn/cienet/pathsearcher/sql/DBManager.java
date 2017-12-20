package cn.cienet.pathsearcher.sql;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import cn.cienet.pathsearcher.interfaces.OnDelFileListener;
import cn.cienet.pathsearcher.utils.FileUtils;

public class DBManager {
	
	private static final String TAG="SQLManager";
	private static final String dbName="android_astar.db";
	private static final String dbPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/cn.cienet.astar/database/";
	//private File dbFile;
	
	private volatile static DBManager instance;
	

	private DBManager(){}
	
	public static DBManager build(){
		if(instance==null){
			synchronized(DBManager.class){
				if(instance==null){
					instance=new DBManager();
				}
			}
		}
		
		return instance;
	}
	
	public SQLiteDatabase openDatabase(Context context){
		//filePath=context.getDatabasePath(fileName).getPath();
	    File dbFile=new File(dbPath, dbName);
		
		if(dbFile.exists()){
			Log.i(TAG, "file:"+ dbName+ " exists");
			return SQLiteDatabase.openOrCreateDatabase(dbFile, null);
		}else{
			try {
				FileUtils.copyAssetsToDB(context, dbPath, dbName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return openDatabase(context);
	}
	
	public void deleteDbFile(OnDelFileListener onDelFileListener){
		FileUtils.deleteFile(dbPath, dbName, onDelFileListener);
	}
	
	

}
