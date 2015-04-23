package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.ObservationMeta;
import com.amecfw.sage.persistence.ObservationMetaDao;

public class ObservationMetaTest extends AbstractDaoTestLongPk<ObservationMetaDao, ObservationMeta> {

    public ObservationMetaTest() {
        super(ObservationMetaDao.class);
    }

    @Override
    protected ObservationMeta createEntity(Long key) {
        ObservationMeta entity = new ObservationMeta();
        entity.setId(key);
        entity.setRowGuid();
        entity.setParentID();
        return entity;
    }

}
