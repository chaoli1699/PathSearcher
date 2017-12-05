# PathSearcher

Pathseatcher 是一款基于A Star 算法的自动寻路library, 基于安卓系统。可广泛应用于室内导航（包括地下停车场， 商场各个楼层，办公室等小范围地图），
以及各类游戏等场景的开发。

本例采用某家庭居室为参考，实现点击预置位置实现导航到目标地点的功能，功能在未来人工智能、智能家居等领域都有很重要的意义。

1. 特性：完全封装，实现不超过20行代码
2. 使用方法：

    1. 下载本例jar、assets文件加下的pathsearcher.jar, astar_android.db到本地，并拷贝到工程。
    2. 按需求修改astar_android.db，详情如下图所示：
	
	
	![map](https://github.com/chaoli1699/PathSeacher/raw/master/images/map.png)
	![area](https://github.com/chaoli1699/PathSeacher/raw/master/images/area.png)
	![aim](https://github.com/chaoli1699/PathSeacher/raw/master/images/aim.png)

    注： PSMapView对象中有setStonesVisiable(boolean b);功能用来显示障碍物阴影，以便矫正阴影与实际地图的贴合度，astar_android.db中map_info的unknow_scale既为错位矫正参数，希望大家善用。
	
	
    3. 在MyApplication OnCreat()方法中初始化地图
	
       ![app](https://github.com/chaoli1699/PathSeacher/raw/master/images/app.png)

    4. 在layout中添加PSMapView
	
       ![layout](https://github.com/chaoli1699/PathSeacher/raw/master/images/layout.png)

    5. 在Activity中调用	
	
	
       ![activity](https://github.com/chaoli1699/PathSeacher/raw/master/images/activity.png)
	
	
    6. 调试结果：
	
	
       ![test](https://github.com/chaoli1699/PathSeacher/raw/master/images/test.png)	
