package com.boun.give2get.mail;

import org.apache.log4j.Logger;

import java.util.Properties;
import java.io.IOException;
import java.io.FileInputStream;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.boun.give2get.core.Configuration;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 8, 2011
 * Time: 4:39:55 PM
 * To change this template use File | Settings | File Templates.
 */

public final class MailFactory {

    private static final Logger log = Logger.getLogger(MailFactory.class);

    private static PropertiesCredentials credentials;
    private static Properties mailProperties;

    private static String from;

    private static String registrationSubject;
    private static String registrationContent;

    private static String welcomeSubject;
    private static String welcomeContent;


    //  AWS Credentials
    private static final String ACCESS_KEY = "AKIAJGSRHPNJSO7SWAGQ";
    private static final String SECRET_KEY = "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"; 


    public static final void init () throws IOException {

        //  Mail Properties
        mailProperties = new Properties();
		mailProperties.setProperty("mail.transport.protocol",   "aws");
        mailProperties.setProperty("mail.aws.user",             ACCESS_KEY);
        mailProperties.setProperty("mail.aws.password",         SECRET_KEY);


        //  Other Properties
        Properties otherProps = new Properties();
        otherProps.load(
                MailFactory.class.getResourceAsStream("mail.properties")
        );

        from = otherProps.getProperty("from");

        registrationContent = otherProps.getProperty("register.content");
        registrationSubject = otherProps.getProperty("register.subject");
        welcomeContent      = otherProps.getProperty("welcome.content");
        welcomeSubject      = otherProps.getProperty("welcome.subject");

    }

    public static AmazonSimpleEmailServiceClient getNewInstance() {

        return new AmazonSimpleEmailServiceClient(credentials);

    }

    public static Properties getMailProperties() {
        return mailProperties;
    }


    public static String getFrom() {
        return from;
    }

    public static String getRegistrationSubject() {
        return registrationSubject;
    }

    public static String getRegistrationContent() {
        return registrationContent;
    }

    public static String getWelcomeSubject() {
        return welcomeSubject;
    }

    public static String getWelcomeContent() {
        return welcomeContent;
    }

}
