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
            int unreadMessageCount = MessageDAO.getUnreadMessageCount(user.getId());
            int unreadNotificationCount = MessageDAO.getUnreadNotificationCount(user.getId());
            renderArgs.put("unreadNotificationCount", unreadNotificationCount);
            renderArgs.put("unreadMessageCount", unreadMessageCount);
        }

    }
	
	 public static final void sendMessageView(int receiverId) {

	        if (session.get("userid") == null || receiverId == Integer.parseInt(session.get("userid"))) {

	        	Application.index();
	        }

	        //  Basic Info
	        User receiverUser = DAO.getUserDetailedInfo(receiverId);
	        render(receiverUser);

	    }
	    
	    public static final void sendMessage(int receiverId,
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
	        
	        Message messageObj = new Message(receiverId,message);
	        //  Basic Info
	        MessageDAO.sendMessage(Integer.parseInt(session.get("userid")),messageObj);
	        
	        Application.index(); 
	    }
	    
	    public static final void sendNotification(int receiverId,String message) {
	        
	        Message messageObj = new Message(receiverId,message);
	        //  Basic Info
	        MessageDAO.sendNotification(messageObj);
	        
	        Application.index(); 
	    }
	    
	    public static final void replyMessage(int receiverId,
	            @Required(message = "Message is required") String message) {

	    		
	        if (session.get("userid") == null || receiverId == Integer.parseInt(session.get("userid")) ) {

	        	Application.index();
	        }
	        
	        if (validation.hasErrors()) {

	        	
	            System.out.println("validation error!");

	            params.flash();
	            validation.keep();

	            getMessageDetail(receiverId);

	        }
	        
	        Message messageObj = new Message(receiverId,message);
	        //  Basic Info
	        MessageDAO.sendMessage(Integer.parseInt(session.get("userid")),messageObj);
	        
	        getMessageDetail(receiverId); 
	    }
	    
	    public static final void inbox(int userId) {

	        if (session.get("userid") == null || userId != Integer.parseInt(session.get("userid"))) {

	        	Application.index();
	        }

	        List<Message> inboxMessages = MessageDAO.getMessages(userId);
	        int countUnreadMessage = MessageDAO.getUnreadMessageCount(userId);
	        render(inboxMessages,countUnreadMessage);
	        
	    }
	        
	    public static final void getMessageDetail(int userId) {
	    	
	        if (session.get("userid") == null || userId == Integer.parseInt(session.get("userid"))) {

	        	Application.index();
	        }

	        List<Message> messageDetails = MessageDAO.getMessageDetails(userId,Integer.parseInt(session.get("userid")));
	        
	        renderArgs.put("senderFullName", DAO.getUser(userId).getRegistration().getFullName());
	        renderArgs.put("receiverFullName", DAO.getUser(Integer.parseInt(session.get("userid"))).getRegistration().getFullName());
	        
	        int countUnreadMessage = MessageDAO.getUnreadMessageCount(Integer.parseInt(session.get("userid")));
	        
	        render(messageDetails,userId,countUnreadMessage);
	    }
	    
	    public static final void getNotificationDetail() {

	        List<Message> messageDetails = MessageDAO.getMessageDetails(0,Integer.parseInt(session.get("userid")));
	        
	        renderArgs.put("senderFullName", "System");
	        renderArgs.put("receiverFullName", DAO.getUser(Integer.parseInt(session.get("userid"))).getRegistration().getFullName());
	        
	        int countUnreadMessage = MessageDAO.getUnreadMessageCount(Integer.parseInt(session.get("userid")));
	        
	        render(messageDetails,0,countUnreadMessage);
	    }
	    
	    public static final void deleteMessage(int userId) {
	    	
	        if (session.get("userid") == null || userId == Integer.parseInt(session.get("userid"))) {

	        	Application.index();
	        }

	        MessageDAO.deleteMessageOfAUser(userId, Integer.parseInt(session.get("userid")));
	        
	        inbox(Integer.parseInt(session.get("userid")));

	    }
	    
	    public static final void unreadMessage(int userId) {
	    	
	        if (session.get("userid") == null || userId == Integer.parseInt(session.get("userid"))) {

	        	Application.index();
	        }

	        MessageDAO.unreadMessage(userId, Integer.parseInt(session.get("userid")));
	        
	        inbox(Integer.parseInt(session.get("userid")));

	    }
	    
	   
	    
}
