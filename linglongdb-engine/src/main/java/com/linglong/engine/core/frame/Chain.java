package com.linglong.engine.core.frame;

/**
 * @author Stereo
 */
public interface Chain<E> {
    E element();

    Chain<E> next();
}
