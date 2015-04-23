package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.Parameter;
import com.amecfw.sage.persistence.ParameterDao;

public class ParameterTest extends AbstractDaoTestLongPk<ParameterDao, Parameter> {

    public ParameterTest() {
        super(ParameterDao.class);
    }

    @Override
    protected Parameter createEntity(Long key) {
        Parameter entity = new Parameter();
        entity.setId(key);
        entity.setRowGuid();
        entity.setName();
        return entity;
    }

}
