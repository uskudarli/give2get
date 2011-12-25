package com.boun.give2get.exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 8, 2011
 * Time: 9:46:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataStoreException extends RuntimeException{

    public DataStoreException(Throwable cause) {
        super(cause);
    }

    public DataStoreException(String message) {
        super(message);
    }
}
