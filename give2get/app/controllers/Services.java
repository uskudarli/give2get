package controllers;

import play.mvc.*;

@With(Secure.class)
@Check("user")

public class Services extends Controller {

    public static void index() {
        render();
    }

}
