package cn.cienet.pathsearcher.weight;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;

public class ScaleImageView extends ImageView implements OnGlobalLayoutListener, OnScaleGestureListener, OnTouchListener{

	private boolean mOnce = false ;

	private float mInitScale ;
	private float mMidScale ;
	private float mMaxScale ;
	protected Matrix mMatrix ;
	private ScaleGestureDetector mScaleGestureDetector ;//�����û���ָ����ʱ���ŵı���

	//---------------------�����ƶ��ı���------------------------
	/**
	 * ��¼�ϴζ�㴥�ص�����
	 */
	private int mLastPointerCount ;
	//��¼�ϴ����ĵ������
	private float mLastPointerX ;
	private float mLastPointerY ;

	/**
	 * ϵͳ��������С��������
	 */
	private float mTouchSlop ;

	private boolean isCanDrag;

	private boolean isCheckLeftAndRight ;
	private boolean isCheckTopAndBottom ;

	//---------------------˫���Ŵ�����С����-------------------------
	private GestureDetector mGestureDetector ;//�û�˫�����ƵĶ������
	protected boolean isScaling ;//�Ƿ����ڷŴ����С---��ֹ�û������ڷŴ����Сʱ�����

	private class SlowlyScaleRunnable implements Runnable{
		//���ŵ�Ŀ��ֵ
		private float mTargetScale ;
		//���ŵ����ĵ�
		private float x ;
		private float y ;

		//�Ŵ�����С���ݶ�
		private final float BEGGER = 1.07F ;
		private final float SMALL = 0.97F ;

		private float tmpScale ;
		public SlowlyScaleRunnable(float mTargetScale, float x, float y) {
			this.mTargetScale = mTargetScale;
			this.x = x;
			this.y = y;
			if(getScale()<mTargetScale){
				tmpScale = BEGGER ;
			}
			if (getScale()>mTargetScale) {
				tmpScale = SMALL ;
			}
		}

		@Override
		public void run() {
			//��������
			mMatrix.postScale(tmpScale, tmpScale, x, y);
			checkBorderAndCenterWhenScale();
			setImageMatrix(mMatrix);
			float currentScale = getScale();
			if((tmpScale>1.0f&&currentScale<mTargetScale)||(tmpScale<1.0f&&currentScale>mTargetScale)){
				postDelayed(this, 16);
			}else{
				isScaling = false ;
				//������Ŀ��ֵ
				float scale = mTargetScale/currentScale ;
				mMatrix.postScale(scale, scale, x, y);
				checkBorderAndCenterWhenScale();
				setImageMatrix(mMatrix);

			}
		}

	}

	public ScaleImageView(Context context) {
		this(context, null);
	}

	public ScaleImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressLint("ClickableViewAccessibility")
	public ScaleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// ��ʼ��
		mMatrix = new Matrix() ;
		setScaleType(ScaleType.MATRIX);

		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		setOnTouchListener(this);
		//ϵͳ��������С��������
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop() ;

