package controllers;
 
import play.*;
import play.mvc.*;

import java.util.*;
 
import models.*;

@With(Secure.class)
public class Home extends Controller {
    
    @Before
    static void setConnectedUser() {
        if(Security.isConnected()) {
            User user = User.find("byEmail", Security.connected()).first();
        }
        
        //Application.index();

        //forbidden("HOOOOOOOOP");
    }
 
    @Check("user")
    public static void index() {
        render();
    }
    
}