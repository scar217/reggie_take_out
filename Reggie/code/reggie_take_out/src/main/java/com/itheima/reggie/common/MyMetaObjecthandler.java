package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 自定义元数据对象处理器
 * */
@Component
@Slf4j
public class MyMetaObjecthandler implements MetaObjectHandler {
    @Autowired
    HttpServletRequest request;//通过自动装配的方式 调用getSession.getAttribute("employee")获取当前登录者的id
    /**
     * 注意：自动填充的表需要涉及以下所有字段，不然插入或更新会报错
     */

    /**
     * 插入操作，自动填充
     * */
    @Override
    public void insertFill(MetaObject metaObject) {
        Long empId = (Long) request.getSession().getAttribute("employee");
        Long userId = (Long) request.getSession().getAttribute("user");
        log.info("公共字段自动填充[insert]..........");
        log.info("创建者id:{}",empId);
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        if(empId==null){//移动执行插入操作
            metaObject.setValue("createUser", userId);
            metaObject.setValue("updateUser", userId);
        }else {//后台执行插入操作
            metaObject.setValue("createUser", empId);
            metaObject.setValue("updateUser", empId);
        }

    }
    /**
     * 更新修改操作，自动填充
     * */
    @Override
    public void updateFill(MetaObject metaObject) {
        Long empId = (Long) request.getSession().getAttribute("employee");
        Long userId = (Long) request.getSession().getAttribute("user");
        log.info("公共字段自动填充[update]..........");
        log.info("更新者id:{}",empId);
        metaObject.setValue("updateTime",LocalDateTime.now());
        if(empId==null){//移动端更新操作
            metaObject.setValue("updateUser", userId);
        }else {//后台更新操作
            metaObject.setValue("updateUser", empId);
        }
    }
}
