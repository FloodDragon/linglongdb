package com.glodon.linglong.engine.core.frame;

/**
 * @author Stereo
 */
public interface Chain<E> {
    E element();

    Chain<E> next();
}
