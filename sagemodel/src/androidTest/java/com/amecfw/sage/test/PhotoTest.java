package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.Photo;
import com.amecfw.sage.persistence.PhotoDao;

public class PhotoTest extends AbstractDaoTestLongPk<PhotoDao, Photo> {

    public PhotoTest() {
        super(PhotoDao.class);
    }

    @Override
    protected Photo createEntity(Long key) {
        Photo entity = new Photo();
        entity.setId(key);
        entity.setRowGuid();
        return entity;
    }

}
