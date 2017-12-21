package cn.cienet.pathsearcher.weight;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import cn.cienet.astarandroid.R;
import cn.cienet.pathsearcher.astar.MapBuilder;
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
	private Resources resources = this.getResources();
	private DisplayMetrics dm = resources.getDisplayMetrics();
	private int screenWidth;
	private int screenHeight;
	/*
	 * 图形矩阵缩放量
	 */
	private float[] matrixValues=new float[9];
	/*
	 * 画笔
	 */
	private Paint currentPosPaint;
	private Paint endPosPaint;
	private Paint errAllowedPaint;
	private Paint aimPaint;
	private Paint stonePaint;
	private Paint pathPaint;
	private Paint pointLablePaint;
	private Paint stonesLablePaint;
	/*
	 * 路线起止点坐标
	 */
	private float currentPosX=-1;
	private float currentPosY=-1;
	private float endPosX=-1;
	private float endPosY=-1;
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
	private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
	private static final int CORE_POOL_SIZE =CPU_COUNT *2;
	private static final int MAXIMUM_POOL_SIZE = CPU_COUNT *2+1;
	private static final long KEEP_ALIVE=10L;
	/*
	 * 寻路线程
	 */
	private PathSearcher pathSearcher;
	/*
	 * 循迹线程
	 */
	private PositionWhatcher positionWhatcher;
	/*
	 * 清除路线开关
	 */
	private boolean CLEAR_PATH=false;
	/*
	 * 显示障碍物开关
	 */
	private boolean SHOW_STONES=false;
	/*
	 * 显示定位容错范围
	 */
	private boolean SHOW_ERR_ALLOWED=false;
	private float locationErrorAllowdRadiu=300;
	/*
	 * 目标地参数
	 */
	private OnPointClickListener onPointClickListener;
	private AimArea aimArea;
	private List<AimArea> aimAreas;
	private List<StoneArea> stoneAreas;
	
	private static final int DEFAULT_RADIU = 20;
	protected static final int JUST_REFRESH_VIEW = 0;
	protected static final int READY_TO_SEARCH = 1;
	protected static final int SEARCH_SUCCESS = 2;
	protected static final int WALK_OUT_OF_PATH = 3;
	
	private boolean LOCK_AIM_POINT=false;
	private AnimatorSet animationSet;
	private ValueAnimator endPointAnimator;
	private static final int END_ANIMATOR_DURATION=1000;
	private int endPointRadiu = DEFAULT_RADIU;
	
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
				LOCK_AIM_POINT=true;
				aimPaint.setColor(Color.LTGRAY);
				startAnimator();
				THREAD_POOL_EXECUTOR.execute(pathSearcher);
				break;
			case SEARCH_SUCCESS:
				//Walk to aim position
				positionWhatcher.setPathList(mPathList);
				THREAD_POOL_EXECUTOR.execute(positionWhatcher);
				break;
			case WALK_OUT_OF_PATH:
				//Walking out of path, warning to host and reset path
				pathSearcher.setStartAndEnd(false, msg.arg1, msg.arg2,
						endPosX*MapBuilder.SCALETOREAL, endPosY*MapBuilder.SCALETOREAL);
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
//			Executors.newSingleThreadExecutor(sThreadFactory);
			new ThreadPoolExecutor(
					CORE_POOL_SIZE,
					MAXIMUM_POOL_SIZE,
					KEEP_ALIVE,
					TimeUnit.SECONDS,
					new LinkedBlockingDeque<Runnable>(),
					sThreadFactory);

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
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		
		initPaint();
		initMapBean();
		initAnimator();
	}
	
	private void initPaint(){
		currentPosPaint=new Paint();
		currentPosPaint.setStyle(Paint.Style.FILL);
		currentPosPaint.setStrokeWidth((float) 4.0);
		currentPosPaint.setColor(Color.BLUE);
		currentPosPaint.setAntiAlias(true);
		
		endPosPaint=new Paint();
		endPosPaint.setStyle(Paint.Style.FILL);
		endPosPaint.setStrokeWidth((float) 4.0);
		endPosPaint.setColor(Color.RED);
		endPosPaint.setAntiAlias(true);
		
		errAllowedPaint=new Paint();
		errAllowedPaint.setStyle(Paint.Style.STROKE);
		errAllowedPaint.setStrokeWidth((float) 4.0);
		errAllowedPaint.setColor(Color.YELLOW);
		errAllowedPaint.setAntiAlias(true);
		
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
		
		pointLablePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        pointLablePaint.setColor(Color.BLACK);
        pointLablePaint.setAntiAlias(true);
        pointLablePaint.setTextSize((float) 40.0);
        pointLablePaint.setTextAlign(Paint.Align.CENTER);
        
        stonesLablePaint=new Paint(Paint.ANTI_ALIAS_FLAG);
        stonesLablePaint.setColor(Color.BLACK);
        stonesLablePaint.setAntiAlias(true);
        stonesLablePaint.setTextSize((float) 60.0);
        stonesLablePaint.setTextAlign(Paint.Align.CENTER);
        
        astarPath=new Path();
	}
	
	private void initMapBean(){
		
		if (MapBuilder.mapBean!=null) {
			
			displayScale=(float)screenWidth * (float) MapBuilder.mapBean.getUnknowScale() / (float)(MapBuilder.mapBean.getmWidth());
			locationErrorAllowdRadiu=MapBuilder.mapBean.getLocationErrorAllowed()*100/MapBuilder.SCALETOREAL/displayScale;
			stoneAreas=MapBuilder.mapBean.getStoneList();
			aimAreas=MapBuilder.mapBean.getAimList();
			aimArea=aimAreas.get(0);
			
			changeMapById(MapBuilder.mapBean.getMapId());
			
			setCurrentPos(aimArea.getPointX(),aimArea.getPointY());
			
	        move2Point(aimArea.getPointX(), aimArea.getPointY(), screenWidth/2, screenHeight/2, null);
		}
	}
	
	private void initAnimator(){
		setEndPointAnimator();
		
		animationSet=new AnimatorSet();
		animationSet.play(endPointAnimator);
	}
	
	private void setEndPointAnimator(){
		
		endPointAnimator=ValueAnimator.ofInt(DEFAULT_RADIU, DEFAULT_RADIU*3);
		endPointAnimator.setDuration(END_ANIMATOR_DURATION);
		endPointAnimator.setRepeatCount(ValueAnimator.INFINITE);
		endPointAnimator.addUpdateListener(new AnimatorUpdateListener() {
			
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO Auto-generated method stub
				endPointRadiu=(Integer) animation.getAnimatedValue();
				int alpha=(int) (255-(endPointRadiu*255)/(DEFAULT_RADIU*3));
				endPosPaint.setAlpha(alpha);
				invalidate();
			}
		});
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void startAnimator(){
		if (!animationSet.isStarted()||animationSet.isPaused()) {
			animationSet.start();
		}
	}
	
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void pauseAnimator(){
		if (animationSet.isRunning()) {
			animationSet.pause();
		}
	}
	
	public void setPathSearcher(PathSearcher pathSearcher){
		
		this.pathSearcher=pathSearcher;
		positionWhatcher=new PositionWhatcher(handler);
		
        pathSearcher.setOnPathSearchListener(new OnPathSearchListener() {
			
			@Override
			public void onStartAndEndPrepared(boolean ifClearPath, float sx, float sy, float ex, float ey) {
				// TODO Auto-generated method stub
				CLEAR_PATH=ifClearPath;
				currentPosX=sx;
				currentPosY=sy;
				endPosX=ex;
				endPosY=ey;
				handler.sendEmptyMessage(READY_TO_SEARCH);
			}

			@Override
			public void onSearchingPathComplate(List<int[]> pathList) {
				// TODO Auto-generated method stub
				CLEAR_PATH=false;
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
				
				if (newPathList.size()<1) {
					LOCK_AIM_POINT=false;
					aimPaint.setColor(Color.RED);
					endPosPaint.setAlpha(0);
					if (animationSet!=null) {
						pauseAnimator();
					}
				}
				handler.sendEmptyMessage(JUST_REFRESH_VIEW);
			}
		});
	}
	
	private void setCurrentPos(float x, float y){
		currentPosX=x/MapBuilder.SCALETOREAL;
		currentPosY=y/MapBuilder.SCALETOREAL;
	}
	
