package cn.cienet.pathsearcher.sql;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import cn.cienet.pathsearcher.interfaces.OnDelFileListener;

public class DBManager {
	
	private static final String TAG="SQLManager";
	private static final String dbName="android_astar.db";
	private static final String dbPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/cn.cienet.astar/database/";
	private File dbFile;
	
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
	    dbFile=new File(dbPath,dbName);
		
		if(dbFile.exists()){
			Log.i(TAG, "file:"+dbPath+dbName+" exists");
			return SQLiteDatabase.openOrCreateDatabase(dbFile,null);
		}else{
			try {
				copyAssetsToDB(context, dbFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return openDatabase(context);
	}
	
	public void deleteDbFile(OnDelFileListener onDelFileListener){
		File dbFile=new File(dbPath, dbName);
		if (dbFile.exists()) {
			boolean result = dbFile.delete();
			String str;
	   		if (result) {
    		    str="Del local database success!";
				Log.i(TAG, str);
				onDelFileListener.onDelResult(true, str);
				
			}else {
				str="Del local database fail!";
				Log.i(TAG, str);
				onDelFileListener.onDelResult(false, str);
			}
		}
	}
	
	/**
     * 将assets下的资源复制到应用程序的databases目录下
     * @param context 上下文
     * @param dbName assets下的资源的文件名
     */
    private void copyAssetsToDB(Context context, File dbFile) throws IOException {
        
        if(dbFile.getParentFile().mkdirs()){
        	Log.i(TAG, "Create file success!");
        }else{
        	Log.i(TAG, "Create file failed!");
        }
        
        //打开assest文件，获得输入流
        InputStream is = context.getClassLoader().getResourceAsStream("assets/"+dbName);
        //获得写入文件的输出流
        FileOutputStream fos = new FileOutputStream(dbFile);

        byte[] data = new byte[2 * 1024];
        int len;
        while ((len = is.read(data)) > 0){
            fos.write(data, 0, len);
        }

        fos.flush();
        is.close();
        fos.close();
    }

}
