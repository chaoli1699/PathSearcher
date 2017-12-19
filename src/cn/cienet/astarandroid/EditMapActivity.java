package cn.cienet.astarandroid;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class EditMapActivity extends BaseActivity{
	
	private static final String TAG="EditMapActivity";
	private EditText unknowScale;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initActionAar("EditMap", false, true);
		setContentView(R.layout.activity_editmap);
		
		initView();	
	}
	
	private void initView(){
		unknowScale=(EditText) findViewById(R.id.editmap_unknowScale);	
	}
	
	public void Display(View view){
		showToast("Display");
	}
	
	public void Applay(View view){
		showToast("Applay");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
