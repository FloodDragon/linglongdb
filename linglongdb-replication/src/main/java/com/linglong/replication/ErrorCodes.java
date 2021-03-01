package com.linglong.replication;

import java.util.logging.Level;

/**
 * @author Stereo
 */
final class ErrorCodes {
    static final byte SUCCESS = 0, UNKNOWN_OPERATION = 1, NO_ACCEPTOR = 2,
            UNKNOWN_MEMBER = 3, UNCONNECTED_MEMBER = 4, INVALID_ADDRESS = 5,
            VERSION_MISMATCH = 6, NO_CONSENSUS = 7, NO_LEADER = 8, NOT_LEADER = 9;

    static String toString(byte errorCode) {
        switch (errorCode) {
            default:
                return "unknown error: " + errorCode;
            case SUCCESS:
                return "success";
            case UNKNOWN_OPERATION:
                return "unknown operation";
            case NO_ACCEPTOR:
                return "no acceptor";
            case UNKNOWN_MEMBER:
                return "unknown member";
            case INVALID_ADDRESS:
                return "invalid address";
            case UNCONNECTED_MEMBER:
                return "unconnected member";
            case VERSION_MISMATCH:
                return "group version mismatch";
            case NO_CONSENSUS:
                return "no consensus";
            case NO_LEADER:
                return "no leader";
            case NOT_LEADER:
                return "not leader";
        }
    }

    static Level levelFor(byte errorCode) {
        switch (errorCode) {
            default:
                return Level.SEVERE;
            case VERSION_MISMATCH:
            case NO_CONSENSUS:
            case NO_LEADER:
            case NOT_LEADER:
                return Level.WARNING;
            case SUCCESS:
                return Level.INFO;
        }
    }
}
