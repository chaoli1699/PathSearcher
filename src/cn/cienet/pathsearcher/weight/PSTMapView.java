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
	 * �������� ����
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
    
    private static final int TTS_INIT_SUCCESS=990;
    private Context context;
    
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
				LOCK_AIM_POINT=true;
				THREAD_POOL_EXECUTOR.execute(pathSearcher);
				break;
			case SEARCH_SUCCESS:
				//Walk to aim position
				if (mPathList!=null&&mPathList.size()>0) {
					positionWhatcher.setPathList(mPathList);		
					THREAD_POOL_EXECUTOR.execute(positionWhatcher);
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
		this.context=context;
		
		initPaint();
		initMapBean();
	}
	
    private void initTTSHelper(Map<String, String>  ttsmap){
		
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
	
	public void setTTSConstants(String appId, String apiKey, String secretKey){
		Map<String, String> ttsmap=new HashMap<String, String>();
		ttsmap.put("appId", TTSConstants.appId);
		ttsmap.put("appKey", TTSConstants.apiKey);
		ttsmap.put("secretKey", TTSConstants.secretKey);
		
		initTTSHelper(ttsmap);
	}
	
	public void releaseBaiduTTSHelper(){
		
		if (baiduTTSHelper!=null) {
			baiduTTSHelper.stop();
			baiduTTSHelper.release();
			baiduTTSHelper=null;
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
				CLEAR_PATH=ifClearPath;
				currentPosX=sx;
				currentPosY=sy;
				endX=ex;
				endY=ey;
				
				if (baiduTTSHelper!=null) {
					baiduTTSHelper.stop();
					baiduTTSHelper.speak(voiceDetails[getVoiceIndex("ALREADY_SELECT")]
							+aimArea.getAimDescribe()
					+voiceDetails[getVoiceIndex("AS_UR_AIM")]);
					baiduTTSHelper.speak(voiceDetails[getVoiceIndex("SEARCH_PATH")]);
				}
				pstmHandler.sendEmptyMessage(READY_TO_SEARCH);
			}

			@Override
			public void onSearchingPathComplate(List<int[]> pathList) {
				// TODO Auto-generated method stub
				CLEAR_PATH=false;
				mPathList=pathList;
				
				if (baiduTTSHelper!=null) {
					baiduTTSHelper.speak(voiceDetails[getVoiceIndex("SEARCH_SUCCESS")]);
				}
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
				if (mPathList.size()<1) {
					LOCK_AIM_POINT=false;
					
					if (baiduTTSHelper!=null) {
						baiduTTSHelper.stop();
						baiduTTSHelper.speak(voiceDetails[getVoiceIndex("AT_UR_AIM")]);
					}
				}
				pstmHandler.sendEmptyMessage(JUST_REFRESH_VIEW);
			}
		});
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
