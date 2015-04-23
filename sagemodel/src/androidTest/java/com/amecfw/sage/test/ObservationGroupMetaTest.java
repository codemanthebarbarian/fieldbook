package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.ObservationGroupMeta;
import com.amecfw.sage.persistence.ObservationGroupMetaDao;

public class ObservationGroupMetaTest extends AbstractDaoTestLongPk<ObservationGroupMetaDao, ObservationGroupMeta> {

    public ObservationGroupMetaTest() {
        super(ObservationGroupMetaDao.class);
    }

    @Override
    protected ObservationGroupMeta createEntity(Long key) {
        ObservationGroupMeta entity = new ObservationGroupMeta();
        entity.setId(key);
        entity.setRowGuid();
        entity.setParentID();
        return entity;
    }

}
