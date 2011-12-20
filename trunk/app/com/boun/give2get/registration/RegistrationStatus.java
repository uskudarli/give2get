package com.boun.give2get.registration;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 8, 2011
 * Time: 5:08:37 PM
 * To change this template use File | Settings | File Templates.
 */
public enum RegistrationStatus {

    NOT_ACTIVATED, ACTIVATED, DISABLED;

    public static RegistrationStatus fromString(String statusAsString) {

        if (statusAsString.equalsIgnoreCase(RegistrationStatus.NOT_ACTIVATED.toString()))
            return RegistrationStatus.NOT_ACTIVATED;
        else if (statusAsString.equalsIgnoreCase(RegistrationStatus.ACTIVATED.toString()))
            return RegistrationStatus.ACTIVATED;
        else
            return RegistrationStatus.DISABLED;

    }

}
