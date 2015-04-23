package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.Measurement;
import com.amecfw.sage.persistence.MeasurementDao;

public class MeasurementTest extends AbstractDaoTestLongPk<MeasurementDao, Measurement> {

    public MeasurementTest() {
        super(MeasurementDao.class);
    }

    @Override
    protected Measurement createEntity(Long key) {
        Measurement entity = new Measurement();
        entity.setId(key);
        entity.setRowGuid();
        entity.setStationID();
        entity.setParameterID();
        return entity;
    }

}
