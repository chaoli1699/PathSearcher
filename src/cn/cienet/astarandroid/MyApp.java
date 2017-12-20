package cn.cienet.astarandroid;

import android.app.Application;
import cn.cienet.pathsearcher.astar.MapBuilder;

public class MyApp extends Application{
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		MapBuilder.build().initMap(getApplicationContext());
	}
}
