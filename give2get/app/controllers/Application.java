package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
    	List users = User.find("order by id desc").fetch();
        render(users);
    }

    public static void signUp(String name, String surname, String username, String password) {
    	User user = new User(name, surname, username, password).save();
    	renderJSON(user);
    }
}