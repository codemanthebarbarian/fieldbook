package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.GroupObservationMeta;
import com.amecfw.sage.persistence.GroupObservationMetaDao;

public class GroupObservationMetaTest extends AbstractDaoTestLongPk<GroupObservationMetaDao, GroupObservationMeta> {

    public GroupObservationMetaTest() {
        super(GroupObservationMetaDao.class);
    }

    @Override
    protected GroupObservationMeta createEntity(Long key) {
        GroupObservationMeta entity = new GroupObservationMeta();
        entity.setId(key);
        entity.setRowGuid();
        entity.setParentID();
        return entity;
    }

}
