package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.MeasurementMeta;
import com.amecfw.sage.persistence.MeasurementMetaDao;

public class MeasurementMetaTest extends AbstractDaoTestLongPk<MeasurementMetaDao, MeasurementMeta> {

    public MeasurementMetaTest() {
        super(MeasurementMetaDao.class);
    }

    @Override
    protected MeasurementMeta createEntity(Long key) {
        MeasurementMeta entity = new MeasurementMeta();
        entity.setId(key);
        entity.setRowGuid();
        entity.setParentID();
        return entity;
    }

}
