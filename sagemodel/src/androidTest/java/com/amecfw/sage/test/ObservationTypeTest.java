package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.ObservationType;
import com.amecfw.sage.persistence.ObservationTypeDao;

public class ObservationTypeTest extends AbstractDaoTestLongPk<ObservationTypeDao, ObservationType> {

    public ObservationTypeTest() {
        super(ObservationTypeDao.class);
    }

    @Override
    protected ObservationType createEntity(Long key) {
        ObservationType entity = new ObservationType();
        entity.setId(key);
        entity.setRowGuid();
        entity.setName();
        return entity;
    }

}
