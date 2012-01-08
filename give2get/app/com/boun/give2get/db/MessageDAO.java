package com.boun.give2get.db;

import models.*;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;


import javax.mail.Session;

import com.boun.give2get.exceptions.DataStoreException;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 19, 2011
 * Time: 9:18:42 AM
 * To change this template use File | Settings | File Templates.
 */
public final class MessageDAO {

    private static final Logger log = Logger.getLogger(MessageDAO.class);

    public static final void sendMessage(int userId, Message message) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        Timestamp time;
        
        Calendar calendar = Calendar.getInstance();

        Date now = (Date) calendar.getTime();
        
        time = new Timestamp(now.getTime());
        
        try {

            conn = DAO.getConnection();


            pstmt = conn.prepareStatement("INSERT INTO messages (senderId,receiverId, message, sendDate,status,type) VALUES (?,?,?,?,1,1)", Statement.RETURN_GENERATED_KEYS);


            pstmt.setInt(1, userId);
            pstmt.setInt(2, message.getUserId());
            pstmt.setString(3, message.getMessage());
            pstmt.setTimestamp(4,time);


            pstmt.executeUpdate();
            
            rs = pstmt.getGeneratedKeys();
            
            int skillId = -1;

            while(rs.next()) {
                skillId = rs.getInt(1);
            }

            if (skillId == -1) throw new Exception("Can't send the message! Sorry!");
        
            
            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

        	DAO.close(rs);
        	DAO. close(pstmt);
        	DAO.close(conn);
        }

    }
    
    public static final void sendNotification(Message message) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        Timestamp time;
        
        Calendar calendar = Calendar.getInstance();

        Date now = (Date) calendar.getTime();
        
        time = new Timestamp(now.getTime());
        
        try {

            conn = DAO.getConnection();


            pstmt = conn.prepareStatement("INSERT INTO messages (senderId,receiverId, message, sendDate,status,type) VALUES (0,?,?,?,1,2)", Statement.RETURN_GENERATED_KEYS);


            pstmt.setInt(1, message.getUserId());
            pstmt.setString(2, message.getMessage());
            pstmt.setTimestamp(3,time);


            pstmt.executeUpdate();
            
            rs = pstmt.getGeneratedKeys();
            
            int skillId = -1;

            while(rs.next()) {
                skillId = rs.getInt(1);
            }

            if (skillId == -1) throw new Exception("Can't send the notification! Sorry!");
        
            
            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

        	DAO.close(rs);
        	DAO. close(pstmt);
        	DAO.close(conn);
        }

    }
    
    public static final List<Message> getMessages(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Message> messages          = null;

        try {

            conn = DAO.getConnection();

            pstmt = conn.prepareStatement("SELECT m.userId,r.firstname,r.lastname,message,sendDate," +
            		"CASE WHEN senderId = ? THEN 'replied' ELSE 'notreplied' END as replystatus," +
            		"(select case when count(*) > 0 THEN 0 ELSE 1 END from messages where senderId = m.userId and receiverId = ? and status = 0) as status" +
            		",m.messageId FROM (SELECT * FROM (SELECT DISTINCT CASE WHEN m.senderId <> ? THEN senderId ELSE m.receiverId END as userId," +
            		"senderId,receiverId,messageId,message,sendDate,status FROM messages m " +
            		"WHERE (m.receiverId = ? or m.senderId = ?) and type = 1 ORDER BY messageId desc) T GROUP BY userId ) m," +
            		"registrations r,users u WHERE m.userId=u.id and u.reg_id = r.id order by sendDate desc");
            
            pstmt.setInt(1,     userId);
            pstmt.setInt(2,     userId);
            pstmt.setInt(3,     userId);
            pstmt.setInt(4,     userId);
            pstmt.setInt(5,     userId);
            
            rs = pstmt.executeQuery();

            messages = new ArrayList<Message>();


            Message message;

            int index = 0;

            while (rs.next()) {
                                              
                message = Message.getMessages(rs, index);

                messages.add(message);

                index ++;

            }


        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

        	DAO.close(rs);
        	DAO.close(pstmt);
        	DAO.close(conn);

        }

        return messages;

    }
    
    public static final Integer getUnreadMessageCount(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        int countUnreadMessage = 0;
        
        try {

            conn = DAO.getConnection();

            pstmt = conn.prepareStatement("SELECT COUNT(*) as count FROM messages m WHERE receiverId = ? and type=1 and status = 0;");

            pstmt.setInt(1,     userId);
            
            rs = pstmt.executeQuery();

            

            while (rs.next()) {
                                              
            	countUnreadMessage = rs.getInt("count");
            }


        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

        	DAO.close(rs);
        	DAO.close(pstmt);
        	DAO.close(conn);

        }

        return countUnreadMessage;
    }
    
    public static final Integer getUnreadNotificationCount(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        int countUnreadNotification = 0;
        
        try {

            conn = DAO.getConnection();

            pstmt = conn.prepareStatement("SELECT COUNT(*) as count FROM messages m WHERE receiverId = ? and type=2 and status = 0;");

            pstmt.setInt(1,     userId);
            
            rs = pstmt.executeQuery();

            

            while (rs.next()) {
                                              
            	countUnreadNotification = rs.getInt("count");
            }


        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

        	DAO.close(rs);
        	DAO.close(pstmt);
        	DAO.close(conn);

        }

        return countUnreadNotification;
    }
    
    
    
    
    public static final List<Message> getMessageDetails(int userId,int loginUserId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Message> messages          = null;

        try {

            conn = DAO.getConnection();

            pstmt = conn.prepareStatement("SELECT s.*,senderId,receiverId FROM messages s WHERE ((s.senderId = ? and receiverId = ?)) or " +
            		"(s.senderId = ? and receiverId = ?) ORDER BY sendDate desc");

            pstmt.setInt(1,     userId);
            pstmt.setInt(2, 	loginUserId);
            pstmt.setInt(3,     loginUserId);
            pstmt.setInt(4,     userId);
            
            rs = pstmt.executeQuery();

            messages = new ArrayList<Message>();


            Message message;

            int index = 0;

            while (rs.next()) {
                                              
                message = Message.getMessage(rs, index);

                messages.add(message);

                index ++;
            }
            
            updateMessageStatus(loginUserId,userId);

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

        	DAO.close(rs);
        	DAO.close(pstmt);
        	DAO.close(conn);

        }

        return messages;

    }
    
    
    //Update message status to 1 if message is read
    private static final void updateMessageStatus(int receiverId,int senderId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;


        try {

            conn = DAO.getConnection();


            pstmt = conn.prepareStatement("UPDATE messages m SET m.status = 1 WHERE receiverId = ? and senderId = ?");


            pstmt.setInt(1, receiverId);
            pstmt.setInt(2, senderId);

            pstmt.executeUpdate();
            
            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

        	DAO.close(rs);
        	DAO. close(pstmt);
        	DAO.close(conn);
        }

    }
    
    public static final void deleteMessageOfAUser(int receiverId,int senderId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;


        try {

            conn = DAO.getConnection();


            pstmt = conn.prepareStatement("DELETE FROM messages WHERE (receiverId = ? AND senderId = ?) or " +
            		"(receiverId = ? AND senderId = ?)");


            pstmt.setInt(1, receiverId);
            pstmt.setInt(2, senderId);
            pstmt.setInt(3, senderId);
            pstmt.setInt(4, receiverId);

            pstmt.executeUpdate();
            
            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

        	DAO.close(rs);
        	DAO. close(pstmt);
        	DAO.close(conn);
        }

    }
    
    public static final void unreadMessage(int senderId,int loginUserId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        int maxMessageId = getMaxMessageId(senderId, loginUserId);
        try {

            conn = DAO.getConnection();


            pstmt = conn.prepareStatement("UPDATE messages SET status=0 WHERE messageId= ?");


            pstmt.setInt(1, maxMessageId);

            pstmt.executeUpdate();
            
            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

        	DAO.close(rs);
        	DAO. close(pstmt);
        	DAO.close(conn);
        }

    }
    
    
    public static final Integer getMaxMessageId(int userId,int loginUserId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        int maxMessageId = 0;

        try {

            conn = DAO.getConnection();

            pstmt = conn.prepareStatement("SELECT max(messageId) as maxMessageId FROM messages WHERE " +
            		"receiverId = ? and senderId = ?");

            pstmt.setInt(1,     loginUserId);
            pstmt.setInt(2, 	userId);
            
            rs = pstmt.executeQuery();

            while (rs.next()) {
                                              
            	maxMessageId = rs.getInt("maxMessageId");
            	
            }
            

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

        	DAO.close(rs);
        	DAO.close(pstmt);
        	DAO.close(conn);

        }

        return maxMessageId;

    }


}
