package cn.cienet.pathsearcher.sql;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import cn.cienet.pathsearcher.bean.AimArea;
import cn.cienet.pathsearcher.bean.MapBean;
import cn.cienet.pathsearcher.bean.StoneArea;

public class MapBeanDao {
	
	public MapBean getMapBeanById(Context context,String mapId){
		DBManager manager=DBManager.build();
		SQLiteDatabase db=manager.openDatabase(context);
		
		try{
			MapBean mapBean=new MapBean();
			Cursor c=db.rawQuery("SELECT * FROM map_info where map_id=?", new String[]{mapId});
			
			while(c.moveToNext()){
				mapBean.setMapId(c.getInt(c.getColumnIndex("map_id")));
				mapBean.setMapName(c.getString(c.getColumnIndex("map_name")));
				mapBean.setmWidth(c.getInt(c.getColumnIndex("width")));
				mapBean.setmHeight(c.getInt(c.getColumnIndex("height")));
				mapBean.setMapDesc(c.getString(c.getColumnIndex("map_describe")));
				mapBean.setUnknowScale(c.getDouble(c.getColumnIndex("unknow_scale")));
				mapBean.setLocationErrorAllowed(c.getFloat(c.getColumnIndex("location_error_allowed")));
				if (getStoneListByMapId(db, mapId)!=null) {
					mapBean.setStoneList(getStoneListByMapId(db,mapId));
				}
				if (getAimListByMapId(db, mapId)!=null) {
					mapBean.setAimList(getAimListByMapId(db, mapId));
				}
			}
			c.close();
			
			return mapBean;
		}
		catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return null;	
	}
	
	private List<StoneArea> getStoneListByMapId(SQLiteDatabase db,String mapId){
		
		try{
			@SuppressWarnings({ "rawtypes", "unchecked" })
			List<StoneArea> stoneAreas=new ArrayList();
			
			Cursor c=db.rawQuery("SELECT * FROM stone_area where map_id=?", new String[]{mapId});
			while(c.moveToNext()){
				StoneArea stoneArea=new StoneArea();
				stoneArea.setAreaId(c.getInt(c.getColumnIndex("area_id")));
				stoneArea.setAreaName(c.getString(c.getColumnIndex("area_name")));
				stoneArea.setStartX(c.getInt(c.getColumnIndex("start_x")));
				stoneArea.setStartY(c.getInt(c.getColumnIndex("start_y")));
				stoneArea.setEndX(c.getInt(c.getColumnIndex("end_x")));
				stoneArea.setEndY(c.getInt(c.getColumnIndex("end_y")));
				stoneArea.setAreaDesc(c.getString(c.getColumnIndex("area_describe")));
				stoneAreas.add(stoneArea);
			}
			c.close();
			
			return stoneAreas;
		}
		catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return null;
	}
	
	private List<AimArea> getAimListByMapId(SQLiteDatabase db,String mapId){
		
		try {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			List<AimArea> aimAreas =new ArrayList();
			
			Cursor c=db.rawQuery("SELECT * FROM aim_area where map_id=?", new String[]{mapId});
			while(c.moveToNext()){
				AimArea aimArea=new AimArea();
				aimArea.setAimId(c.getInt(c.getColumnIndex("aim_id")));
				aimArea.setAimName(c.getString(c.getColumnIndex("aim_name")));
				aimArea.setPointX(c.getInt(c.getColumnIndex("point_x")));
				aimArea.setPointY(c.getInt(c.getColumnIndex("point_y")));
				aimArea.setAimDescribe(c.getString(c.getColumnIndex("aim_describe")));
				aimAreas.add(aimArea);
			}
			c.close();
			
			return aimAreas;
		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return null;
	}

}
