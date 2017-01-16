package junicorn.mrpc.demo.server.impl;

import junicorn.mrpc.demo.api.DeptService;
import junicorn.mrpc.spring.annotation.MRpcService;

/**
 * Created by biezhi on 2016/10/29.
 */
@MRpcService
public class DeptServiceImpl implements DeptService {

    @Override
    public void save(String dept) {
        System.out.println("save - " + dept);
    }

}
