package controllers;

import play.Logger;
import play.mvc.*;
import models.*;

public class Security extends Secure.Security {

	static boolean authenticate(String username, String password) {
		return User.getUser(username, password) != null;
    }
	
	//To create an admin profile, you just need to override the check() method in the controllers.
	static boolean check(String profile) {
		
		// If profile is inactive
	    if("inactive".equals(User.find("byUsername", connected()).<User>first().status)) {
	        //return User.find("byUsername", connected()).<User>first().status == "admin";
	    	return false;
	    }
	    else if("admin".equals(User.find("byUsername", connected()).<User>first().status)){
	    	return true;
	    }
	    
	    return true;
	}
	
	static void onDisconnected() {
	    Logger.info("Signed out by user %s", connected());
	    Application.index();
	}
	
	static void onCheckFailed(String profile) {
		Logger.warn("Failed auth for profile -> %s", profile);
		forbidden();
		//Application.index();
	}
	
	static void onAuthenticated() {
		Logger.info("Signed in by user %s", connected());
		// if admin go to admin panel
		
		
		// else go to user screen
		Home.index();
	}
	



}
