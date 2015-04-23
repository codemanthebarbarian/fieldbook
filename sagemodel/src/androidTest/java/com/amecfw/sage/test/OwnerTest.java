package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.Owner;
import com.amecfw.sage.persistence.OwnerDao;

public class OwnerTest extends AbstractDaoTestLongPk<OwnerDao, Owner> {

    public OwnerTest() {
        super(OwnerDao.class);
    }

    @Override
    protected Owner createEntity(Long key) {
        Owner entity = new Owner();
        entity.setId(key);
        entity.setRowGuid();
        entity.setName();
        entity.setType();
        return entity;
    }

}