//	public void setCurrentPos(float[] pos){
//		currentPosX=pos[0]/MapBuilder.SCALETOREAL;
//		currentPosY=pos[1]/MapBuilder.SCALETOREAL;
//	}
//	
	public float[] getCurrentPos(){
		float[] pos=new float[2];
		pos[0]=currentPosX*MapBuilder.SCALETOREAL;
		pos[1]=currentPosY*MapBuilder.SCALETOREAL;
		return pos;
	}
	
	public void setStonesVisiable(boolean ifShow){
		SHOW_STONES=ifShow;
		invalidate();
	}
	
	public void setPosErrVisiable(boolean ifShow){
		SHOW_ERR_ALLOWED=ifShow;
		invalidate();
	}
	
	public void release(){
		MapBuilder.build().markCurrentMap(MapBuilder.mapBean.getMapId());
		if (animationSet!=null) {
			pauseAnimator();
			animationSet.cancel();
			animationSet=null;
		}
	}
	
	public void reloadMapById(int mapId, int mapRes){
		if (!LOCK_AIM_POINT&&mapId!=MapBuilder.mapBean.getMapId()) {
			MapBuilder.build().initMap(getContext(), mapId);
			initMapBean();
		}
	}
	
	private void changeMapById(int mapId){
		Bitmap b = null;
		switch (mapId) {
		case 1:
			b=BitmapFactory.decodeResource(getResources(), R.drawable.map);
			break;

		case 2:
			b=BitmapFactory.decodeResource(getResources(), R.drawable.map2);
			break;
		default:
			break;
		}
		setImageBitmap(b);
	}
	
	private float[] fixXY(float x, float y){
		float[] fixedXY=new float[2];
		fixedXY[0] = matrixValues[2]+x*MapBuilder.SCALETOREAL*displayScale*matrixValues[0];
		fixedXY[1] = matrixValues[5]+y*MapBuilder.SCALETOREAL*displayScale*matrixValues[4];
		return fixedXY;
	}
	
	private void drawPointOnMap(Canvas canvas,
			float x, float y, float radiu, Paint posPaint,
			boolean showlable, String lable, Paint textPaint,
			boolean showPosErrAllowed, Paint errAllowedPaint){
		
		if (x > -1 &&y > -1) {
			float[] fs=fixXY(x, y);
			canvas.drawCircle(fs[0], fs[1], radiu, posPaint);
			
			if (showlable) {
				canvas.drawText(lable, fs[0], fs[1], textPaint);
			}
			
			if (showPosErrAllowed) {
				canvas.drawCircle(fs[0], fs[1], locationErrorAllowdRadiu, errAllowedPaint);
			}
		}
	}
	
	private void drawStartPosOnMap(Canvas canvas){
		drawPointOnMap(canvas, currentPosX, currentPosY, DEFAULT_RADIU, currentPosPaint,
				false, "start", pointLablePaint,
				SHOW_ERR_ALLOWED, errAllowedPaint);
	}
	
	private void drawEndPosOnMap(Canvas canvas){
		drawPointOnMap(canvas, endPosX, endPosY, endPointRadiu, endPosPaint,
				false, "end", pointLablePaint,
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
				
				drawPointOnMap(canvas, cx, cy, DEFAULT_RADIU, aimPaint,
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
				canvas.drawText(stoneArea.getAreaName(), (sfs[0] + efs[0]) /2, (sfs[1] + efs[1]) /2, stonesLablePaint);
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		mMatrix.getValues(matrixValues);
		
		if (SHOW_STONES) {
			drawStonesOnMap(canvas);
		}
		
		drawAimPointsOnMap(canvas);
		
		drawStartPosOnMap(canvas);
		drawEndPosOnMap(canvas);
		
		if(CLEAR_PATH){
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
				if (!LOCK_AIM_POINT) {
					if (aimAreas!=null) {
						float[] afs;
						AimArea aimArea;
						for(int i=0;i<aimAreas.size();i++){
							aimArea=aimAreas.get(i);
							afs=fixXY(aimArea.getPointX()/MapBuilder.SCALETOREAL,
									aimArea.getPointY()/MapBuilder.SCALETOREAL);
							double distance=Math.sqrt((Math.pow((x-afs[0]), 2)+Math.pow((y-afs[1]),2)));
							if (distance < (DEFAULT_RADIU+10)) {				
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
