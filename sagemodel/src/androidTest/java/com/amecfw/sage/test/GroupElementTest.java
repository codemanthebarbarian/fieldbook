package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.GroupElement;
import com.amecfw.sage.persistence.GroupElementDao;

public class GroupElementTest extends AbstractDaoTestLongPk<GroupElementDao, GroupElement> {

    public GroupElementTest() {
        super(GroupElementDao.class);
    }

    @Override
    protected GroupElement createEntity(Long key) {
        GroupElement entity = new GroupElement();
        entity.setId(key);
        entity.setRowGuid();
        return entity;
    }

}
