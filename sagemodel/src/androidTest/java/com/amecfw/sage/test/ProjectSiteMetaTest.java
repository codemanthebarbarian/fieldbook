package com.amecfw.sage.test;

import de.greenrobot.dao.test.AbstractDaoTestLongPk;

import com.amecfw.sage.model.ProjectSiteMeta;
import com.amecfw.sage.persistence.ProjectSiteMetaDao;

public class ProjectSiteMetaTest extends AbstractDaoTestLongPk<ProjectSiteMetaDao, ProjectSiteMeta> {

    public ProjectSiteMetaTest() {
        super(ProjectSiteMetaDao.class);
    }

    @Override
    protected ProjectSiteMeta createEntity(Long key) {
        ProjectSiteMeta entity = new ProjectSiteMeta();
        entity.setId(key);
        entity.setRowGuid();
        entity.setParentID();
        return entity;
    }

}
