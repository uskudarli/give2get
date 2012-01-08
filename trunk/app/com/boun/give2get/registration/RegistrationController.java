package com.boun.give2get.registration;

import models.Registration;
import models.User;
import models.ActivityType;

import java.util.UUID;
import java.util.Date;

import play.db.DB;
import com.boun.give2get.db.DAO;


/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 8, 2011
 * Time: 5:09:17 PM
 * To change this template use File | Settings | File Templates.
 */
public final class RegistrationController {

    public static Registration createNewRegistration(User user) {

        Registration registration = new Registration();

        /*registration.email          = user.email;
        registration.firstname      = user.firstname;
        registration.lastname       = user.lastname;
        registration.password       = user.password;
        registration.code           = UUID.randomUUID().toString();
        registration.created        = new Date(); */

        return registration;


    }


    public static final Registration getRegistration (String code) {
        return DAO.getRegistration(code);
    } 

    public static void activateRegistration(String registrationCode) {

        //  Activate Registration
        DAO.activateRegistration(registrationCode);
        
    }
    
}
