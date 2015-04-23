package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.PhotoMeta;
import com.amecfw.sage.persistence.PhotoMetaDao;

public class PhotoMetaTest extends AbstractDaoTestLongPk<PhotoMetaDao, PhotoMeta> {

    public PhotoMetaTest() {
        super(PhotoMetaDao.class);
    }

    @Override
    protected PhotoMeta createEntity(Long key) {
        PhotoMeta entity = new PhotoMeta();
        entity.setId(key);
        entity.setRowGuid();
        entity.setParentID();
        return entity;
    }

}
