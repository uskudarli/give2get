package controllers;

import play.mvc.*;
import play.data.validation.Required;

import models.*;
import com.boun.give2get.registration.RegistrationController;
import com.boun.give2get.db.DAO;
import com.boun.give2get.db.ServiceDAO;
import com.boun.give2get.mail.MailUtil;
import com.boun.give2get.exceptions.MailException;
import org.apache.log4j.Logger;

import java.util.List;

public class Application extends Controller {


    @Before
    public static void addUser() {        

        User user = isUserConnected();

        if (user != null) {

            System.out.println("User is connected!");

            renderArgs.put("user",      user);
            renderArgs.put("username",  user.getRegistration().getFirstname().concat(" ").concat(user.getRegistration().getLastname()));
        }  


    }

    public static final User isUserConnected() {

        
        //  Check render Args
        if (renderArgs.get("user") != null) {

            User user = renderArgs.get("user", User.class);

            System.out.println("User found in render args!");

            return user;

        }

        //  Check session
        String userid = session.get("userid");

        if (userid != null) {

            User user = DAO.getUser(Integer.valueOf(userid).intValue());

            System.out.println("User fetched from databse!");

            return user;

        }         

        //  Not Logged in
        return null;

    }

    public static void index() {
       
        //  Recent Activities;
        List<Activity> recentActivities = DAO.getRecentActivities();

        renderArgs.put("recentActivities", recentActivities);



        //  Top Rated Services
        List<TopRatedService> topRatedServices = DAO.getTopRatedServices();

        renderArgs.put("topRatedServices", topRatedServices);


        //  Top Rated Users
        List<TopRatedUser> topRatedUsers = DAO.getTopRatedUsers();

        renderArgs.put("topRatedUsers", topRatedUsers);

        
        render();

    }
 

    public static void signup()  {

        render();
        
    }

    public static void signup(String status) {

        render(status);

    }




    public static final void saveUser(
            @Required(message = "Email is required") String email,
            @Required(message = "Firstname is required") String firstname,
            @Required(message = "Lastname is required") String lastname,
            @Required(message = "Password is required") String password) throws MailException {
    	
    		validation.email(email);
    		validation.match(password, "^.*(?=.*[a-z])(?=.*[A-Z])(?=.*[\\d]).*$").message("Password must contaion at lest one uppercase char,lowercase char and digit.");;
    		validation.minSize(password, 6).message("Password length must be between 6 and 12 character.");
    		validation.maxSize(password, 12).message("Password length must be between 6 and 12 character.");
    		
        if (validation.hasErrors()) {

        	
            System.out.println("validation error!");

            params.flash();
            validation.keep();

            signup();

        }
        

        System.out.println("Registering...");

        //  New Registration
        Registration registration = Registration.create(email, firstname, lastname, password);

        //  Persist
        DAO.saveRegistration(registration);

        //  Send Registration Email
        MailUtil.sendRegistrationEmail(registration);


        flash.success(
                "Thank you %s,\n\nPlease check your mail inbox for the confirmation.",
                registration.getFirstname().concat(" ").concat(registration.getLastname())
        );


        signup();


    }

    public static void login(String email, String password) {

        User user = DAO.login(email, password);

        if (user == null) {

            flash.error("No User Found! Please Check your credentials!");

            index();

        }else {

            session.put("userid",       user.getId());
            session.put("username",     user.getRegistration().getFirstname().concat(" ").concat(user.getRegistration().getLastname()));
            
            renderArgs.put("user",      user);

            index();

        }


        
    }

    public static final void complete(String reg) {

        //  Activate Registration and create User
        RegistrationController.activateRegistration(reg);

        //  Say Welcome to the new user
        MailUtil.sayThankyouForRegistration(RegistrationController.getRegistration(reg).getEmail());
        
        flash.success(
                "Your account is now activated. Please feel free to login and Give 2 Get!"
        );

        render();

    }


    public static final void logout() {

        session.clear();
        index();

    }

    

    public static final void about() {
        render();
    }

    public static final void contact() {
        render();
    }

    public static final void terms() {
        render();
    }

    
    public static final void search(String keyword,String provider,String date) {
    		
        //   Search Results
        List<Service> services = DAO.getSearchResults(keyword,provider,date);

        render(services, keyword,provider,date);
    }

    public static final void profile(String message) {

        if (session.get("userid") != null) {
            
            int userId = Integer.valueOf(session.get("userid")).intValue();

            String username = session.get("username");
            

            // Basic Info
            User user = DAO.getUserDetailedInfo(userId);


            //  Posted Services
            List<Service> userProvidedServices = DAO.getUserPostedServices(userId);

            //  Posted Rolling Services
            List<Service> providedRollingServices = DAO.getUserOnRollServices(userId);

            System.out.println(providedRollingServices.size());


            //  Latest Comments
            List<Comment> userComments = DAO.getUserLatestComments(userId);


            //  My Requested Services
            List<Service> requestedServices = ServiceDAO.getRequestedServices(userId);

            String postedServiceOnRollError  = "Error!!";

            if (message != null) {                
                renderArgs.put("profile_status",    message);
            }


            //  Unreated Services
            List<ServiceProgress> unratedServicesProgresses  = ServiceDAO

            render(user, username, userProvidedServices, userComments, requestedServices, providedRollingServices, postedServiceOnRollError);

        }

        index();

    }

    public static final void profile() {

        System.out.println("here!");

        profile(null);
        
    }
    
    public static final void editProfile() {

        if (session.get("userid") != null) {

            System.out.println("profileEdit()");

            int userId = Integer.valueOf(session.get("userid")).intValue();
            System.out.println("userId=" + userId);

            String username = session.get("username");
            System.out.println(username);


            //   Rating Info

            // Basic Info
            User user = DAO.getUserDetailedInfo(userId);


            //  Posted Services
            List<Service> userProvidedServices = DAO.getUserPostedServices(userId);


            //  Latest Comments
            List<Comment> userComments = DAO.getUserLatestComments(userId);


            render(user, username, userProvidedServices, userComments);

        }

        index();
    }
    
    public static final void completeEditProfile(String email,String firstName,String lastName) {

        if (session.get("userid") != null) {

            System.out.println("completeProfileEdit()");

            int userId = Integer.valueOf(session.get("userid")).intValue();
            System.out.println("userId=" + userId);

            String username = session.get("username");
            System.out.println(username);

            DAO.completeEditProfile(userId,email,firstName,lastName);
            System.out.println("userId:"+userId+ " email:"+email+" firstname:"+firstName+" lastname:"+lastName);
            profile();

        }

        index();
    }

    public static final void user(int id) {

        if (session.get("userid") == null) {

            index();

        }

        if (id == Integer.parseInt(session.get("userid"))) {

            Application.profile();

        }

        //  Basic Info
        User user = DAO.getUserDetailedInfo(id);

        //  Services
        List<Service> userServices = DAO.getUserPostedServices(id);

        
        render(user, userServices);

    }


  
}