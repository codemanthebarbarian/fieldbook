package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.Station;
import com.amecfw.sage.persistence.StationDao;

public class StationTest extends AbstractDaoTestLongPk<StationDao, Station> {

    public StationTest() {
        super(StationDao.class);
    }

    @Override
    protected Station createEntity(Long key) {
        Station entity = new Station();
        entity.setId(key);
        entity.setRowGuid();
        entity.setName();
        entity.setStationType();
        return entity;
    }

}
