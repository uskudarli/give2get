package controllers;

import play.mvc.Controller;
import play.mvc.Before;
import models.User;
import models.Service;
import models.ServiceRating;
import com.boun.give2get.db.DAO;
import com.boun.give2get.db.ServiceDAO;
import com.boun.give2get.core.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 11, 2011
 * Time: 5:01:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class Rating extends Controller {

    @Before
    public static void addUser() {

        User user = Application.isUserConnected();

        if (user != null) {

            renderArgs.put("user", user);

        }

    }

    public static void rate(String sId, String c, String t) {      

        //  check code
        //  TODO


        //  Service Detail
        Service service = DAO.getServiceDetail(Integer.parseInt(sId));


        //  Title
        String ratingTitle;

        if (Integer.parseInt(t) == 2) {

            //  consumer is rating the service
            
            ratingTitle = Messages.get("title.rateprovider");

            int provider_id = service.getProviderId();

            render(service, ratingTitle, c, t, provider_id);

        } else {

            //  provider is rating for his consumer

            User consumer = DAO.getUser(service.getRequesterId());                        

            ratingTitle = String.format(Messages.get("title.rateconsumer"), new String[]{service.getTitle(), consumer.getRegistration().getFullName()});
                        
            render(service, ratingTitle, consumer, c, t);

        }


    }

    public static void giveRating(String code, int serviceId, int t, int rating_int, String comment, int consumer_id, int provider_id) {

        System.out.println(code);
        System.out.println(serviceId);
        System.out.println(t);
        System.out.println(rating_int);
        System.out.println(comment);
        System.out.println("consumerId=" + consumer_id);


        //  Create Rating
        int userId = Integer.parseInt(session.get("userid"));

        ServiceRating rating = new ServiceRating(code, rating_int, serviceId, userId, comment);

        if (t == 2) {

            //  consumer is rating the service
            ServiceDAO.giveConsumerRating(rating, provider_id);

        } else {

            Service service = DAO.getServiceDetail(serviceId);

            //  provider is rating the consumer
            ServiceDAO.giveProviderRating(rating, consumer_id);

        }

        Application.index();

                           
    }
}
