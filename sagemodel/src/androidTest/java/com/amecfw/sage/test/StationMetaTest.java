package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.StationMeta;
import com.amecfw.sage.persistence.StationMetaDao;

public class StationMetaTest extends AbstractDaoTestLongPk<StationMetaDao, StationMeta> {

    public StationMetaTest() {
        super(StationMetaDao.class);
    }

    @Override
    protected StationMeta createEntity(Long key) {
        StationMeta entity = new StationMeta();
        entity.setId(key);
        entity.setRowGuid();
        entity.setParentID();
        return entity;
    }

}
