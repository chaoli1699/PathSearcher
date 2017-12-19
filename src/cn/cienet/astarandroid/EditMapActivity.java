package cn.cienet.astarandroid;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EditMapActivity extends BaseActivity {

	private EditText mapId;
	private EditText mapName;
	private EditText unknowScale;
	private EditText errAllowedRadiu;
	
	private Button ok;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
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
				String mapid=mapId.getText().toString();
				String mapname=mapName.getText().toString();
				String unknowscale=unknowScale.getText().toString();
				String errallowedradiu=errAllowedRadiu.getText().toString();
				
				finish();	
			}
		});
	}
}
