package models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import com.boun.give2get.db.DAO;

/**
 * User: orcunguducu
 * Date: Nov 24, 2011
 */
public final class Message extends ListableModel{

    private int userId;
    private String title;
    private String message;  
    private String firstName;
    private String lastName;
    private String sendDate;
    private int	status;
    private String replyStatus;
    private int messageId;
    private String sendByWho;

    public Message () {

    }
    
    public Message (int userId,String title,String message) {

    	this.userId = userId;
    	this.title = title;
    	this.message = message;
    }
    
    public static Message getMessages(ResultSet rs, int index) throws SQLException {
    	Message message = new Message();
    	
    	message.userId       	= rs.getInt("userId");
    	message.title			= rs.getString("title");
    	message.message			= rs.getString("message");
    	message.sendDate		= rs.getString("sendDate");
    	message.firstName 		= rs.getString("firstname");
    	message.lastName 		= rs.getString("lastname");
    	message.status 			= rs.getInt("status");
    	message.messageId       = rs.getInt("messageId");
    	message.replyStatus 	= rs.getString("replystatus");
    	
    	message.oddOrEven       = index %2 == 0 ? "odd" :"even";
    	
		return message;
    }
    
    public static Message getMessage(ResultSet rs, int index) throws SQLException {
    	Message message = new Message();
    	
    	message.userId       	= rs.getInt("userId");
    	message.title			= rs.getString("title");
    	message.message			= rs.getString("message");
    	message.sendDate		= rs.getString("sendDate");
    	message.firstName 		= rs.getString("firstname");
    	message.lastName 		= rs.getString("lastname");
    	message.sendByWho		= rs.getString("sendByUs");
    	
    	message.oddOrEven       = index %2 == 0 ? "odd" :"even";
    	
		return message;
    }
    
    
    public int getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }
    
    public String getSendDate() {
        return sendDate;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getLastName() {
        return lastName;
    }

    @Override
    public String toString() {
        return "Activity{" +
                ", userId=" + userId +
                ", title=" + title +
                ", message=" + message +
                ", sendDate=" + sendDate +
                ", firstName=" + firstName +
                ", lastName=" + lastName +
                ", status=" + status +
                ", replyStatus" + replyStatus +
                ", messageId" + messageId +
                '}';
    }
}
