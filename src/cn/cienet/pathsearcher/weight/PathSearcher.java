package cn.cienet.pathsearcher.weight;

import java.util.List;

import cn.cienet.pathsearcher.astar.AStar;
import cn.cienet.pathsearcher.astar.MapBuilder;
import cn.cienet.pathsearcher.astar.Node;
import cn.cienet.pathsearcher.astar.PathInfo;
import cn.cienet.pathsearcher.interfaces.OnPathSearchListener;

public class PathSearcher implements Runnable {
	
	private OnPathSearchListener onPathSearchListener;
	private int startX, startY, endX, endY;

	public void setOnPathSearchListener(OnPathSearchListener onPathSearchListener){
		this.onPathSearchListener=onPathSearchListener;
	}
	
	public  void setStartAndEnd(boolean ifClearPath, float sx , float sy, float ex, float ey ){
		
		startX=(int)(sx/MapBuilder.SCALETOREAL);
		startY=(int)(sy/MapBuilder.SCALETOREAL);
		endX=(int)(ex/MapBuilder.SCALETOREAL);
		endY=(int)(ey/MapBuilder.SCALETOREAL); 
		
		onPathSearchListener.onStartAndEndPrepared(ifClearPath, startX, startY, endX, endY);
	}
	
	private void searchPath(int[][] map,int sx, int sy,int ex,int ey){
		
		synchronized (this){
			List<int[]> pathList=new AStar()
					.start(new PathInfo(MapBuilder.map, 
							new Node(sx, sy),
							new Node(ex, ey)));
			
			if (pathList!=null) {
				onPathSearchListener.onSearchingPathComplate(pathList);
			}
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		searchPath(MapBuilder.map, startX, startY, endX, endY);
	}
}
