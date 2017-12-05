package cn.cienet.pathsearcher.interfaces;

import java.util.List;

public interface OnPathSearchListener {

	void onStartAndEndPrepared(boolean ifClearPath, float sx, float sy, float ex, float ey);
	void onSearchingPathComplate(List<int[]> pathList);
	void onSearchingPathFail(String msg);
}
