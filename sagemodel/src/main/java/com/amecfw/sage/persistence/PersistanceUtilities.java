package com.amecfw.sage.persistence;

import com.amecfw.sage.model.EntityBase;
import com.amecfw.sage.model.SageApplication;

import de.greenrobot.dao.AbstractDao;

public class PersistanceUtilities {

	public static String getTableName(EntityBase obj){
		DaoSession session = SageApplication.getInstance().getDaoSession();
		AbstractDao<?,?> dao = session.getDao(obj.getClass());
		String tblName = dao.getTablename();
		return tblName;
	}
	
}
