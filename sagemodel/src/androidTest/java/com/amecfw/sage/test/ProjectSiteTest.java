package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.ProjectSite;
import com.amecfw.sage.persistence.ProjectSiteDao;

public class ProjectSiteTest extends AbstractDaoTestLongPk<ProjectSiteDao, ProjectSite> {

    public ProjectSiteTest() {
        super(ProjectSiteDao.class);
    }

    @Override
    protected ProjectSite createEntity(Long key) {
        ProjectSite entity = new ProjectSite();
        entity.setId(key);
        entity.setRowGuid();
        return entity;
    }

}
