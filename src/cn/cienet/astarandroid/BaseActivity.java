package cn.cienet.astarandroid;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

public class BaseActivity  extends Activity{

	protected void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	protected void startAct(Class activityClass){
		startActivity(new Intent(this, activityClass));
	}
}
