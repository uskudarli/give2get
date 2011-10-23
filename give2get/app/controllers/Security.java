package controllers;

import play.mvc.*;
import models.*;

public class Security extends Secure.Security {

	static boolean authenticate(String username, String password) {
		return User.connect(username, password) != null;
    }
	
	static void onDisconnected() {
	    Application.index();
	}
	
	static void onAuthenticated() {
		// if admin go to admin panel
		SignIn.index();
		
		// else go to user screen
	    
	}
	
	//To create an admin profile, you just need to override the check() method in the controllers.
	static boolean check(String profile) {
	    if(profile.equals("admin")) {
	        return User.find("byUsername", connected()).<User>first().status == "admin";
	    }
	    return false;
	}


}
