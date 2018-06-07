package com.kongzhong.mrpc.admin.service;

import com.blade.exception.ValidatorException;
import com.blade.ioc.annotation.Bean;
import com.blade.kit.EncryptKit;
import com.blade.mvc.ui.RestResponse;
import com.kongzhong.mrpc.admin.model.SysUser;
import com.kongzhong.mrpc.admin.params.LoginParam;
import com.kongzhong.mrpc.admin.validators.CommonValidator;

import static io.github.biezhi.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/6/6
 */
@Bean
public class UserService {

    public RestResponse<SysUser> login(LoginParam loginParam) {
        CommonValidator.valid(loginParam);

        String pwd = EncryptKit.md5(loginParam.getUsername() + loginParam.getPassword());

        SysUser sysUser = select().from(SysUser.class)
                .where(SysUser::getUsername, loginParam.getUsername())
                .and(SysUser::getPassword, pwd).one();
        if (null == sysUser) {
            return RestResponse.fail("用户名或密码错误");
        }

        return RestResponse.ok(sysUser);
    }

}
