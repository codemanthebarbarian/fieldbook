package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.ElementGroupMeta;
import com.amecfw.sage.persistence.ElementGroupMetaDao;

public class ElementGroupMetaTest extends AbstractDaoTestLongPk<ElementGroupMetaDao, ElementGroupMeta> {

    public ElementGroupMetaTest() {
        super(ElementGroupMetaDao.class);
    }

    @Override
    protected ElementGroupMeta createEntity(Long key) {
        ElementGroupMeta entity = new ElementGroupMeta();
        entity.setId(key);
        entity.setRowGuid();
        entity.setParentID();
        return entity;
    }

}
