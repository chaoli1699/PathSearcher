package cn.cienet.pathsearcher.sql;

import cn.cienet.pathsearcher.interfaces.IMapBean;

public class BeanFactory implements IMapBean{

	@Override
	public MapBeanDao getMapBean() {
		// TODO Auto-generated method stub
		return new MapBeanDao();
	}
}
