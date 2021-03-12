
package com.linglong.rpc.exception;


/**
 * @author Stereo on 2019/12/12.
 */
public class ClientTimeoutException extends RuntimeException{

    private static final long serialVersionUID = -3008927155169307876L;

    public ClientTimeoutException(Throwable throwable) {
        super(throwable);
    }

    public ClientTimeoutException(String errorMsg) {
        super(errorMsg);
    }

    public ClientTimeoutException(String errorMsg, Throwable throwable) {
        super(errorMsg, throwable);
    }
}
