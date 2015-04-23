package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.ElementMeta;
import com.amecfw.sage.persistence.ElementMetaDao;

public class ElementMetaTest extends AbstractDaoTestLongPk<ElementMetaDao, ElementMeta> {

    public ElementMetaTest() {
        super(ElementMetaDao.class);
    }

    @Override
    protected ElementMeta createEntity(Long key) {
        ElementMeta entity = new ElementMeta();
        entity.setId(key);
        entity.setRowGuid();
        entity.setParentID();
        return entity;
    }

}
