package cn.cienet.pathsearcher.interfaces;

import java.util.List;

public interface OnPositionWatchListener {

	void onPositionChanged(int x, int y,List<int[]> newPathlist);
}
