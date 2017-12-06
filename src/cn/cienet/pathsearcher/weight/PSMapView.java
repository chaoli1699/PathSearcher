package cn.cienet.pathsearcher.weight;

import java.util.List;
import java.util.concurrent.Executor;
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import cn.cienet.pathsearcher.astar.MapBuilder;
import cn.cienet.pathsearcher.astar.PathSearcher;
import cn.cienet.pathsearcher.astar.PositionWhatcher;
import cn.cienet.pathsearcher.bean.AimArea;
import cn.cienet.pathsearcher.bean.StoneArea;
import cn.cienet.pathsearcher.interfaces.OnPathSearchListener;
import cn.cienet.pathsearcher.interfaces.OnPointClickListener;
import cn.cienet.pathsearcher.interfaces.OnPositionWatchListener;

public class PSMapView extends ScaleImageView {
	
	private static final String TAG="PSMapView";

	/*
	 * 屏幕长宽
	 */
	private int screenWidth;
	private int screenHeight;
	/*
	 * 图形矩阵缩放量
	 */
	private float[] matrixValues;
	/*
	 * 画笔
	 */
	private Paint posPaint;
	private Paint posPaint2;
	private Paint aimPaint;
	private Paint stonePaint;
	private Paint pathPaint;
	private Paint textPaint;
	private Paint textPaint2;
	/*
	 * 路线起止点坐标
	 */
	private float currentPosX=-1;
	private float currentPosY=-1;
	private float endX=-1;
	private float endY=-1;
	/*
	 * 路线
	 */
	private List<int[]> mPathList;
	private Path astarPath;
	/*
	 * 图片加载时，为适应view尺寸的缩放量
	 */
	private float displayScale=1;
	/*
	 * 线程池参数
	 */
//	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
//	private static final int CORE_POOL_SIZE =CPU_COUNT *2;
//	private static final int MAXIMUM_POOL_SIZE = CPU_COUNT *2+1;
//	private static final long KEEP_ALIVE=10L;
	/*
	 * 寻路线程
	 */
	private PathSearcher pathSearcher;
//	private Thread pathSearcherThread;
	/*
	 * 循迹线程
	 */
	private PositionWhatcher positionWhatcher;
//	private Thread postionWatcherThread;
	/*
	 * 清除路线开关
	 */
	private boolean clearPath=false;
	/*
	 * 显示障碍物开关
	 */
	private boolean showStonesAllowed=false;
	/*
	 * 显示定位容错范围
	 */
	private boolean showPosErrAllowed=false;
	private float locationErrorAllowdRadiu=300;
	/*
	 * 目标地参数
	 */
	private OnPointClickListener onPointClickListener;
	private List<AimArea> aimAreas;
	private List<StoneArea> stoneAreas;
	
	private static final float pointRadiu=20;
	
	protected static final int JUST_REFRESH_VIEW=0;
	protected static final int READY_TO_SEARCH=1;
	protected static final int SEARCH_SUCCESS=2;
	protected static final int WALK_OUT_OF_PATH=3;
	
	private boolean LOCK_VIEW=false;
	
	@SuppressLint("HandlerLeak")
	private Handler handler=new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			
			postInvalidate();
			switch (msg.what) {
			case JUST_REFRESH_VIEW:
				//Normal status
				break;
			case READY_TO_SEARCH:
				//Prepare to search path
//				pathSearcherThread.start();
				LOCK_VIEW=true;
				THREAD_POOL_EXECUTOR.execute(pathSearcher);
				break;
			case SEARCH_SUCCESS:
				//Walk to aim position
				positionWhatcher.setPathList(mPathList);
//				postionWatcherThread.start();
				THREAD_POOL_EXECUTOR.execute(positionWhatcher);
				break;
			case WALK_OUT_OF_PATH:
				//Walking out of path, warning to host and reset path
				pathSearcher.setStartAndEnd(false, msg.arg1, msg.arg2,
						endX*MapBuilder.SCALETOREAL, endY*MapBuilder.SCALETOREAL);
				break;
				
			default:    
				break;
			}
		};
	};
	
	private static final ThreadFactory sThreadFactory=new ThreadFactory() {
		
		private final AtomicInteger mCount=new AtomicInteger();
		
		@Override
		public Thread newThread(Runnable r) {
			// TODO Auto-generated method stub
			return new Thread(r,TAG+" #"+ mCount.getAndIncrement());
		}
	};
//	
	public static final Executor THREAD_POOL_EXECUTOR = 
			Executors.newSingleThreadExecutor(sThreadFactory);
