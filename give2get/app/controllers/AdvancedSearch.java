package controllers;

import play.mvc.Controller;
import play.mvc.Before;
import models.User;
import models.Service;

import java.util.List;

import com.boun.give2get.db.ServiceDAO;
import com.boun.give2get.db.SearchDAO;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 19, 2011
 * Time: 1:55:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class AdvancedSearch extends Controller {

    @Before
    public static void addUser() {

        User user = Application.isUserConnected();

        if (user != null) {

            renderArgs.put("user", user);

        }

    }

    public static void index() {

        System.out.println("here!");

        render();
        
    }

    public static void search(String keyword, boolean searchInTitle, boolean searchInDescription) {

        System.out.println(keyword);
        System.out.println(searchInTitle);
        System.out.println(searchInDescription);

        if (!searchInTitle  && !searchInDescription) {

            searchInTitle           = true;
            searchInDescription     = true;
            
        }

        List<Service> services = SearchDAO.advanceSearch(keyword, searchInTitle, searchInDescription);

        render(services, keyword);                   
        
    }
}
