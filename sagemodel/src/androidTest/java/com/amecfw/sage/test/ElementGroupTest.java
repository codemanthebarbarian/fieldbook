package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.ElementGroup;
import com.amecfw.sage.persistence.ElementGroupDao;

public class ElementGroupTest extends AbstractDaoTestLongPk<ElementGroupDao, ElementGroup> {

    public ElementGroupTest() {
        super(ElementGroupDao.class);
    }

    @Override
    protected ElementGroup createEntity(Long key) {
        ElementGroup entity = new ElementGroup();
        entity.setId(key);
        entity.setRowGuid();
        entity.setName();
        return entity;
    }

}
