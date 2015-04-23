package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.Location;
import com.amecfw.sage.persistence.LocationDao;

public class LocationTest extends AbstractDaoTestLongPk<LocationDao, Location> {

    public LocationTest() {
        super(LocationDao.class);
    }

    @Override
    protected Location createEntity(Long key) {
        Location entity = new Location();
        entity.setId(key);
        entity.setRowGuid();
        return entity;
    }

}
