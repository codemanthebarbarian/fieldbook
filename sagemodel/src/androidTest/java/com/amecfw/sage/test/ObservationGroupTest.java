package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.ObservationGroup;
import com.amecfw.sage.persistence.ObservationGroupDao;

public class ObservationGroupTest extends AbstractDaoTestLongPk<ObservationGroupDao, ObservationGroup> {

    public ObservationGroupTest() {
        super(ObservationGroupDao.class);
    }

    @Override
    protected ObservationGroup createEntity(Long key) {
        ObservationGroup entity = new ObservationGroup();
        entity.setId(key);
        entity.setRowGuid();
        entity.setName();
        return entity;
    }

}
