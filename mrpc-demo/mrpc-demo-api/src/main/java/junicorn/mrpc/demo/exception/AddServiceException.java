package junicorn.mrpc.demo.exception;

public class AddServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;
	public AddServiceException() { }

	public AddServiceException(String message) {
			super(message);
		}
		
}