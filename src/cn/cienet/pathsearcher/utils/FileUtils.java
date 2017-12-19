package cn.cienet.pathsearcher.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import cn.cienet.pathsearcher.interfaces.OnDelFileListener;

public class FileUtils {
	
	private static final String TAG="FileUtils";

	/**
	 * writeString2LocalFile
	 * @param filePath
	 * @param fileName
	 * @param content
	 * @throws IOException
	 */
	public static void writeString2LocalFile(String filePath, String fileName, String content)throws IOException{
    	File file=new File(filePath, fileName);
    	
    	if(!file.exists()){
    		if(file.getParentFile().mkdirs()){
    			Log.i(TAG,"Create "+ fileName+ " successed!");
    		}else{
    			Log.i(TAG, "Create "+ fileName+ " failed!");
    		}
    	}
    	
    	FileWriter  fo=new FileWriter (file);
		fo.write(EncryptUtils.encodeString(content));
		fo.flush();
		fo.close();
    }
    
	/**
	 * readStringFromLocalFile
	 * @param filePath
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
    public static String readStringFromLocalFile(String filePath, String fileName) throws IOException{
    	
    	File mapFile=new File(filePath, fileName);
    	
    	if(!mapFile.exists()){
    		return null;
    	}
    	
    	BufferedReader in=new BufferedReader(new FileReader(mapFile));
    	String str=in.readLine();
    	in.close();
    	
    	return EncryptUtils.decodeString(str);
    }
    
    /**
     * write2RateArray2File
     * @param filePath
     * @param fileName
     * @param map
     * @throws IOException
     */
    public static void write2RateArray2File(String filePath, String fileName, int[][] map)throws IOException{
    	File file=new File(filePath, fileName);
    	
    	if(!file.exists()){
    		if(file.getParentFile().mkdirs()){
    			Log.i(TAG,"Create "+fileName+ " success!");
    		}else{
    			Log.i(TAG, "Create "+fileName+ " fail!");
    		}
    		
    		FileWriter  fo=new FileWriter (file);
			for(int i=0;i<map.length;i++){
				for(int j=0;j<map[i].length;j++){
					fo.write(map[i][j]+"\t");
				}
				fo.write("\r\n");
			}
			fo.flush();
			fo.close();
    	}
    }
    
    /**
     * read2RateArrayFromLocalFile
     * @param filePath
     * @param fileName
     * @return
     * @throws IOException
     */
    public static int[][] read2RateArrayFromLocalFile(String filePath, String fileName) throws IOException{
    	
    	File file=new File(filePath, fileName);
    	
    	if(!file.exists()){
    		return null;
    	}
    	
    	BufferedReader in=new BufferedReader(new FileReader(file));
    	String line;
    	
    	@SuppressWarnings({ "rawtypes", "unchecked" })
		List<String[]> tempList =new ArrayList() ;
    	String[] temp = null;
    	
    	while((line=in.readLine())!=null){
    		temp = line.split("\t");
    		tempList.add(temp);
    	}
    	in.close();
    	
        int[][] arr=new int[tempList.size()][temp.length];
        
        for(int i=tempList.size()-1;i>=0;i--){
        	for(int j=0;j<temp.length;j++){
        		arr[i][j]=Integer.parseInt(tempList.get(i)[j]);
        	}
        }
    	
    	return arr;
    }
    
    /**
     * 将assets下的资源复制到应用程序的databases目录下
     * @param context
     * @param filePath
     * @param fileName
     * @throws IOException
     */
    public static void copyAssetsToDB(Context context, String filePath, String fileName) throws IOException {
        File file=new File(filePath, fileName);
        if(file.getParentFile().mkdirs()){
        	Log.i(TAG, "Create file success!");
        }else{
        	Log.i(TAG, "Create file failed!");
        }
        
        //打开assest文件，获得输入流
        InputStream is = context.getClassLoader().getResourceAsStream("assets/"+fileName);
        //获得写入文件的输出流
        FileOutputStream fos = new FileOutputStream(file);

        byte[] data = new byte[2 * 1024];
        int len;
        while ((len = is.read(data)) > 0){
            fos.write(data, 0, len);
        }

        fos.flush();
        is.close();
        fos.close();
    }
    
    /**
     * deleteFile
     * @param filePath
     * @param fileName
     * @param onDelFileListener
     */
    public static void deleteFile(String filePath, String fileName, OnDelFileListener onDelFileListener){
    	File file=new File(filePath, fileName);
    	if (file.exists()) {
    		boolean result = file.delete();
    		String str;
    		if (result) {
    		    str="Del "+fileName+" success!";
				Log.i(TAG, str);
				onDelFileListener.onDelReuslt(true, str);
				
			}else {
				str="Del "+fileName+ " fail!";
				Log.i(TAG, str);
				onDelFileListener.onDelReuslt(false, str);
			}
		}
    }
}
