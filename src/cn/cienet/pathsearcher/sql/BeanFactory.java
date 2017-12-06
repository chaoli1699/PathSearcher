package cn.cienet.pathsearcher.sql;

import cn.cienet.pathsearcher.interfaces.IMapBean;
import cn.cienet.pathsearcher.interfaces.IVoiceBean;

public class BeanFactory implements IMapBean, IVoiceBean{

	@Override
	public MapBeanDao getMapBean() {
		// TODO Auto-generated method stub
		return new MapBeanDao();
	}

	@Override
	public VoiceBeanDao getVoiceBean() {
		// TODO Auto-generated method stub
		return new VoiceBeanDao();
	}

	
}
