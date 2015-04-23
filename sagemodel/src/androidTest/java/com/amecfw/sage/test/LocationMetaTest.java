package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.LocationMeta;
import com.amecfw.sage.persistence.LocationMetaDao;

public class LocationMetaTest extends AbstractDaoTestLongPk<LocationMetaDao, LocationMeta> {

    public LocationMetaTest() {
        super(LocationMetaDao.class);
    }

    @Override
    protected LocationMeta createEntity(Long key) {
        LocationMeta entity = new LocationMeta();
        entity.setId(key);
        entity.setRowGuid();
        entity.setParentID();
        return entity;
    }

}
