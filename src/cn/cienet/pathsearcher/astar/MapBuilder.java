package cn.cienet.pathsearcher.astar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import cn.cienet.pathsearcher.bean.MapBean;
import cn.cienet.pathsearcher.bean.StoneArea;
import cn.cienet.pathsearcher.interfaces.OnDelFileListener;
import cn.cienet.pathsearcher.sql.BeanFactory;
import cn.cienet.pathsearcher.utils.FileUtils;

public class MapBuilder {
	
    public static int[][] map;
    public static float SCALETOREAL=1.0f;
    public static MapBean mapBean;
    private volatile static MapBuilder instance;
    
    private static final String TAG="MapBuilder";
    private String mapName="astar_map.txt";
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
    
    private void initMapName(int mapId){
    	StringBuffer sb=new StringBuffer();
    	sb.append("astar_map");
    	sb.append(mapId);
    	sb.append(".txt");
    	mapName=sb.toString();
    }
    
    /**
     * 读取上次使用过的地图（默认mapID=1）
     * @param context
     */
    public void initMap(Context context){
    	initMap(context, getLastMapId());
    }
    
    /**
     * 初始化地图
     * @param context
     * @param mapId
     */ 
    public void initMap(Context context,int mapId){
    	
    	initMapName(mapId);
    	mapBean=initMapBean(context, mapId+"");
    	
    	if (mapBean==null) {
			return ;
		}
    	
    	checkComplexRateOfMap(mapBean);
    	
    	try {  
    		map=FileUtils.read2RateArrayFromLocalFile(mapPath, mapName);
			if(map==null){
				FileUtils.write2RateArray2File(mapPath, mapName, createMap(mapBean));
				initMap(context,mapId);
			}else {
				Log.i(TAG,"Load map successed!");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void delMapFile(OnDelFileListener onDelFileListener){
    	FileUtils.deleteFile(mapPath, mapName, onDelFileListener);
    }
    
    public void markCurrentMap(int mapId){
    	try {
    		FileUtils.writeString2LocalFile(mapPath, "mapMark", "last mapId is: "+mapId);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private int getLastMapId(){
    	try {
			String mark=FileUtils.readStringFromLocalFile(mapPath, "mapMark");
			if (mark!=null) {
				String[] temp=mark.split(":");
				return Integer.valueOf(temp[1].trim());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return 1;
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
    
    private MapBean initMapBean(Context context, String mapId){
    	
    	try {
    		String content=FileUtils.readStringFromLocalFile(mapPath, "mapBean"+mapId);
    		if (content==null) {
    			MapBean mapBean=new BeanFactory().getMapBean().getMapBeanById(context, mapId);
    			content=new Gson().toJson(mapBean);
    			FileUtils.writeString2LocalFile(mapPath, "mapBean"+mapId, content);
    		}
    		
        	return new Gson().fromJson(content, MapBean.class);

		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    	
    	return null;	
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
