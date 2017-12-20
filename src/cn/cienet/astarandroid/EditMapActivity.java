package cn.cienet.astarandroid;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EditMapActivity extends BaseActivity {

	private static final String TAG="EditMapActivity";
	private EditText mapId;
	private EditText mapName;
	private EditText unknowScale;
	private EditText errAllowedRadiu;
	
	private Button ok;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		initActionAar(TAG, false, true);
		setContentView(R.layout.activity_editmap);
		
		initView();
		
	}
	
	private void initView(){
		mapId=(EditText) findViewById(R.id.editmap_mapId);
		mapName=(EditText) findViewById(R.id.editmap_mapName);
		unknowScale=(EditText) findViewById(R.id.editmap_unknowScale);
		errAllowedRadiu=(EditText) findViewById(R.id.editmap_errAllowedRadiu);
		
		ok=(Button) findViewById(R.id.editmap_ok);
		
		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String idStr=mapId.getText().toString();
				String nameStr=mapName.getText().toString();
				String unknowscaleStr=unknowScale.getText().toString();
				String errallowedradiuStr=errAllowedRadiu.getText().toString();
				
				finish();	
			}
		});
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
