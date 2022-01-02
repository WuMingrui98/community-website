package com.wmr.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public class AlphaDAOMyBatis implements AlphaDAO{
    @Override
    public String select() {
        return "MyBatis";
    }
}
