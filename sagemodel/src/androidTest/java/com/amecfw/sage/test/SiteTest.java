package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.Site;
import com.amecfw.sage.persistence.SiteDao;

public class SiteTest extends AbstractDaoTestLongPk<SiteDao, Site> {

    public SiteTest() {
        super(SiteDao.class);
    }

    @Override
    protected Site createEntity(Long key) {
        Site entity = new Site();
        entity.setId(key);
        entity.setRowGuid();
        return entity;
    }

}
