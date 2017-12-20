package cn.cienet.astarandroid;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import cn.cienet.pathsearcher.astar.MapBuilder;
import cn.cienet.pathsearcher.interfaces.OnDelFileListener;
import cn.cienet.pathsearcher.interfaces.OnPointClickListener;
import cn.cienet.pathsearcher.sql.DBManager;
import cn.cienet.pathsearcher.utils.TTSConstants;
import cn.cienet.pathsearcher.weight.PSTMapView;
import cn.cienet.pathsearcher.weight.PathSearcher;

public class MainActivity extends BaseActivity {
	
	private PSTMapView pstMapView;
	
	//private static final String TAG="MainActivity";
	private PathSearcher pathSearcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActionBar(getResources().getString(R.string.app_name));
		setContentView(R.layout.activity_main);
		
		pstMapView=(PSTMapView) findViewById(R.id.main_mapView);
				
		pathSearcher=new PathSearcher();
		pstMapView.setPathSearcher(pathSearcher);
		//初始化起点
//		final float[] currentPos=new float[2];
//		currentPos[0]=MapBuilder.mapBean.getAimList().get(0).getPointX();
//		currentPos[1]=MapBuilder.mapBean.getAimList().get(0).getPointY();
//		pstMapView.setCurrentPos(currentPos);
//		psMapView.setPosErrVisiable(true);
//		psMapView.setStonesVisiable(true);
		pstMapView.setTTSConstants(TTSConstants.appId, TTSConstants.apiKey, TTSConstants.secretKey);
				
		pstMapView.setOnPointClickListener(new OnPointClickListener() {
			
			@Override
			public void onClick(float x, float y) {
				// TODO Auto-generated method stub
				pathSearcher.setStartAndEnd(pstMapView.getCurrentPos()[0], pstMapView.getCurrentPos()[1], x, y);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {
		case R.id.map1:
			pstMapView.reloadMapById(1, R.drawable.map);
			break;
		case R.id.map2:
			pstMapView.reloadMapById(2, R.drawable.map2);
			break;

		case R.id.stone_on:
			pstMapView.setStonesVisiable(true);
			break;
		case R.id.stone_off:
			pstMapView.setStonesVisiable(false);
			break;
		case R.id.err_on:
			pstMapView.setPosErrVisiable(true);
			break;
		case R.id.err_off:
			pstMapView.setPosErrVisiable(false);
			break;
		case R.id.del_map:
			MapBuilder.build().delMapFile(new OnDelFileListener() {
				
			@Override
			public void onDelResult(boolean result, String msg) {
					// TODO Auto-generated method stub
					showToast(msg);
				}
			});
			break;
		case R.id.del_db:
			DBManager.build().deleteDbFile(new OnDelFileListener() {
				
				@Override
				public void onDelResult(boolean result, String msg) {
					// TODO Auto-generated method stub
					showToast(msg);
			}
			});
			break;
		case R.id.reset_mapInfo:
//			startAct(EditMapActivity.class);
			break;
			
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		pstMapView.release();
		super.onDestroy();
	}
	
}
