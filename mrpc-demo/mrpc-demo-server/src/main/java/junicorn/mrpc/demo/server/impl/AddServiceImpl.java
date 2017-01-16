package junicorn.mrpc.demo.server.impl;

import junicorn.mrpc.demo.api.AddService;
import junicorn.mrpc.demo.exception.AddServiceException;
import junicorn.mrpc.spring.annotation.MRpcService;

@MRpcService
public class AddServiceImpl implements AddService {

	@Override
	public Integer add(Integer param) {
		return ++param;
	}

	@Override
	public void exception() throws AddServiceException {
		throw new AddServiceException("抛出运行时异常");
	}
	
}
