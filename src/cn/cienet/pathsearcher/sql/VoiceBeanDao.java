package cn.cienet.pathsearcher.sql;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import cn.cienet.pathsearcher.bean.VoiceBean;

public class VoiceBeanDao {
	
	@SuppressLint("DefaultLocale")
	public List<VoiceBean> getVoiceBeans(Context context){
		DBManager manager=DBManager.build();
		SQLiteDatabase db=manager.openDatabase(context);	
		
		try{
			List<VoiceBean> voiceBeans=new ArrayList<VoiceBean>();
			Cursor c=db.rawQuery("SELECT * FROM tts_voice where enable=?",new String[]{"0"});
			while (c.moveToNext()) {
				VoiceBean voiceBean=new VoiceBean();
				voiceBean.setVoiceId(c.getInt(c.getColumnIndex("voice_id")));
	            voiceBean.setVoiceName((c.getString(c.getColumnIndex("voice_name")).toUpperCase()));
	            voiceBean.setVoiceDetail(c.getString(c.getColumnIndex("voice_detail")));
	            
	            voiceBeans.add(voiceBean);
			}
			c.close();
			
			return voiceBeans;
		}catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return null; 	
	}
}
