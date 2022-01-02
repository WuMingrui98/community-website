package com.wmr.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository("alphaDAOHibernate")
public class AlphaDAOHibernateImpl implements AlphaDAO{
    @Override
    public String select() {
        return "Hibernate";
    }
}
