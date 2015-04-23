package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.StationElement;
import com.amecfw.sage.persistence.StationElementDao;

public class StationElementTest extends AbstractDaoTestLongPk<StationElementDao, StationElement> {

    public StationElementTest() {
        super(StationElementDao.class);
    }

    @Override
    protected StationElement createEntity(Long key) {
        StationElement entity = new StationElement();
        entity.setId(key);
        entity.setRowGuid();
        entity.setStationID();
        entity.setElementID();
        return entity;
    }

}
