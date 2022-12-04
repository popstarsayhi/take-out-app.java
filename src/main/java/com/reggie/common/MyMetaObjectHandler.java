package com.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;


/**
 * Customized meta object handler
 */

@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * autofill when inserting data
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("public fields autofill[insert]");
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

    /**
     * autofille when updating data
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("public fields autofill[update]");
        log.info(metaObject.toString());

        long id = Thread.currentThread().getId();
        log.info("thread id is {}", id);

        metaObject.setValue("updateTime", LocalDateTime.now());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
