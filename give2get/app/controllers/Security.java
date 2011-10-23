package controllers;

import play.mvc.*;
import models.*;

public class Security extends Secure.Security {

	static boolean authenticate(String username, String password) {
		//return User.connect(username, password) != null;
		return true;
    }
	
	static void onDisconnected() {
	    Application.index();
	}
	
	static void onAuthenticated() {
	    Admin.index();
	}
	
	//To create an admin profile, you just need to override the check() method in the controllers.
	static boolean check(String profile) {
	    if("admin".equals(profile)) {
	        return User.find("byEmail", connected()).<User>first().status == "admin";
	    }
	    return false;
	}


}
