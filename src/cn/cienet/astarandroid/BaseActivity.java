package cn.cienet.astarandroid;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

public class BaseActivity  extends Activity{
	
	protected void initActionBar(String title){
		initActionAar(title, false, false);
	}
	
	protected void initActionAar(String title, boolean logoEnabled, boolean homeBtnEnabled){
		ActionBar actionBar=this.getActionBar();
		if(title!=null){
			actionBar.setTitle(title);
		}
		actionBar.setDisplayUseLogoEnabled(logoEnabled);
		
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(homeBtnEnabled);
		actionBar.setHomeButtonEnabled(homeBtnEnabled);
	}

	protected void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	protected void startAct(Class activityClass){
		startActivity(new Intent(this, activityClass));
	}
}
