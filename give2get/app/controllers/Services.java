package controllers;

import play.mvc.Controller;
import play.mvc.Before;
import models.*;
import com.boun.give2get.db.DAO;
import com.boun.give2get.db.MessageDAO;
import com.boun.give2get.db.ServiceDAO;
import com.boun.give2get.db.SearchDAO;
import com.boun.give2get.mail.MailUtil;
import com.boun.give2get.exceptions.MailException;
import com.boun.give2get.core.Messages;
import com.boun.give2get.core.Cache;

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
            int unreadMessageCount = MessageDAO.getUnreadMessageCount(user.getId());
            int unreadNotificationCount = MessageDAO.getUnreadNotificationCount(user.getId());
            renderArgs.put("unreadNotificationCount", unreadNotificationCount);
            renderArgs.put("unreadMessageCount", unreadMessageCount);
        }

    } 

    public static final void post() {

        if (session.get("userid") == null) {

            Application.index();

        }

        List<Sehir> sehirler = Cache.getSehirler();

        render(sehirler);
        
    }

    public static void getIlceler(int sehirId) {
       
        List<Ilce> ilceler = SearchDAO.getIlceler(sehirId);

        renderJSON(ilceler);

    }

    public static void getSemtler(int ilceId) {

        List<Semt> semtler = SearchDAO.getSemtler(ilceId);

        renderJSON(semtler);

    }

    public static void getSehirler() {

        renderJSON(Cache.getSehirler());
    }


    public static final void postNewService(
            String title,
            String description,
            String startdate,
            String enddate,
            int city,
            int ilce,
            int semt,
            String fromDay,
            String toDay,
            String betweenFrom,
            String betweenTo) {

        validation.required(title);
        validation.required(description);
        

        if (startdate.length() != 0) {

            startdate = startdate.substring(startdate.lastIndexOf("/") + 1) + "-" +
                    startdate.substring(0, startdate.indexOf("/")) + "-" +
                    startdate.substring(3, 5);
            
        }

        if (enddate.length() != 0) {

            enddate = enddate.substring(enddate.lastIndexOf("/") + 1) + "-" +
                    enddate.substring(0, enddate.indexOf("/")) + "-" +
                    enddate.substring(3, 5);

        }

        //  todo

        /*if (validation.hasErrors()) {

            System.out.println("validation errors!");

            params.flash();
            validation.keep();           

            post();

        } */


        //  post it
        Service service = new Service(title, description, startdate, enddate, city, ilce, semt, fromDay, toDay, betweenFrom, betweenTo);

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
        List<Tag> tags = service.getTags();

        render(service, comments, tags, userId);

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
        
        
        DAO.completeEditService(service,userId,serviceId);
        
        service(Integer.parseInt(serviceId));
    }
    
    public static final void newComment(int serviceId, String title) {
        render(serviceId, title);
    }

    public static final void newTag(int serviceId, String title) {
        render(serviceId, title);
    }

    public static final void postComment(int serviceId, String content) {
        DAO.postNewComment(serviceId, Integer.valueOf(session.get("userid")).intValue(), content);
        List<Comment> comments = DAO.getServiceComments(serviceId);

        renderJSON(comments);
    }

    public static final void postTag(int serviceId, String content) {
        DAO.postNewTag(serviceId, Integer.valueOf(session.get("userid")).intValue(), content);
        List<Tag> tags = DAO.getServiceTags(serviceId);

        renderJSON(tags);
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
        

        boolean userEligible = ServiceDAO.isUserEligibleForNewRequests(userId);
                

        if (userEligible) {

            //  Create new Service Request
            ServiceDAO.makeServiceRequest(serviceId, providerId, userId);


            //  Inform provider about the new request
            User provider = DAO.getUser(providerId);

            User crntUser  = renderArgs.get("user", User.class);
           
            User requester = DAO.getUser(userId);
            
            String notification = requester.getRegistration().getFullName()+ " has requested your service: " + serviceTitle;
            MessageController.sendNotification(providerId, notification);
            
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

        //  Service Details
        Service service = DAO.getServiceDetail(serviceId);

        //  Service Requests
        List<ServiceRequest> serviceRequests = ServiceDAO.getServiceRequests(serviceId);


        //  Service Comments
        List<Comment> comments = DAO.getServiceComments(serviceId);

        //  Service Tags
        List<Tag> tags = DAO.getServiceTags(serviceId);

        render(service, serviceRequests, comments, tags);

    }

    public static final void acceptConsumer(int requesterId, String requesterEmail, String requesterFullName, int serviceId, int providerId, String serviceTitle, String providerFullName) throws MailException {
        
       
        //  bunch of actions in one transaction
        ServiceDAO.acceptConsumer(requesterId, serviceId, providerId);


        //  inform requester
        MailUtil.informRequester(requesterEmail, serviceTitle, providerFullName, requesterFullName);


        //  Initiate new service progress
        int serviceRequestId = ServiceDAO.getServiceRequestId(serviceId, providerId, requesterId);        

        ServiceDAO.initializeServiceProgress(ServiceProgress.createNew(serviceRequestId));


        //  Increment user providedCount
        DAO.incrementUserProvidedCount(providerId);


        //  Return to profile
        Application.profile();

    }

    public static final void resolve(int serviceId, int requesterId, int providerId, String serviceTitle, String requesterFullName) throws MailException {

        /*int requestId = ServiceDAO.getServiceRequestId(serviceId, providerId, requesterId);

        System.out.println("requestId=" + requestId);*/
       

        //  bunch of updates
        String serviceProgressCode = ServiceDAO.resolve(serviceId, providerId, requesterId);        


        User provider = DAO.getUser(providerId);
        User consumer = DAO.getUser(requesterId);
        

        //  mail provider about ServiceRating
        MailUtil.sendProviderRatingMail(serviceProgressCode, serviceId, serviceTitle, requesterFullName, provider.getEmail(), provider.getRegistration().getFullName());


        //  mail consumer about ServiceRating
        MailUtil.sendConsumerRatingMail(serviceProgressCode, serviceId, consumer.getEmail(), consumer.getRegistration().getFullName(), serviceTitle);


        String status = Messages.get("info.service.resolved");

        Application.profile(status);


    }

}
