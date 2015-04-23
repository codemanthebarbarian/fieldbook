package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.Element;
import com.amecfw.sage.persistence.ElementDao;

public class ElementTest extends AbstractDaoTestLongPk<ElementDao, Element> {

    public ElementTest() {
        super(ElementDao.class);
    }

    @Override
    protected Element createEntity(Long key) {
        Element entity = new Element();
        entity.setId(key);
        entity.setRowGuid();
        return entity;
    }

}
