package com.linglong.rpc.exception;

import java.util.Arrays;

/**
 * @author Stereo on 2019/12/12.
 */
public class MethodNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7559230924102506068L;

	public MethodNotFoundException(String methodName) {
		super("Method " + methodName + " without arguments not found");
	}

	public MethodNotFoundException(String methodName, Object[] args) {
		super("Method " + methodName + " with arguments " + Arrays.asList(args) + " not found");
	}

}