//			new ThreadPoolExecutor(
//					CORE_POOL_SIZE,
//			        MAXIMUM_POOL_SIZE,
//			        KEEP_ALIVE,
//			        TimeUnit.SECONDS,
//			        new LinkedBlockingDeque<Runnable>(),
//			        sThreadFactory);

	public PSMapView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	
	public PSMapView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	
	public PSMapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		initPaint();
		initMapBean();
	}
	
	private void initPaint(){
		posPaint=new Paint();
		posPaint.setStyle(Paint.Style.FILL);
		posPaint.setStrokeWidth((float) 4.0);
		posPaint.setColor(Color.BLUE);
		posPaint.setAntiAlias(true);
		
		posPaint2=new Paint();
		posPaint2.setStyle(Paint.Style.STROKE);
		posPaint2.setStrokeWidth((float) 4.0);
		posPaint2.setColor(Color.YELLOW);
		posPaint2.setAntiAlias(true);
		
		aimPaint=new Paint();
		aimPaint.setStyle(Paint.Style.FILL);
	    aimPaint.setStrokeWidth((float) 4.0);
		aimPaint.setColor(Color.RED);
		aimPaint.setAntiAlias(true);
		
		stonePaint=new Paint();
		stonePaint.setStyle(Paint.Style.FILL);
		stonePaint.setStrokeWidth((float) 4.0);
		stonePaint.setColor(Color.GRAY);
		stonePaint.setAntiAlias(true);
		
		pathPaint=new Paint();
		pathPaint.setStyle(Paint.Style.STROKE);
		pathPaint.setStrokeWidth((float) 5.0);
		pathPaint.setColor(Color.RED);
		pathPaint.setAntiAlias(true);
		
		textPaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize((float) 40.0);
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        textPaint2=new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint2.setColor(Color.WHITE);
        textPaint2.setAntiAlias(true);
        textPaint2.setTextSize((float) 60.0);
        textPaint2.setTextAlign(Paint.Align.CENTER);
        
        astarPath=new Path();
	}
	
	private void initMapBean(){
		
		if (MapBuilder.mapBean!=null) {
			
			Resources resources = this.getResources();
			DisplayMetrics dm = resources.getDisplayMetrics();
			screenWidth = dm.widthPixels;
			screenHeight = dm.heightPixels;
			
			displayScale=(float)screenWidth * (float) MapBuilder.mapBean.getUnknowScale() / (float)(MapBuilder.mapBean.getmWidth());
			locationErrorAllowdRadiu=MapBuilder.mapBean.getLocationErrorAllowed()*100/MapBuilder.SCALETOREAL/displayScale;
			stoneAreas=MapBuilder.mapBean.getStoneList();
			aimAreas=MapBuilder.mapBean.getAimList();
		}
	}
	
	public void setPathSearcher(PathSearcher pathSearcher){
		
		this.pathSearcher=pathSearcher;
		positionWhatcher=new PositionWhatcher(handler);
		
        pathSearcher.setOnPathSearchListener(new OnPathSearchListener() {
			
			@Override
			public void onStartAndEndPrepared(boolean ifClearPath, float sx, float sy, float ex, float ey) {
				// TODO Auto-generated method stub
				clearPath=ifClearPath;
				currentPosX=sx;
				currentPosY=sy;
				endX=ex;
				endY=ey;
				handler.sendEmptyMessage(READY_TO_SEARCH);
			}

			@Override
			public void onSearchingPathComplate(List<int[]> pathList) {
				// TODO Auto-generated method stub
				clearPath=false;
				mPathList=pathList;
				handler.sendEmptyMessage(SEARCH_SUCCESS);
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
				handler.sendEmptyMessage(JUST_REFRESH_VIEW);
				if (newPathList.size()<1) {
					LOCK_VIEW=false;
				}
			}
		});
		
//		pathSearcherThread=new Thread(pathSearcher);
//		postionWatcherThread=new Thread(positionWhatcher);
	}
	
	public void setCurrentPos(float[] pos){
		currentPosX=pos[0]/MapBuilder.SCALETOREAL;
		currentPosY=pos[1]/MapBuilder.SCALETOREAL;
	}
	
	public float[] getCurrentPos(){
		float[] pos=new float[2];
		pos[0]=currentPosX*MapBuilder.SCALETOREAL;
		pos[1]=currentPosY*MapBuilder.SCALETOREAL;
		return pos;
	}
	
	public void setStonesVisiable(boolean ifShow){
		showStonesAllowed=ifShow;
	}
	
	public void setPosErrVisiable(boolean ifShow){
		showPosErrAllowed=ifShow;
	}
	
	private float[] fixXY(float x, float y){
		float[] fixedXY=new float[2];
		fixedXY[0] = matrixValues[2]+x*MapBuilder.SCALETOREAL*displayScale*matrixValues[0];
		fixedXY[1] = matrixValues[5]+y*MapBuilder.SCALETOREAL*displayScale*matrixValues[4];
		return fixedXY;
	}
	
	private void drawPointOnMap(Canvas canvas,
			float x, float y, Paint posPaint,
			boolean showlable, String lable, Paint textPaint,
			boolean showPosErrAllowed, Paint errAllowedPaint){
		
		if (x > -1 &&y > -1) {
			float[] fs=fixXY(x, y);
			canvas.drawCircle(fs[0], fs[1], pointRadiu, posPaint);
			
			if (showlable) {
				canvas.drawText(lable, fs[0], fs[1], textPaint);
			}
			
			if (showPosErrAllowed) {
				canvas.drawCircle(fs[0], fs[1], locationErrorAllowdRadiu, errAllowedPaint);
			}
		}
	}
	
	private void drawStartPosOnMap(Canvas canvas){
		drawPointOnMap(canvas, currentPosX, currentPosY, posPaint,
				true, "start", textPaint,
				showPosErrAllowed, posPaint2);
	}
	
	private void drawEndPosOnMap(Canvas canvas){
		drawPointOnMap(canvas, endX, endY, posPaint,
				true, "end", textPaint,
				false, null);
	}
	
	private void drawPathOnMap(Canvas canvas){
		if (!astarPath.isEmpty()) {
			astarPath.reset();
		}
	
		if (mPathList!=null&&mPathList.size()>0) {
			float mfs[]=fixXY(mPathList.get(0)[0], mPathList.get(0)[1]);
			astarPath.moveTo(mfs[0], mfs[1]);
			
			float[] lfs;
			for(int i=1;i<mPathList.size();i++){
				lfs=fixXY(mPathList.get(i)[0], mPathList.get(i)[1]);
				astarPath.lineTo(lfs[0], lfs[1]);
			}
			canvas.drawPath(astarPath, pathPaint);
		}	
	} 
	
	private void clearPathFormMap(Canvas canvas){
		astarPath.reset();
		canvas.drawPath(astarPath, pathPaint);
	}
	
    private void drawAimPointsOnMap(final Canvas canvas){
    	
    	if (aimAreas!=null) {
    		
    		float cx, cy;
        	AimArea aimArea;

        	for(int i=0;i<aimAreas.size();i++){
				aimArea=aimAreas.get(i);
				cx=aimArea.getPointX()/MapBuilder.SCALETOREAL;
				cy=aimArea.getPointY()/MapBuilder.SCALETOREAL;
				
				drawPointOnMap(canvas, cx, cy, aimPaint,
						false, null, null,
						false, null);
			}
		}
	}
	
	private void drawStonesOnMap(final Canvas canvas){
		
		if (stoneAreas!=null) {
			
			float[] sfs, efs;
			StoneArea stoneArea;
			
			for(int i=0;i<stoneAreas.size();i++){
				stoneArea=stoneAreas.get(i);
				sfs = fixXY(stoneArea.getStartX()/MapBuilder.SCALETOREAL,
						stoneArea.getStartY()/MapBuilder.SCALETOREAL);
				efs = fixXY(stoneArea.getEndX()/MapBuilder.SCALETOREAL,
						stoneArea.getEndY()/MapBuilder.SCALETOREAL);
				
				canvas.drawRect(sfs[0], sfs[1], efs[0], efs[1], stonePaint);
				canvas.drawText(stoneArea.getAreaName(), (sfs[0] + efs[0]) /2, (sfs[1] + efs[1]) /2, textPaint2);
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		matrixValues=new float[9];
		mMatrix.getValues(matrixValues);
		
		if (showStonesAllowed) {
			drawStonesOnMap(canvas);
		}
		
		drawAimPointsOnMap(canvas);
		
		drawStartPosOnMap(canvas);
		drawEndPosOnMap(canvas);
		
		if(clearPath){
			clearPathFormMap(canvas);
		}else {
			drawPathOnMap(canvas);
		}
		
	}

	public void setOnPointClickListener(OnPointClickListener onPointClickListener) {
		this.onPointClickListener = onPointClickListener;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//return super.onTouch(v, event);
		super.onTouch(v, event);

		switch (event.getAction()){
			case MotionEvent.ACTION_DOWN:

				float x=event.getX();
				float y=event.getY();
				
//				if (!(pathSearcherThread.isAlive()||postionWatcherThread.isAlive())) {
				if (!LOCK_VIEW) {
					if (aimAreas!=null) {
						float[] afs;
						AimArea aimArea;
						for(int i=0;i<aimAreas.size();i++){
							aimArea=aimAreas.get(i);
							afs=fixXY(aimArea.getPointX()/MapBuilder.SCALETOREAL,
									aimArea.getPointY()/MapBuilder.SCALETOREAL);
							double distance=Math.sqrt((Math.pow((x-afs[0]), 2)+Math.pow((y-afs[1]),2)));
							if (distance < (pointRadiu+10)) {				
								onPointClickListener.onClick(aimAreas.get(i).getPointX(), aimAreas.get(i).getPointY());
								break;
							}
						}
//					}
					}
				}
				break;
		}
		return true;

	}

	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int defaultWidthOrHeight=Math.min(screenWidth, screenHeight);
        int widthSpecMode= MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize= MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode=MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize=MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode==MeasureSpec.AT_MOST&&heightSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(defaultWidthOrHeight,defaultWidthOrHeight);
        }else if (widthSpecMode==MeasureSpec.AT_MOST){
            setMeasuredDimension(defaultWidthOrHeight,heightSpecSize);
        }else if (widthMeasureSpec==MeasureSpec.AT_MOST){
            setMeasuredDimension(widthSpecSize,defaultWidthOrHeight);
        }
    }
}
