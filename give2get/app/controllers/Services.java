package controllers;

import play.mvc.Controller;
import play.mvc.Before;
import play.data.validation.Required;
import models.User;
import models.Service;
import models.Comment;
import com.boun.give2get.db.DAO;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 8, 2011
 * Time: 4:23:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class Services extends Controller {

    @Before
    public static void addUser() {

        User user = Application.isUserConnected();

        if (user != null) {

            renderArgs.put("user", user);
            
        }

    } 

    public static final void post() {

        if (session.get("userid") == null) {

            Application.index();

        }
                 
        render();
        
    }


    public static final void postNewService(
            String title,
            String description,
            String startdate,
            String enddate,
            String duration,
            String location) {

        validation.required(title);
        validation.required(description);
        validation.required(startdate);
        validation.required(enddate);
        validation.required(duration);
        validation.required(location);

        if (validation.hasErrors()) {

            params.flash();
            validation.keep();

            post();
        }

        //  post it
        Service service = new Service(title, description, startdate, enddate, duration, location, "1");

        System.out.println(service.toString());

        DAO.postNewService(service, Integer.valueOf(session.get("userid")).intValue());

        flash.success(
                "You've just posted a new service! Congratulations! "               
        );

        post();

    }
    
    public static final void list() {

         if (session.get("userid") == null) {

            Application.index();

        }


        //  Last 50 Services for now..
        List<Service> services = DAO.getServices(Integer.valueOf(session.get("userid")).intValue());       

        render(services);
        
    }

    public static final void service(int id) {

        if (session.get("userid") == null) {

            Application.index();           

        }

        int userId = Integer.parseInt(session.get("userid"));
        Service service  = DAO.getServiceDetail(id);

        List<Comment> comments = service.getComments();

        render(service, comments,userId);

    }
    
    public static final void editService(int id) {

        if (session.get("userid") == null) {

            Application.index();           

        }
        
        int userId = Integer.parseInt(session.get("userid"));
        
        Service service  = DAO.getServiceDetail(id);
        
        if (userId != service.getProviderId()) {

            Application.index();           

        }
        render(service,id);
    }


    public static final void completeEditService(
    		String title,
            String description,
            String startdate,
            String enddate,
            String duration,
            String location,
            String serviceId,
            String period) {

        if (session.get("userid") == null) {

            Application.index();           

        }
        
        String userId = session.get("userid");
        
        Service service = new Service(title, description, startdate, enddate, duration, location, period);
        
        System.out.println("title:"+service.getTitle());
        
        DAO.completeEditService(service,userId,serviceId);
        
        service(Integer.parseInt(serviceId));
    }
    
    public static final void newComment(int serviceId, String title) {

        render(serviceId, title);
        
    }

    public static final void postComment(int serviceId, String content) {


        DAO.postNewComment(serviceId, Integer.valueOf(session.get("userid")).intValue(), content);

        Services.service(serviceId);
        
    }
}
