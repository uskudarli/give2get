package controllers;

import play.data.validation.Required;
import play.mvc.Before;
import play.mvc.Controller;
import models.Message;
import models.User;
import com.boun.give2get.db.DAO;
import com.boun.give2get.db.MessageDAO;

import java.util.List;

/**.
 * User: orcunguducu
 * Date: Dec 24, 2011
 */
public class MessageController extends Controller {

	@Before
    public static void addUser() {

        User user = Application.isUserConnected();

        if (user != null) {

            renderArgs.put("user", user);

        }

    }
	
	 public static final void sendMessageView(int receiverId) {

	        if (session.get("userid") == null || receiverId == Integer.parseInt(session.get("userid"))) {

	        	Application.index();
	        }

	        //  Basic Info
	        User user = DAO.getUserDetailedInfo(receiverId);
	        
	        render(user);

	    }
	    
	    public static final void sendMessage(int receiverId,
	    		@Required(message = "Title is required") String title,
	            @Required(message = "Message is required") String message) {

	    		int senderId = Integer.parseInt(session.get("userid"));
	        if (session.get("userid") == null || receiverId == senderId ) {

	        	Application.index();
	        }
	        
	        if (validation.hasErrors()) {

	        	
	            System.out.println("validation error!");

	            params.flash();
	            validation.keep();

	            sendMessageView(receiverId);

	        }
	        
	        Message messageObj = new Message(receiverId,title,message);
	        //  Basic Info
	        MessageDAO.sendMessage(Integer.parseInt(session.get("userid")),messageObj);
	        
	        Application.index(); 
	    }
	    
	    public static final void inbox(int userId) {

	        if (session.get("userid") == null || userId != Integer.parseInt(session.get("userid"))) {

	        	Application.index();
	        }

	        List<Message> inboxMessages = MessageDAO.getMessages(userId);
	        
	        render(inboxMessages);
	        
	    }
	        
	    public static final void getMessageDetail(int userId) {
	    	
	        if (session.get("userid") == null || userId == Integer.parseInt(session.get("userid"))) {

	        	Application.index();
	        }

	        List<Message> inboxMessages = MessageDAO.getMessageDetails(userId,Integer.parseInt(session.get("userid")));
	        
	        render(inboxMessages);

	    }
	    
	   
	    
}
