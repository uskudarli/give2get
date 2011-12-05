package controllers;

import play.mvc.Controller;
import play.mvc.Before;
import play.data.validation.Required;
import models.*;
import com.boun.give2get.db.DAO;
import com.boun.give2get.db.ServiceDAO;
import com.boun.give2get.mail.MailUtil;
import com.boun.give2get.exceptions.MailException;
import com.boun.give2get.core.Messages;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas,orcunguducu
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

    public static final void list(String message) {

        if (session.get("userid") == null) {

            Application.index();

        }

        //  Last 50 Services for now..
        List<Service> services = DAO.getServices(Integer.valueOf(session.get("userid")).intValue());

        System.out.println("messages=" + message);

        if (message != null)
            flash.error(message);

        render(services);

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

    public static final void unRequest(int serviceId) {

        int userId = Integer.parseInt(session.get("userid"));

        boolean requestCancelable = ServiceDAO.isServiceRequestCancellable(serviceId, userId);

        if (requestCancelable) {

            ServiceDAO.unrequest(serviceId, userId);

            Application.profile();

        } else {

            flash.error(Messages.get("error.canceltoolate"));
            
            Application.profile();
            
            
        }
        
    }


    public static final void request(int serviceId, int providerId, String serviceTitle) throws MailException {

        int userId = Integer.parseInt(session.get("userid"));
        System.out.println("userId=" + userId);

        System.out.println(serviceId);
        System.out.println(providerId);
        System.out.println(serviceTitle);


        boolean userEligible = ServiceDAO.isUserEligibleForNewRequests(userId);
                

        if (userEligible) {

            //  Create new Service Request
            ServiceDAO.makeServiceRequest(serviceId, providerId, userId);


            //  Inform provider about the new request
            User provider = DAO.getUser(providerId);

            User crntUser  = renderArgs.get("user", User.class);
           
            MailUtil.informServiceProvider(
                    provider.getRegistration().getFullName(),
                    provider.getEmail(),
                    serviceTitle,
                    crntUser.getRegistration().getFullName()
            );

            String message = Messages.get("info.request.success");

            Services.list(message);
            

        } else {

            Services.list(Messages.get("error.insufficientcredits"));

        }

    }


    public static final void requesters(int serviceId) {

        System.out.println(serviceId);                

        //  Service Details
        Service service = DAO.getServiceDetail(serviceId);

        //  Service Requests
        List<ServiceRequest> serviceRequests = ServiceDAO.getServiceRequests(serviceId);


        //  Service Comments
        List<Comment> comments = DAO.getServiceComments(serviceId);

        render(service, serviceRequests, comments);

    }

    public static final void acceptConsumer(int requesterId, String requesterEmail, String requesterFullName, int serviceId, int providerId, String serviceTitle, String providerFullName) throws MailException {
        
        System.out.println(requesterId);
        System.out.println(serviceId);
        System.out.println(providerId);
        System.out.println(requesterEmail);
        System.out.println(requesterFullName);


        //  bunch of actions in one transaction
        ServiceDAO.acceptConsumer(requesterId, serviceId, providerId);


        //  inform requester
        MailUtil.informRequester(requesterEmail, serviceTitle, providerFullName, requesterFullName);


        //  Initiate new service progress
        int serviceRequestId = ServiceDAO.getServiceRequestId(serviceId, providerId, requesterId);

        System.out.println("serviceRequestId=" + serviceRequestId);

        ServiceDAO.initializeServiceProgress(ServiceProgress.createNew(serviceRequestId));


        //  Return to profile
        Application.profile();

    }

    public static final void resolve(int serviceId, int requesterId, int providerId) {

        /*int requestId = ServiceDAO.getServiceRequestId(serviceId, providerId, requesterId);

        System.out.println("requestId=" + requestId);*/

        System.out.println("seviceId=" + serviceId);
        System.out.println("requesterId=" + requesterId);
        System.out.println("providerId= "+ providerId);


        //  bunch of updates
        String serviceProgressCode = ServiceDAO.resolve(serviceId, providerId, requesterId);

        System.out.println("progressCode=" + serviceProgressCode);



        //  mail provider

        //  mail consumer




        String status = Messages.get("info.service.resolved");

        Application.profile(status);


    }

}
