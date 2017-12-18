package cn.cienet.astarandroid;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import cn.cienet.pathsearcher.astar.MapBuilder;
import cn.cienet.pathsearcher.interfaces.OnDelFileListener;
import cn.cienet.pathsearcher.interfaces.OnPointClickListener;
import cn.cienet.pathsearcher.sql.DBManager;
import cn.cienet.pathsearcher.weight.PSMapView;
import cn.cienet.pathsearcher.weight.PathSearcher;

public class MainActivity extends BaseActivity {
	
	private PSMapView psMapView;
	
	private static final String TAG="MainActivity";
	private PathSearcher pathSearcher;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActionBar(TAG);
		setContentView(R.layout.activity_main);
		
		psMapView=(PSMapView) findViewById(R.id.main_mapView);
				
		pathSearcher=new PathSearcher();
		psMapView.setPathSearcher(pathSearcher);
		//初始化起点
//		final float[] currentPos=new float[2];
//		currentPos[0]=MapBuilder.mapBean.getAimList().get(0).getPointX();
//		currentPos[1]=MapBuilder.mapBean.getAimList().get(0).getPointY();
//		psMapView.setCurrentPos(currentPos);
//		psMapView.setPosErrVisiable(true);
//		psMapView.setStonesVisiable(true);
		
		psMapView.setOnPointClickListener(new OnPointClickListener() {
			
			@Override
			public void onClick(float x, float y) {
				// TODO Auto-generated method stub
				pathSearcher.setStartAndEnd(true,
						psMapView.getCurrentPos()[0],
						psMapView.getCurrentPos()[1],
						x, y);
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
			psMapView.reloadMapById(1, R.drawable.map);
			break;
		case R.id.map2:
			psMapView.reloadMapById(2, R.drawable.map2);	
			break;
		case R.id.stone_on:
			psMapView.setStonesVisiable(true);
			break;
		case R.id.stone_off:
			psMapView.setStonesVisiable(false);
			break;
		case R.id.err_on:
			psMapView.setPosErrVisiable(true);
			break;
		case R.id.err_off:
			psMapView.setPosErrVisiable(false);
			break;
		case R.id.del_map:
			MapBuilder.build().delMapFile(new OnDelFileListener() {
				
				@Override
				public void onDelReuslt(boolean result, String msg) {
					// TODO Auto-generated method stub
					showToast(msg);
				}
			});
			break;
		case R.id.del_db:
			DBManager.build().deleteDbFile(new OnDelFileListener() {
				
				@Override
				public void onDelReuslt(boolean result, String msg) {
					// TODO Auto-generated method stub
					showToast(msg);
				}
			});
			break;
		case R.id.reset_mapInfo:
			startAct(EditMapActivity.class);
			break;
		default:
			break;
		}
	
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		psMapView.release();
	}
	
}
