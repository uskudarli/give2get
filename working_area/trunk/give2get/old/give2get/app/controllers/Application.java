package controllers;

import play.*;
import play.mvc.*;

import play.libs.*;
import play.cache.*;

import java.util.*;

import org.eclipse.jdt.core.dom.ThisExpression;

import models.*;

public class Application extends Controller {
	
    public static void index() {
    	List<User> users = User.find("order by id desc").fetch();
        render(users);
    }
    
	public static void captcha(String id) {
	    Images.Captcha captcha = Images.captcha();
	    String code = captcha.getText("#222222");
	    Cache.set(id, code, "10mn");
	    renderBinary(captcha);
	}
	
    public static void signUp(String name, String surname, String username, String password, String email) {
    	
    	if (validation.hasErrors()) {
            params.flash(); // add http parameters to the flash scope
            validation.keep(); // keep the errors for the next request
            index();
    	}
    	else {
    		User user = new User(name, surname, username, password, email).save();
    		renderJSON(user);
    	}
    }
    
}