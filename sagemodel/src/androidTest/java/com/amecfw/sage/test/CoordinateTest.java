package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.Coordinate;
import com.amecfw.sage.persistence.CoordinateDao;

public class CoordinateTest extends AbstractDaoTestLongPk<CoordinateDao, Coordinate> {

    public CoordinateTest() {
        super(CoordinateDao.class);
    }

    @Override
    protected Coordinate createEntity(Long key) {
        Coordinate entity = new Coordinate();
        entity.setId(key);
        entity.setRowGuid();
        return entity;
    }

}
