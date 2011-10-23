package controllers;

import play.*;
import play.mvc.*;

import play.libs.*;
import play.cache.*;

import java.util.*;

import models.*;


@With(Secure.class)
public class Application extends Controller {

	public static void captcha(String id) {
	    Images.Captcha captcha = Images.captcha();
	    String code = captcha.getText("#222222");
	    Cache.set(id, code, "10mn");
	    renderBinary(captcha);
	}
	
    public static void index() {
    	List users = User.find("order by id desc").fetch();
        render(users);
    }

    public static void signUp(String name, String surname, String username, String password, String email) {
    	User user = new User(name, surname, username, password, email).save();
    	renderJSON(user);
    }
    
    public static void signIn() {
    	
    }
}