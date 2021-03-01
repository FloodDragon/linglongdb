
package com.linglong.replication;

import java.io.IOException;

/**
 * @author Stereo
 */
public class JoinException extends IOException {
    private static final long serialVersionUID = 1L;

    public JoinException() {
    }

    public JoinException(String message) {
        super(message);
    }
}
