package com.linglong.server.database.controller.annotation;

import java.lang.annotation.*;

/**
 * Created by liuj-ai on 2021/4/29.
 */

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Leader {
    LeaderOp op() default LeaderOp.READ_ONLY;
}
