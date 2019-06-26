package com.sq.tools.service;

import com.sq.tools.dao.UserLogDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.object.SqlOperation;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    @Autowired
    private UserLogDao userLogDao;

    public boolean saveUserMsg(String openId, String msgType, String msgId, String content, String picUrl){
        userLogDao.save(openId, msgType, msgId, content, picUrl);
        return true;
    }
}
