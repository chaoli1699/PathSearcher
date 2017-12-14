package cn.cienet.pathsearcher.weight;

import java.util.List;

import android.os.Handler;
import cn.cienet.pathsearcher.interfaces.OnPositionWatchListener;

public class PositionWhatcher implements Runnable{
	
//	private static final String TAG="PositionWatcher";
	private OnPositionWatchListener onPositionWatchListener;
	private List<int[]> pathList;
	private static final int walkSpeed=200;
//	private Handler handler;
	
	public PositionWhatcher(Handler handler){
//		this.handler=handler;
	}
	
	public void setPositionWatcher(OnPositionWatchListener onPositionWatchListener){
		this.onPositionWatchListener=onPositionWatchListener;
	}
	
	public void setPathList(List<int[]> pathList){
		this.pathList=pathList;
	}
	
	/**
	 * 惯性导航
	 * @param pathList
	 */
	private void walkOnPath(List<int[]> pathList){
		
		for(int i=pathList.size()-1;i>=0;i--){
			try {
				Thread.sleep(walkSpeed);
				int[] path=pathList.get(i);	
				
//				if (i%10==0) {//每隔2秒进行一次定位矫正
//					if (!checkIfWalkingOnPath(locationFun(), path)) {
//						//Log.i(TAG, "realPos: "+locationFun()[0]+", "+locationFun()[1]);
//						Message message=new Message();
//						message.what=3;
//						message.arg1=locationFun()[0];
//						message.arg2=locationFun()[1];
//						handler.sendMessage(message);
//						break;
//					}
//				}   
				
				pathList.remove(i);
				onPositionWatchListener.onPositionChanged(path[0], path[1], pathList);
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
//	private boolean checkIfWalkingOnPath(int[] currentPos, int[] shouleBePos ){
//		float distance=(float) Math.sqrt((Math.pow((currentPos[0]-shouleBePos[0]), 2)+Math.pow((currentPos[1]-shouleBePos[1]), 2)));
//		if (distance>MapBuilder.mapBean.getLocationErrorAllowed()*100) {
//			return false;
//		}
//		return true;
//	}
	
//	private int[] locationFun(){
//		int[] location=new int[2];
//		//TODO location by iBeacon or some other thing..
//		
//		//Test
//		Random random=new Random();
//		location[0]=random.nextInt(145)+315;
//		location[1]=random.nextInt(80)+210;
//		
//		return location;
//		
//	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		walkOnPath(pathList);
	}

}
