package cn.cienet.pathsearcher.astar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import cn.cienet.pathsearcher.bean.MapBean;
import cn.cienet.pathsearcher.bean.StoneArea;
import cn.cienet.pathsearcher.sql.BeanFactory;

public class MapBuilder {
	
    public static int[][] map;
    public static float SCALETOREAL=1.0f;
    public static MapBean mapBean;
    private volatile static MapBuilder instance;
    
    private static final String TAG="MapBuilder";
    private static final String mapName="astar_map.txt";
    private static final String mapPath=Environment.getExternalStorageDirectory().getAbsolutePath()+"/cn.cienet.astar/map/";
    
    private MapBuilder(){}
    
    public static MapBuilder build(){
    	if(instance==null){
    		synchronized(MapBuilder.class){
    			if(instance==null){
    				instance=new MapBuilder();
    			}
    		}
    	}
    	return instance;
    }
    
    /**
     * 初始化地图
     * @param context
     * @param mapId
     */
    public void initMap(Context context,int mapId){
    	try {
    		
    		mapBean=new BeanFactory().getMapBean().getMapBeanById(context, mapId+"");
    		if (mapBean!=null) {
    			checkComplexRateOfMap(mapBean);
    			
    			if(readMapFromLocalFile()<0){
    				saveMapToLocalFile(createMap(mapBean));
    				initMap(context,mapId);
    			}
    			Log.i(TAG,"Load map successed!");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void checkComplexRateOfMap(MapBean mapBean){
    	
    	int widthOrHeight = Math.max(mapBean.getmWidth(), mapBean.getmHeight());
    	
    	final int bestWOH=300;
    	int mScale=1, insertUnit=bestWOH;
    	while (insertUnit<=2400) {
    		mScale++;
    		if (widthOrHeight>bestWOH && widthOrHeight<insertUnit+bestWOH) {
    			SCALETOREAL=mScale;
    			break;
    			}
    		insertUnit=insertUnit*2;
    		}
    	Log.i(TAG, "SCALETOREAL:"+SCALETOREAL);
    }
    
    
    private void saveMapToLocalFile(int[][] map)throws IOException{
    	File mapFile=new File(mapPath,mapName);
    	
    	if(!mapFile.exists()){
    		if(mapFile.getParentFile().mkdirs()){
    			Log.i(TAG,"Create mapFile successed!");
    		}else{
    			Log.i(TAG, "Create mapFile failed!");
    		}
    		
    		FileWriter  fo=new FileWriter (mapFile);
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
    
    private int readMapFromLocalFile() throws IOException{
    	
    	File mapFile=new File(mapPath,mapName);
    	
    	if(!mapFile.exists()){
    		return -1;
    	}
    	
    	BufferedReader in=new BufferedReader(new FileReader(mapFile));
    	String line;
    	
    	@SuppressWarnings({ "rawtypes", "unchecked" })
		List<String[]> tempList =new ArrayList() ;
    	String[] temp = null;
    	
    	while((line=in.readLine())!=null){
    		temp = line.split("\t");
    		tempList.add(temp);
    	}
    	in.close();
    	
        map=new int[tempList.size()][temp.length];
        
        for(int i=tempList.size()-1;i>=0;i--){
        	for(int j=0;j<temp.length;j++){
        		map[i][j]=Integer.parseInt(tempList.get(i)[j]);
        	}
        }
    	
    	return 0;
    }
    
    /**
     * 创建地图数组
     * @param mapBean
     * @return
     */
    private int[][] createMap(MapBean mapBean){
    	@SuppressWarnings({ "unchecked", "rawtypes" })
		List<int[]> stoneList=new ArrayList();
		List<StoneArea> stoneAreas=mapBean.getStoneList();
		for(int i=0;i<stoneAreas.size();i++){
			int[] stone=new int[4];
			stone[0]=(int) (stoneAreas.get(i).getStartX()/SCALETOREAL);
			stone[1]=(int) (stoneAreas.get(i).getStartY()/SCALETOREAL);
			stone[2]=(int) (stoneAreas.get(i).getEndX()/SCALETOREAL);
			stone[3]=(int) (stoneAreas.get(i).getEndY()/SCALETOREAL);
			stoneList.add(stone);
		}
        return createMap((int) (mapBean.getmWidth()/SCALETOREAL), (int) (mapBean.getmHeight()/SCALETOREAL), stoneList);
    }
	
    /**
     * 创建地图数组2
     * @param width
     * @param height
     * @param stoneList eg:{ax, ay, bx, by} a,b分别问障碍区域（矩形）左上角和右下角的点
     * @return int[][] map
     */
	private int[][] createMap(int width, int height,List<int[]> stoneList){
		int[][] map=new int[height][width];
		
		for(int k=0;k<stoneList.size();k++){
			int[] stone=stoneList.get(k);
			//setStones(map,width,height,stone[0],stone[1],stone[2],stone[3]);
			for(int i=0;i<height;i++){
				for(int j=0;j<width;j++){
					if(j>=stone[0]&&j<=stone[2]&&i>=stone[1]&&i<=stone[3]){
						map[i][j]=1;
					}
				}
			}
		}
		
		return map;
	}
	
	/*
	private void setStones(int[][] map,int width,int height,int ax,int ay,int bx,int by){
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				if(j>=ax&&j<=bx&&i>=ay&&i<=by){
					map[i][j]=1;
				}
			}
		}
	}
	*/
	
	/**
	 * 输出地图数组
	 * @param map
	 */
	public void printMap(int[][] map){
		for(int i=0;i<map.length;i++){
			System.out.print('\n');
			for(int j=0;j<map[i].length;j++){
				System.out.print(map[i][j]);
			}
		}
	}
	
	/**
	 * 输出路径坐标
	 * @param pointList
	 */
	public void printPathPoints(List<int[]> pointList){
		for(int i=pointList.size()-1;i>=0;i--){
			System.out.print('\n');
			int[] point=pointList.get(i);
			System.out.print("point"+i+":x="+point[0]+",y="+point[1]);
		}
	}
}
