package com.kongzhong.mrpc.admin.bootstrap;

import com.blade.exception.ValidatorException;
import com.blade.ioc.annotation.Bean;
import com.blade.mvc.WebContext;
import com.blade.mvc.handler.DefaultExceptionHandler;
import com.blade.mvc.ui.RestResponse;

/**
 * @author biezhi
 * @date 2018/6/6
 */
@Bean
public class ExceptionHandler extends DefaultExceptionHandler {

    @Override
    public void handle(Exception e) {
        if (ValidatorException.class.isInstance(e)) {
            WebContext.response().json(RestResponse.fail(e.getMessage()));
        } else {
            super.handle(e);
        }
    }
}
