package com.linglong.rpc.exception;

/**
 * @author Stereo on 2019/12/12.
 */
public class NotAllowedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7552833324276839926L;

	public NotAllowedException() {
		super();
	}

	public NotAllowedException(String message) {
		super(message);
	}

}
