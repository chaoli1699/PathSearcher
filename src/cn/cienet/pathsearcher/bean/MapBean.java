package cn.cienet.pathsearcher.bean;

import java.util.List;

public class MapBean {

	private int mapId;
	private String mapName;
	private int mWidth;
	private int mHeight;
	private String mapDesc;
	private double unknowScale;
	private float locationErrorAllowed;
	private List<StoneArea> stoneList;
	private List<AimArea> aimList;
	
	
	public int getMapId() {
		return mapId;
	}



	public void setMapId(int mapId) {
		this.mapId = mapId;
	}



	public String getMapName() {
		return mapName;
	}



	public void setMapName(String mapName) {
		this.mapName = mapName;
	}



	public int getmWidth() {
		return mWidth;
	}



	public void setmWidth(int mWidth) {
		this.mWidth = mWidth;
	}



	public int getmHeight() {
		return mHeight;
	}



	public void setmHeight(int mHeight) {
		this.mHeight = mHeight;
	}



	public String getMapDesc() {
		return mapDesc;
	}



	public void setMapDesc(String mapDesc) {
		this.mapDesc = mapDesc;
	}


	public double getUnknowScale() {
		return unknowScale;
	}

	public void setUnknowScale(double unknowScale) {
		this.unknowScale = unknowScale;
	}
	
	
	


	public float getLocationErrorAllowed() {
		return locationErrorAllowed;
	}



	public void setLocationErrorAllowed(float locationErrorAllowed) {
		this.locationErrorAllowed = locationErrorAllowed;
	}



	public List<StoneArea> getStoneList() {
		return stoneList;
	}



	public void setStoneList(List<StoneArea> stoneList) {
		this.stoneList = stoneList;
	}



	public List<AimArea> getAimList() {
		return aimList;
	}



	public void setAimList(List<AimArea> aimList) {
		this.aimList = aimList;
	}
	
	
	

}
