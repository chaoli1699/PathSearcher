package cn.cienet.pathsearcher.weight;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import cn.cienet.baidutts.util.BaiduTTSHelper;
import cn.cienet.pathsearcher.astar.MapBuilder;
import cn.cienet.pathsearcher.astar.PathSearcher;
import cn.cienet.pathsearcher.astar.PositionWhatcher;
import cn.cienet.pathsearcher.bean.VoiceBean;
import cn.cienet.pathsearcher.interfaces.OnPathSearchListener;
import cn.cienet.pathsearcher.interfaces.OnPositionWatchListener;
import cn.cienet.pathsearcher.sql.BeanFactory;
import cn.cienet.pathsearcher.utils.TTSConstants;

public class PSTMapView extends PSMapView {
	
	private static final String TAG="PSTMapView";
	
	/*
	 * ”Ô“Ùµº∫Ω ≤Œ ˝
	 */
	private BaiduTTSHelper baiduTTSHelper;
	
    private String[] voiceNames =
    	       {"WELCOME",
    			"GUIDE",
    			"SEARCH_PATH",
    			"SEARCH_SUCCESS",
    			"ALREADY_SELECT",
    			"AS_UR_AIM",
    			"AT_UR_AIM"};
    
    private String[] voiceDetails;
    private Map<String, String> ttsmap;
    
    private static final int TTS_INIT_SUCCESS=990;
    
    private Handler pstmHandler=new Handler(){
    	
    	@Override
    	public void handleMessage(Message msg) {
    		// TODO Auto-generated method stub
    		super.handleMessage(msg);
    		
    		postInvalidate();
			switch (msg.what) {
			case JUST_REFRESH_VIEW:
				
				break;
			case READY_TO_SEARCH:
				//Prepare to search path
				if (baiduTTSHelper!=null) {
					baiduTTSHelper.speak(voiceDetails[getVoiceIndex("SEARCH_PATH")]);
				}
				pathSearcherThread.start();
//				THREAD_POOL_EXECUTOR.execute(pathSearcher);
				break;
			case SEARCH_SUCCESS:
				//Walk to aim position
				if (mPathList!=null&&mPathList.size()>0) {
					if (baiduTTSHelper!=null) {
						baiduTTSHelper.speak(voiceDetails[getVoiceIndex("SEARCH_SUCCESS")]);
					}
					positionWhatcher.setPathList(mPathList);
					postionWatcherThread.start();				
//					THREAD_POOL_EXECUTOR.execute(positionWhatcher);
				}
				break;
			case TTS_INIT_SUCCESS:
				if (baiduTTSHelper!=null) {
					baiduTTSHelper.speak(voiceDetails[getVoiceIndex("WELCOME")]);
					baiduTTSHelper.speak(voiceDetails[getVoiceIndex("GUIDE")]);
				}
				break;
				
			default:    
				break;
			}
    	}
    };

	public PSTMapView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	
	public PSTMapView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	
	public PSTMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initPaint();
		initMapBean();
	}
	
    private void initTTSHelper(Context context){
		
		List<VoiceBean> voiceBeans = new BeanFactory().getVoiceBean().getVoiceBeans(context);
		
		if (voiceBeans!=null&&ttsmap!=null) {
			voiceDetails=new String[voiceNames.length];
			for (VoiceBean voiceBean : voiceBeans) {
				for(int i=0; i<voiceNames.length; i++){
					if (voiceBean.getVoiceName().equals(voiceNames[i])) {
						voiceDetails[i]=voiceBean.getVoiceDetail();
					}
				}	
			}
			
	 		baiduTTSHelper=BaiduTTSHelper.build();
	 		baiduTTSHelper.initialTts(context, pstmHandler, ttsmap);
		}
	}
	
	public void setTTSConstants(Context context, String appId, String apiKey, String secretKey){
		ttsmap=new HashMap<String, String>();
		ttsmap.put("appId", TTSConstants.appId);
		ttsmap.put("appKey", TTSConstants.apiKey);
		ttsmap.put("secretKey", TTSConstants.secretKey);
		
		initTTSHelper(context);
	}
	
	public void releaseBaiduTTSHelper(){
		
		if (baiduTTSHelper!=null) {
			baiduTTSHelper.stop();
			baiduTTSHelper.release();
		}
	}
	
	public void setPathSearcher(PathSearcher pathSearcher) {
		// TODO Auto-generated method stub
		this.pathSearcher=pathSearcher;
		positionWhatcher=new PositionWhatcher(pstmHandler);
		
        pathSearcher.setOnPathSearchListener(new OnPathSearchListener() {
			
			@Override
			public void onStartAndEndPrepared(boolean ifClearPath, float sx, float sy, float ex, float ey) {
				// TODO Auto-generated method stub
				clearPath=ifClearPath;
				currentPosX=sx;
				currentPosY=sy;
				endX=ex;
				endY=ey;
				//Log.i(TAG, "----------------onStartAndEndPrepared");
				if (baiduTTSHelper!=null) {
					baiduTTSHelper.stop();
					baiduTTSHelper.speak(voiceDetails[getVoiceIndex("ALREADY_SELECT")]
							+aimArea.getAimDescribe()
					+voiceDetails[getVoiceIndex("AS_UR_AIM")]);
				}
				pstmHandler.sendEmptyMessage(READY_TO_SEARCH);
			}

			@Override
			public void onSearchingPathComplate(List<int[]> pathList) {
				// TODO Auto-generated method stub
				clearPath=false;
				mPathList=pathList;
				pstmHandler.sendEmptyMessage(SEARCH_SUCCESS);
			}

			@Override
			public void onSearchingPathFail(String msg) {
				// TODO Auto-generated method stub
				Log.e(TAG, msg);
			}
		});
		
		positionWhatcher.setPositionWatcher(new OnPositionWatchListener() {
			
			@Override
			public void onPositionChanged(int x, int y, List<int[]> newPathList) {
				// TODO Auto-generated method stub
				currentPosX=x;
				currentPosY=y;
				mPathList=newPathList;
				if (baiduTTSHelper!=null && mPathList.size()<1) {
					baiduTTSHelper.stop();
					baiduTTSHelper.speak(voiceDetails[getVoiceIndex("AT_UR_AIM")]);
				}
				pstmHandler.sendEmptyMessage(JUST_REFRESH_VIEW);
			}
		});
		pathSearcherThread=new Thread(pathSearcher);
		postionWatcherThread=new Thread(positionWhatcher);
		
	}
	
	private int getVoiceIndex(String str){
		for(int i=0; i<voiceNames.length; i++){
			if (voiceNames[i].equals(str)) {
				return i;
			}
		}
		return -1;
	}

}
