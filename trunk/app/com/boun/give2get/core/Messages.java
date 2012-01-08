package com.boun.give2get.core;

import org.apache.log4j.Logger;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 9, 2011
 * Time: 1:43:47 AM
 * To change this template use File | Settings | File Templates.
 */
public final class Messages {

    private static final Logger log = Logger.getLogger(Messages.class);

    private static Properties messages;

    public static void init() throws Exception {

        messages = new Properties();

        try {

            messages.load(Messages.class.getResourceAsStream("messages.properties"));

        } catch(Exception e) {

            log.error(e,e);

            throw e;

        }

    }


    public static String get(String key) {
        return messages.getProperty(key);

    }
}
