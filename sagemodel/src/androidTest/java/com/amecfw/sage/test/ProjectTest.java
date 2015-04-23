package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.Project;
import com.amecfw.sage.persistence.ProjectDao;

public class ProjectTest extends AbstractDaoTestLongPk<ProjectDao, Project> {

    public ProjectTest() {
        super(ProjectDao.class);
    }

    @Override
    protected Project createEntity(Long key) {
        Project entity = new Project();
        entity.setId(key);
        entity.setRowGuid();
        return entity;
    }

}
