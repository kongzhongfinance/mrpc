package com.kongzhong.mrpc.admin.validators;

import com.blade.validator.Validators;
import com.kongzhong.mrpc.admin.params.LoginParam;

/**
 * @author biezhi
 * @date 2018/6/6
 */
public class CommonValidator {

    public static void valid(LoginParam loginParam) {
        Validators.notEmpty().test(loginParam.getUsername()).throwMessage("用户名");
        Validators.notEmpty().test(loginParam.getPassword()).throwMessage("密码");

    }

}
