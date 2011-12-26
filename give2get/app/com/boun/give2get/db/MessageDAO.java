package com.boun.give2get.db;

import models.*;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


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


        try {

            conn = DAO.getConnection();


            pstmt = conn.prepareStatement("INSERT INTO messages (senderId,receiverId,title, message) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);


            pstmt.setInt(1, userId);
            pstmt.setInt(2, message.getUserId());
            pstmt.setString(3, message.getTitle());
            pstmt.setString(4, message.getMessage());


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
    
    public static final List<Message> getMessages(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Message> messages          = null;

        try {

            conn = DAO.getConnection();

            pstmt = conn.prepareStatement("SELECT m.userId,r.firstname,r.lastname,title,message,sendDate," +
            		"CASE WHEN senderId = ? THEN 'replied' ELSE 'notreplied' END as replystatus,m.status,m.messageId FROM " +
            		"(SELECT DISTINCT CASE WHEN m.senderId <> ? THEN senderId ELSE m.receiverId END as userId," +
            		"senderId,receiverId,max(messageId) as messageId,title,message,sendDate,status FROM messages m " +
            		"WHERE m.receiverId = ? or m.senderId = ? GROUP BY userId) m," +
            		"registrations r,users u WHERE m.userId=u.id and u.reg_id = r.id order by sendDate desc");

            pstmt.setInt(1,     userId);
            pstmt.setInt(2,     userId);
            pstmt.setInt(3,     userId);
            pstmt.setInt(4,     userId);
            
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
    
    public static final List<Message> getMessageDetails(int userId,int loginUserId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Message> messages          = null;

        try {

            conn = DAO.getConnection();

            pstmt = conn.prepareStatement("SELECT s.*,CASE WHEN senderId = ? THEN 'sendToUs' " +
            		"ELSE 'sendByUs' END AS sendByWho FROM messages s WHERE ((s.senderId = ? and receiverId = ?)) or " +
            		"(s.senderId = ? and receiverId = ?) ORDER BY sendDate desc");

            pstmt.setInt(1,     userId);
            pstmt.setInt(2,     userId);
            pstmt.setInt(3, 	loginUserId);
            pstmt.setInt(4,     loginUserId);
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


}
