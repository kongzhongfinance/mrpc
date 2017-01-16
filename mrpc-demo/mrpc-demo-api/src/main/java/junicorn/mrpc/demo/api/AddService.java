package junicorn.mrpc.demo.api;


import junicorn.mrpc.demo.exception.AddServiceException;

public interface AddService {
	
	Integer add(Integer param);

	void exception() throws AddServiceException;

}
