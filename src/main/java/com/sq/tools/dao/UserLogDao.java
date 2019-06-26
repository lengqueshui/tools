package com.sq.tools.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserLogDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void save(String openId, String msgType, String msgId, String content, String picUrl){
        final String sql = "INSERT INTO wechat_user_msg_record (open_id, msg_type, msg_id, content, " +
                "pic_url, created_at) VALUES (?, ?, ?, ?,  ?, NOW())";
        jdbcTemplate.update(sql, openId, msgType, msgId, content, picUrl);
    }


}
