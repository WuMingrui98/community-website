package com.wmr.community.dao;

import com.wmr.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

import java.util.Date;

@Mapper
public interface LoginTicketMapper {

    @Select({"SELECT * FROM login_ticket ",
            "WHERE ticket = #{ticket};"})
    LoginTicket selectByTicket(String ticket);

    // 演示一下注解方式动态sql怎么写
    @Update({
            "<script>",
            "UPDATE login_ticket SET `status` = #{status} ",
            "WHERE ticket = #{ticket} ",
            "<if test=\"ticket!=null\">",
            "AND 1 = 1",
            "</if>",
            "</script>"})
    int updateStatus(String ticket, int status);

    // 插入操作的形参要是POJO对象
    @Insert({"INSERT INTO login_ticket(user_id, ticket, `status`, expired) ",
            "VALUES(#{userId}, #{ticket}, #{status}, #{expired});"})
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);
}
