package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.GroupObservation;
import com.amecfw.sage.persistence.GroupObservationDao;

public class GroupObservationTest extends AbstractDaoTestLongPk<GroupObservationDao, GroupObservation> {

    public GroupObservationTest() {
        super(GroupObservationDao.class);
    }

    @Override
    protected GroupObservation createEntity(Long key) {
        GroupObservation entity = new GroupObservation();
        entity.setId(key);
        entity.setRowGuid();
        entity.setObservationTypeID();
        entity.setObservationGroupID();
        return entity;
    }

}
