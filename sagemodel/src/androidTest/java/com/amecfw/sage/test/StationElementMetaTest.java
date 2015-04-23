package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.StationElementMeta;
import com.amecfw.sage.persistence.StationElementMetaDao;

public class StationElementMetaTest extends AbstractDaoTestLongPk<StationElementMetaDao, StationElementMeta> {

    public StationElementMetaTest() {
        super(StationElementMetaDao.class);
    }

    @Override
    protected StationElementMeta createEntity(Long key) {
        StationElementMeta entity = new StationElementMeta();
        entity.setId(key);
        entity.setRowGuid();
        entity.setParentID();
        return entity;
    }

}