		//˫���Ŵ�����С
		mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
			@Override
			public boolean onDoubleTap(MotionEvent e) {
				if(isScaling){
					return true ;
				}
				//�Դ˵�Ϊ��������
				float x = e.getX();
				float y = e.getY();

				if(getScale()<mMidScale){
					postDelayed(new SlowlyScaleRunnable(mMidScale, x, y), 16);
					isScaling = true ;
//					mMatrix.postScale(mMidScale/getScale(), mMidScale/getScale(), x, y);
//					checkBorderAndCenterWhenScale();
//					setImageMatrix(mMatrix);
				}else{
					postDelayed(new SlowlyScaleRunnable(mInitScale, x, y), 16);
					isScaling = true ;
//					mMatrix.postScale(mInitScale/getScale(), mInitScale/getScale(), x, y);
//					checkBorderAndCenterWhenScale();
//					setImageMatrix(mMatrix);
				}

				return true;
			}
		});
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		//ע��onGlobalLayoutListener
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		//�Ƴ�onGlobalLayoutListener
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	/**
	 * ����ͼƬ��������¼� onMeasure ��onDraw�����ʺ�
	 */
	@Override
	public void onGlobalLayout() {
		//��ʼ���Ĳ��� һ�ξͺ�  Ϊ�˱�֤������ֻ����һ��
		if(!mOnce){

			//�õ��ؼ��Ŀ�͸�--��һ������Ļ�Ŀ�͸� ���ܻ���actionBar�ȵ�
			int width = getWidth() ;
			int height = getHeight();

			//�õ����ǵ�ͼƬ �Լ���͸�
			Drawable drawable = getDrawable();
			if(drawable == null){
				return ;
			}
			/**
			 * ����˵��Drawable��������࣬����ʵ����ΪBitmapDrawable
			 * BitmapDrawable�������д��getIntrinsicWidth()��getIntrinsicHeight()����
			 * �������������������˼��֪����ʲô�ˣ����ǵõ�ͼƬ���еĿ�͸ߵ�
			 */
			int intrinsicWidth = drawable.getIntrinsicWidth();
			int intrinsicHeight = drawable.getIntrinsicHeight();
			Log.e("SCALE_IMAGEVIEW", intrinsicWidth+":intrinsicWidth");
			Log.e("SCALE_IMAGEVIEW", intrinsicHeight+":intrinsicHeight");
			// ���ͼƬ��ȱȿؼ����С  �߶ȱȿؼ��� ��Ҫ��С
			float scale = 1.0f ;
			if(width>intrinsicWidth && height<intrinsicHeight){
				scale = height*1.0f/intrinsicHeight ;
			}
			// ���ͼƬ�ȿؼ��� ��Ҫ��С
			if(width<intrinsicWidth && height>intrinsicHeight){
				scale = width*1.0f/intrinsicWidth ;
			}

			if((width<intrinsicWidth && height<intrinsicHeight) || (width>intrinsicWidth&&height>intrinsicHeight)){
				scale = Math.min(width*1.0f/intrinsicWidth, height*1.0f/intrinsicHeight);
			}

			/**
			 * �õ���ʼ�����ŵı���
			 */
			mInitScale = scale * 1.9f;
			mMidScale = 2*mInitScale ;//˫���Ŵ��ֵ
			mMaxScale = 4*mInitScale ;//�Ŵ�����ֵ

			//��ͼƬ�ƶ����ؼ�������
			int dx = width/2 - intrinsicWidth/2 ;
			int dy = height/2 - intrinsicHeight/2 ;
			//��һЩ�������õ�ͼƬ��ؼ��� ����ƽ������ ��ת
			mMatrix.postTranslate(dx, dy);
			mMatrix.postScale(mInitScale, mInitScale, 0, 0);//�Կؼ������Ľ�������
			setImageMatrix(mMatrix);

			mOnce = true ;
		}
	}

	/**
	 * ��ȡͼƬ��ǰ������ֵ
	 * @return
	 */
	public float getScale(){
		float[] values = new float[9];
		mMatrix.getValues(values);
		return values[Matrix.MSCALE_X];
	}

	//�������� initScale --- maxScale
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float scale = getScale() ;
		//�����û���ָ����ʱ���ŵı���
		float scaleFactor = detector.getScaleFactor();
		Log.e("ScaleGestrueDetector", "scaleFactor:"+scaleFactor);
		if(getDrawable()==null){
			return true;
		}
		//�����С����
		if((scale<mMaxScale&&scaleFactor>1.0f)||(scale>mInitScale&&scaleFactor<1.0f)){
			if(scale*scaleFactor > mMaxScale){
				scaleFactor = mMaxScale/scale ;
			}
			if(scale*scaleFactor < mInitScale){
				scaleFactor = mInitScale/scale ;
			}

			mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
			//���ϼ�� ���ưױߺ�����λ��
			checkBorderAndCenterWhenScale();
			setImageMatrix(mMatrix);
		}

		return true;
	}

	/**
	 * ���ͼƬ�Ŵ����С֮��Ŀ�͸� �Լ� left top right bottom�������
	 * @return
	 */
	private RectF getMatrixRectF(){
		Matrix matrix = mMatrix ;
		RectF rect = new RectF();
		Drawable drawable = getDrawable();
		if(null!=drawable){
			rect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			matrix.mapRect(rect);
		}
		return rect ;
	}

	/**
	 * �����ŵ�ʱ����б߽�����Լ����ǵ�����λ�ÿ���
	 */
	private void checkBorderAndCenterWhenScale() {
		RectF rect = getMatrixRectF();
		float delatX =  0 ;
		float delatY = 0 ;
		//�ؼ��Ŀ�͸�
		int width = getWidth() ;
		int height = getHeight();
//		Log.i("top", "top:"+rect.top);
//		Log.i("left", "left:"+rect.left);
//		Log.i("right", "right:"+rect.right);
//		Log.i("bottom", "bottom:"+rect.bottom);

		//���ͼƬ�Ŀ�͸ߴ��ڿؼ��Ŀ�͸� �����Ź����л����border ����ƫ�Ʋ���
		if(rect.width() >= width){
			if(rect.left>0){
				delatX = -rect.left;
			}
			if(rect.right<width){
				delatX = width-rect.right ;
			}
		}

		if(rect.height() >= height){
			if(rect.top>0){
				delatY = -rect.top ;
			}
			if(rect.bottom<height){
				delatY = height - rect.bottom ;
			}
		}

		//���ͼƬ�Ŀ�͸�С�ڿؼ��Ŀ�͸� �������
		if(rect.width()<width){
			delatX = width/2 - rect.right + rect.width()/2f ;
		}
		if(rect.height()<height){
			delatY = height/2 - rect.bottom + rect.height()/2f ;
		}
		mMatrix.postTranslate(delatX, delatY);
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		Log.e("ScaleGestrueDetector", "onScaleBegin");
		return true;//�޸�Ϊtrue �Ż����onScale()�������  �����ָ����һֱ��onScaleBegin���� ���� onScale�� onScaleEnd����
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		Log.e("ScaleGestrueDetector", "onScaleEnd");
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//˫���Ŵ�����С�¼����ݸ�GestureDetector ������ǰ�� ��ֹ˫��ʱ���ܲ����ƶ����¼���Ӧ
		if(mGestureDetector.onTouchEvent(event)){
			return true ;
		}

		//�����ƴ��ݸ�ScaleGestureDetector
		boolean onTouchEvent = mScaleGestureDetector.onTouchEvent(event);

		//-------------------------���Ŵ��ͼƬ�����ƶ��߼�����-----------------start------------
		//�õ��������ĵ������
		float pointerX = 0 ;
		float pointerY = 0 ;
		//�õ���㴥�ص�����
		int pointerCount = event.getPointerCount() ;
		//Log.i("pointerCount", "pointerCount:"+pointerCount);
		for (int i = 0; i < pointerCount; i++) {
			pointerX +=event.getX(i);
			pointerY +=event.getY(i);
		}
		pointerX /=pointerCount ;
		pointerY /=pointerCount ;
		if (mLastPointerCount!=pointerCount) {
			//��ָ�����ı�ʱ ��Ҫ�����ж� �Ƿ��ܹ��ƶ�
			isCanDrag = false ;
			mLastPointerX = pointerX ;
			mLastPointerY = pointerY ;
		}
		mLastPointerCount = pointerCount ;
		RectF rectF = getMatrixRectF();
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			/*if(getParent() instanceof ViewPager){
				//���ͼƬ�Ŵ�ʱ ����ͼƬƽ����ViewPager�Ļ�����ͻ
				if(rectF.width()-getWidth()>0.01||rectF.height()-getHeight()>0.01){
					getParent().requestDisallowInterceptTouchEvent(true);
				}
			}*/
				break ;
			case MotionEvent.ACTION_MOVE:
				//���ͼƬ�Ŵ�ʱ ����ͼƬƽ����ViewPager�Ļ�����ͻ
			/*if(getParent() instanceof ViewPager){
				if(rectF.width()-getWidth()>0.01||rectF.height()-getHeight()>0.01){
					getParent().requestDisallowInterceptTouchEvent(true);
				}
			}*/
				move2Point(pointerX, pointerY, mLastPointerX, mLastPointerY, rectF);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mLastPointerCount = 0 ;
				break ;
		}
		//-------------------------���Ŵ��ͼƬ�����ƶ��߼�����-------------------end----------
		return true;
	}
	
	protected void move2Point(float cx, float cy, float lx, float ly, RectF rectF){
		float dx = cx - lx ;
		float dy = cy - ly ;
		if (!isCanDrag) {
			isCanDrag = isMoveAction(dx, dy);
		}
		
		if(isCanDrag){
			if(getDrawable()!=null){
				isCheckLeftAndRight = isCheckTopAndBottom = true ;
				//���ͼƬ���С�ڿؼ���� ����������ƶ�
				if (rectF!=null) {
					if(rectF.width()<getWidth()){
						isCheckLeftAndRight = false ;
						dx = 0 ;
					}
					//���ͼƬ�ĸ߶�С�ڿؼ��ĸ߶� �����������ƶ�
					if(rectF.height()<getHeight()){
						isCheckTopAndBottom  = false ;
						dy = 0 ;
					}
				}
				mMatrix.postTranslate(dx, dy);
				checkBorderWhenTranslate();
				setImageMatrix(mMatrix);
			}
		} 
		mLastPointerX = cx ;
		mLastPointerY = cy ;
	}

	/**
	 * ���ƶ�ʱ ���б߽���
	 */
	private void checkBorderWhenTranslate() {

		RectF rect = getMatrixRectF() ;
		float deltaX = 0 ;
		float deltaY = 0 ;

		int width = getWidth();
		int height = getHeight();

		if(rect.top>0 && isCheckTopAndBottom){
			deltaY = -rect.top ;
		}
		if(rect.bottom <height && isCheckTopAndBottom){
			deltaY = height - rect.bottom ;
		}

		if(rect.left>0 && isCheckLeftAndRight){
			deltaX = -rect.left ;
		}
		if(rect.right<width && isCheckLeftAndRight){
			deltaX = width - rect.right ;
		}

		mMatrix.postTranslate(deltaX, deltaY);

	}

	/**
	 * �жϻ����ľ����Ƿ񴥷��������ٽ�����
	 * @param dx
	 * @param dy
	 * @return
	 */
	private boolean isMoveAction(float dx, float dy) {
		return Math.sqrt(dx*dx+dy*dy)>mTouchSlop;
	}
}
