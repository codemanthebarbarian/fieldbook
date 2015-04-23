package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.Observation;
import com.amecfw.sage.persistence.ObservationDao;

public class ObservationTest extends AbstractDaoTestLongPk<ObservationDao, Observation> {

    public ObservationTest() {
        super(ObservationDao.class);
    }

    @Override
    protected Observation createEntity(Long key) {
        Observation entity = new Observation();
        entity.setId(key);
        entity.setRowGuid();
        entity.setStationID();
        entity.setObservationTypeID();
        return entity;
    }

}
