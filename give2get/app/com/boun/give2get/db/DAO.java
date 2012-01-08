package com.boun.give2get.db;

import org.apache.log4j.Logger;

import javax.sql.DataSource;
import javax.naming.InitialContext;

import com.boun.give2get.core.Configuration;
import com.boun.give2get.registration.RegistrationStatus;
import com.boun.give2get.exceptions.DataStoreException;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;

import play.db.DB;
import models.*;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas,orcunguducu
 * Date: Nov 8, 2011
 * Time: 9:45:33 PM
 * To change this template use File | Settings | File Templates.
 */
public final class DAO {

    private static final Logger log = Logger.getLogger(DAO.class);

    private static DataSource dataSource;

    private static final Object numberOfConnectionsLock = new Object();
    private static Integer numberOfConnections;

    public static void init() {

        try {

            log.info("Initializing DAO...");

            //dataSource = (DataSource) new InitialContext().lookup(Configuration.getInstance().getDataSource());

            dataSource = DB.datasource;

            numberOfConnections = 0;

            if (areYouOK()) log.info("DAO is OK.");
            else log.info("DAO is SICK.");

            log.info("Done.");

        } catch (Exception e) {

            throw new DataStoreException(e);

        }

    }

    public static void destroy() {
        
        if (numberOfConnections != 0) {

            log.warn("DAO dirty: " + numberOfConnections);

        } else {

            log.info("DAO clean.");

        }

    }

    public static boolean areYouOK() {

        try {

            close(getConnection());

            return true;

        } catch (Exception e) {

            log.warn("health check error", e);

            return false;

        }

    }

    public static Connection getConnection() {

        try {

            Connection conn = dataSource.getConnection();

            conn.setAutoCommit(false);

            synchronized (numberOfConnectionsLock) {
                numberOfConnections++;
                log.warn("numberOfConnections=" + numberOfConnections);
            }

            return conn;

        } catch (SQLException e) {

            synchronized (numberOfConnectionsLock) {
                log.warn("numberOfConnections=" + numberOfConnections);
            }

            throw new DataStoreException(e);

        }

    }

    public static void rollback(Connection conn) {

        try {

            if (conn != null) conn.rollback();

        } catch (Exception e) {
            log.warn("rollback failed", e);
        }

    }

    public static void close(Connection conn) {

        try {

            if (conn != null) {

                conn.close();

                synchronized (numberOfConnectionsLock) {
                    numberOfConnections--;
                    log.warn("numberOfConnections=" + numberOfConnections);
                }

            }

        } catch (Exception e) {

            log.warn("close failed", e);

        }

    }

    public static void close(ResultSet rset) {

        try {

            if (rset != null) rset.close();

        } catch (Exception e) {

            log.warn("rset.close failed", e);

        }

    }

    public static void close(Statement stmt) {

        try {

            if (stmt != null) stmt.close();

        } catch (Exception e) {

            log.warn("stmt.close failed", e);

        }

    }

    public static void close(PreparedStatement pstmt) {

        try {

            if (pstmt != null) pstmt.close();

        } catch (Exception e) {

            log.warn("pstmt.close failed", e);

        }

    }

    public static boolean isEmailAlreadyRegistered(String email) {

        Connection conn             = null;
        PreparedStatement   pstmt   = null;
        ResultSet rs                = null;

        int count = 0;
        try {

            conn = getConnection();

            pstmt = conn.prepareStatement("SELECT count(id) FROM registrations where email = ?");

            pstmt.setString(1, email);

            rs = pstmt.executeQuery();

            while (rs.next()) {

                count = rs.getInt(1);

            }

            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);
        }

        return count > 0;

    }

    public static final void saveRegistration(Registration registration) {

        Connection conn             = null;
        PreparedStatement pstmt     = null;
        ResultSet rs                = null;
        
        try {

            conn = getConnection();


            //  Create Registration
            pstmt = conn.prepareStatement("INSERT INTO registrations (code, email, firstname, lastname, password, created) VALUES (?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1,      registration.getCode());
            pstmt.setString(2,      registration.getEmail());
            pstmt.setString(3,      registration.getFirstname());
            pstmt.setString(4,      registration.getLastname());
            pstmt.setString(5,      registration.getPassword());
            pstmt.setTimestamp(6,   new Timestamp(System.currentTimeMillis()));

            pstmt.executeUpdate();

            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);

        }

    }

    public static final int createNewUser(Connection conn, String regCode) {

        PreparedStatement pstmt     = null;
        ResultSet rs                = null;
        ResultSet rsUserId          = null;

        int userId = -1;

        try {

            //  Get registration Id
            pstmt = conn.prepareStatement("SELECT id FROM registrations WHERE code = ? ");

            pstmt.setString(1, regCode);

            rs = pstmt.executeQuery();

            int regId = -1;

            while (rs.next()) {

                regId = rs.getInt("id");

            }

            if (regId == -1) throw new DataStoreException("No Registration Found with regCode= " + regCode);




            //  Create new user
            pstmt = conn.prepareStatement("INSERT INTO users (reg_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1,             regId);

            pstmt.executeUpdate();

            rsUserId = pstmt.getGeneratedKeys();

            while(rsUserId.next()) {

                userId = rsUserId.getInt(1);

            }


            //  Log Activity
            logActivity(conn, ActivityType.NEW_USER, userId, -1);
            

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(rsUserId);
            close(pstmt);            

        }

        return userId;

    }

    public static Registration getUnActivatedRegistration(String code) {

        Connection conn             = null;
        PreparedStatement   pstmt   = null;
        ResultSet rs                = null;

        Registration registration = null;
        try {

            conn = getConnection();

            pstmt = conn.prepareStatement("SELECT id, code, email, created FROM registrations where code = ? AND status =?");

            pstmt.setString(1,  code);
            pstmt.setString(2, RegistrationStatus.NOT_ACTIVATED.toString());

            rs = pstmt.executeQuery();

            while (rs.next()) {

                registration = Registration.create(rs);

            }

            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);
        }

        return registration;

    }

    public static final Registration getRegistration(String code) {

        Connection conn             = null;
        PreparedStatement   pstmt   = null;
        ResultSet rs                = null;

        Registration registration   = null;

        try {

            conn = getConnection();

            pstmt = conn.prepareStatement("SELECT * FROM registrations where code = ?");

            pstmt.setString(1,  code);


            rs = pstmt.executeQuery();

            while (rs.next()) {

                registration = Registration.create(rs);

            }

            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);
        }

        return registration;

    }


    public static void activateRegistration(String regCode) {

        Connection conn             = null;
        PreparedStatement   pstmt   = null;
        ResultSet rs                = null;       

        try {

            conn = getConnection();
            
            //  Activate Registration
            pstmt = conn.prepareStatement("UPDATE registrations SET status = ?, activated = ? WHERE code = ?");

            pstmt.setString(1,          RegistrationStatus.ACTIVATED.toString());
            pstmt.setTimestamp(2,       new Timestamp(System.currentTimeMillis()));
            pstmt.setString(3,          regCode);

            pstmt.executeUpdate();


            //  Create User
            createNewUser(conn, regCode);            

            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);
        }

    }


    public static final User getUser(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        User user                       = null;

        System.out.println("getting user for " + userId);

        try {

            conn = getConnection();

            //  Control registration
            pstmt = conn.prepareStatement("SELECT u.id as userId, u.credits, u.rating, r.* FROM users u, registrations r WHERE u.id = ? AND r.id =u.reg_id");

            pstmt.setInt(1, userId);

            rs = pstmt.executeQuery();


            while (rs.next()) {

                user = new User(rs);

            }

            System.out.println(user.toString());

            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);
        }

        return user;

    }

    public static final User getUserDetailedInfo(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        User user                       = null;

        try {

            conn = getConnection();

            //  Control registration
            pstmt = conn.prepareStatement("SELECT u.id as userId, r.*, COUNT(c.id) as numOfComments, u.rating, u.credits FROM users u, registrations r, comments c WHERE u.id = ? AND c.user_id = u.id AND u.reg_id = r.id");

            pstmt.setInt(1, userId);
            

            rs = pstmt.executeQuery();


            while (rs.next()) {

                user = new User(rs);

                user.setNumOfComments(rs.getInt("numOfComments"));

                user.setNumOfPostedServices(getUserProvidedServiceCount(conn, userId));

            }

            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);
        }

        return user;

    }

    public static final int getUserProvidedServiceCount(Connection conn, int userId) {

        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        int count                       = 0;

        try {

            pstmt = conn.prepareStatement("SELECT COUNT(id) FROM services WHERE provider_id = ?");

            pstmt.setInt(1, userId);

            rs = pstmt.executeQuery();


            while (rs.next()) {

                count = rs.getInt(1);

            }

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);

        }

        return count;

    }
    
    public static final List<Service> getSearchResults(String keyword,String provider,String date) {
    	Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;
        
        String dateSql 					= "";
        
        if(provider == null)
        	provider = "";        

        if(date != null && !date.isEmpty() && !date.trim().isEmpty())
        {
        	dateSql  = " and '" + date.substring(6,10)+'.'+date.substring(3, 5) +'.'+ date.substring(0,2) + "' between s.start and s.end";
        }
        	
        	
    	List<Service> services;
    	
    	try {

            conn = getConnection();

            /*pstmt = conn.prepareStatement("SELECT s.id AS SERVICE_ID, s.created, s.status, s.provider_id, s.viewCount,r.*,i.* " +
                    "FROM services s,registrations r,users u, serviceInfos i WHERE " +
            							  "s.provider_id = u.id and u.reg_id = r.id and " +
            							  "(i.title like '%"+keyword+"%' or i.description like '%"+keyword+"%') and " +
            							  "r.firstname like '%" +provider+ "%'" + dateSql);*/
            
            pstmt = conn.prepareStatement("SELECT D.service_id, D.title, D.description, D.provider_id, D.created," +
            		"D.status,D.start,D.end,D.viewCount,D.NumberOfViews, D.PROVIDER_ID2, D.Rating, " +
            		"D.NumberOfPostedService, D.NumberOfRatedServices, D.firstname, D.lastname, D.fullname," +
            		"CASE /*WHEN NumberOfRatedServices=0 OR MAXRATE=0 OR MAXPOST=0 OR MAXVIEW=0 THEN " +
            		"convert(int, 1  100* RAND(CHECKSUM(NEWID())))*/ WHEN NumberOfRatedServices>0 THEN " +
            		"CEILING(((Rating-MINRATE)*100/(MAXRATE-MINRATE))*0.40 + " +
            		"((MAXPOST-NumberOfPostedService)*100/(MAXPOST-MINPOST))*0.15 +" +
            		"((MAXVIEW-NumberOfViews)*100/(MAXVIEW-MINVIEW))*0.15 +" +
            		"((NumberOfRatedServices-MIN_NO_OF_RATED)*100/(MAX_NO_OF_RATED-MIN_NO_OF_RATED))*0.30) END AS 'FINAL_RATE' " +
            		"FROM ( SELECT " +
            		"MAX(A.Rating) AS 'MAXRATE', MIN(A.Rating) AS 'MINRATE',  MAX(A.NumberOfPostedService) AS 'MAXPOST'," +
            		"MIN(A.NumberOfPostedService) AS 'MINPOST', MAX(A.NumberOfViews) AS 'MAXVIEW'," +
            		" MIN(A.NumberOfViews) AS 'MINVIEW',MAX(A.NumberOfRatedServices) AS 'MAX_NO_OF_RATED'," +
            		"MIN(A.NumberOfRatedServices) AS 'MIN_NO_OF_RATED' from ( " +
            		"SELECT s.viewCount as 'NumberOfViews', u.rating as 'Rating', " +
            		"u.postedCount as 'NumberOfPostedService', u.providedCount as 'NumberOfRatedServices' FROM  " +
            		"services s INNER JOIN users u ON u.id = s.provider_id)A ) B CROSS JOIN (" +
            		"SELECT s.status,s.viewCount,s.start,s.end,i.service_id, i.title, i.description, s.provider_id, s.created, " +
            		"s.viewCount as 'NumberOfViews', u.id as PROVIDER_ID2, u.rating as 'Rating', " +
            		"u.postedCount as 'NumberOfPostedService', u.providedCount as 'NumberOfRatedServices', " +
            		"r.firstname, r.lastname, concat(r.firstname,r.lastname) as fullname FROM serviceInfos i INNER JOIN services s ON s.id = i.service_id " +
            		"INNER JOIN users u ON u.id = s.provider_id INNER JOIN registrations r ON u.reg_id = r.id) D " +
            		"WHERE title like '%"+keyword+"%' and description like '%"+keyword+"%' and " +
            		"fullname like '%"+provider+"%' ORDER BY FINAL_RATE DESC");
            rs = pstmt.executeQuery();
            services = new ArrayList<Service>();
            int index = 0;
            Service service;
            while (rs.next()) {

                service = Service.getService(rs,index);
                services.add(service);
                index++;
            }
            conn.commit();  
            
        }catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);
        }
    	
    	return services;
    	
    }

    public static final User login(String email, String password) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;
    
        User user                       = null;

        try {

            conn = getConnection();

            //  Control registration
            pstmt = conn.prepareStatement("SELECT u.id as userId, u.rating, u.credits, r.* FROM users u, registrations r WHERE u.reg_id = r.id AND r.email = ? AND r.password = ?");

            pstmt.setString(1,      email);
            pstmt.setString(2,      password);


            rs = pstmt.executeQuery();


            while (rs.next()) {

                user = new User(rs);

            }

            conn.commit();                        

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);
        }

        return user;

    }

    public static String getUserFullName(Connection conn, int userId) {

        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        String userFullName             = null;

        try {
            
            //  Control registration
            pstmt = conn.prepareStatement("SELECT u.id as userId, r.firstname, r.lastname FROM users u, registrations r WHERE u.id = ? and u.reg_id = r.id");

            pstmt.setInt(1,      userId);


            rs = pstmt.executeQuery();


            while (rs.next()) {

                userFullName = rs.getString("firstname").concat(" ").concat(rs.getString("lastname"));

            }


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);

        }

        return userFullName;

    }

    public static void logActivity(Connection conn, ActivityType type, int userId, int serviceId) {

        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        try {            

            boolean provideServiceId = serviceId != -1;
            
            if (!provideServiceId)
                pstmt = conn.prepareStatement("INSERT INTO activities (user_id, type, text) VALUES (?,?,?)");
            else
                pstmt = conn.prepareStatement("INSERT INTO activities (user_id, type, text, service_id) VALUES (?,?,?,?)");

            pstmt.setInt(1,         userId);
            pstmt.setString(2,      type.toString());


            String userName = getUserFullName(conn, userId);


            if (provideServiceId) {

                String serviceName = getServiceFullName(conn, serviceId);

                pstmt.setString(3,  ActivityType.getText(type, userName, serviceName));
                pstmt.setInt(4,     serviceId);

            } else {

                pstmt.setString(3,      ActivityType.getText(type, userName));
            }


            pstmt.executeUpdate();


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);            
        }

        
    }

    public static final String getServiceFullName(Connection conn, int serviceId) {

        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        String serviceName              = null;

        try {

            //  Control registration
            pstmt = conn.prepareStatement("SELECT title from serviceInfos WHERE service_id = ?");

            pstmt.setInt(1,      serviceId);

            rs = pstmt.executeQuery();


            while (rs.next()) {

                serviceName = rs.getString("title");

            }


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);

        }

        return serviceName;

    }


    public static final void postNewService(Service service, int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;


        try {

            conn = getConnection();

            //  Create Service
            pstmt = conn.prepareStatement("INSERT INTO services (provider_id) VALUES (?)", Statement.RETURN_GENERATED_KEYS);


            pstmt.setInt(1,         userId);
            
            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();

            int serviceId = -1;

            while(rs.next()) {
                serviceId = rs.getInt(1);
            }


            boolean startDateExists     = service.getStartDate().length() != 0;
            boolean endDateExists       = service.getEndDate().length() != 0;
            boolean sehirInfoExists     = service.getSehirId() != 0;
            boolean ilceInfoExists      = service.getIlceId() != 0;
            boolean semtInfoExists      = service.getSemtId() != 0;

            String insert = "INSERT INTO serviceInfos (service_id, title, description, fromDay, toDay, fromTime, toTime " +
                    (startDateExists ? ", start" : "") +
                    (endDateExists ? ", end" : "") +
                    (sehirInfoExists ? ", sehir_id" : "") +
                    (ilceInfoExists ? ", ilce_id" : "") +
                    (semtInfoExists ? ", semt_id" : "") +
                    ") " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?" +
                    (startDateExists ? ", ?" : "") +
                    (endDateExists ? ", ?" : "") +
                    (sehirInfoExists ? ", ?" :"") +
                    (ilceInfoExists ? ", ?" : "") +
                    (semtInfoExists ? ", ?" : "") +
                    ")";


            //  Create User Info
            pstmt = conn.prepareStatement(insert);

            pstmt.setInt(1,         serviceId);
            pstmt.setString(2,      service.getTitle());
            pstmt.setString(3,      service.getDescription());

            pstmt.setString(4,      service.getFromDay());
            pstmt.setString(5,      service.getToDay());

            pstmt.setString(6,      service.getFromTime());
            pstmt.setString(7,      service.getToTime());

            int index = 8;

            if (startDateExists) {

                pstmt.setString(index,  service.getStartDate());

                index ++;
            }

            if (endDateExists) {

                pstmt.setString(index,      service.getEndDate());

                index++;
            }

            if (sehirInfoExists) {

                pstmt.setInt(index,     service.getSehirId());
                index++;

            }

            if (ilceInfoExists) {

                pstmt.setInt(index,     service.getIlceId());
                index++;

            }

            if (semtInfoExists) {

                pstmt.setInt(index,     service.getSemtId());
                index++;

            }

          
            pstmt.executeUpdate();


            //  Increment user's postedCount            
            incrementUserPostedCount(conn, userId);
        

            //  Log Activity
            logActivity(conn, ActivityType.NEW_SERVICE, userId, serviceId);

            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);
        }

    }

    public static final void incrementUserProvidedCount(int userId) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;

        try {

            conn = DAO.getConnection();

            String sql = "UPDATE users SET providedCount = providedCount+ 1, postedCount = postedCount - 1 WHERE id = ?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, userId);

            pstmt.executeUpdate();

            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(pstmt);
            close(conn);

        }

    }

    private static final void incrementUserPostedCount(Connection conn, int userId) {

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            String sql = "UPDATE users SET postedCount = postedCount + 1 WHERE id = ?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, userId);

            pstmt.executeUpdate();            

        } catch (Exception e) {

            log.warn(e);

            throw new DataStoreException(e);

        } finally {

            close(pstmt);

        }


    }
    
    
    
    public static final void addSkills(Queue<Skill> skills) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;


        try {

            conn = getConnection();

            while(!skills.isEmpty()) {
            	
            	Skill skill = skills.poll();
            	
	            pstmt = conn.prepareStatement("INSERT INTO skills (title, description,user_id) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS);
	
	
	            pstmt.setString(1,         skill.getTitle());
	            pstmt.setString(2,      skill.getDescription());
	            pstmt.setInt(3,      skill.getUserId());
	
	
	            pstmt.executeUpdate();
	            
	            rs = pstmt.getGeneratedKeys();
	            
	            int skillId = -1;
	
	            while(rs.next()) {
	                skillId = rs.getInt(1);
	            }
	
	            if (skillId == -1) throw new Exception("Can't store the skills! Sorry!");
            }
            
            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);
        }

    }

    public static final void postNewComment(int serviceId, int userId, String content) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;


        try {

            conn = getConnection();

            pstmt = conn.prepareStatement("INSERT INTO comments (text, user_id, service_id) VALUES (?,?,?)");


            pstmt.setString(1,          content);
            pstmt.setInt(2,             userId);
            pstmt.setInt(3,             serviceId);


            pstmt.executeUpdate();


            //  Log Activity
            logActivity(conn, ActivityType.NEW_COMMENT, userId, serviceId);
                     

            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);
        }


    }



    public static final void postNewTag(int serviceId, int userId, String content) {
        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        try {
            conn = getConnection();

            pstmt = conn.prepareStatement("INSERT INTO tags (text, user_id, service_id) VALUES (?,?,?)");

            pstmt.setString(1,          content);
            pstmt.setInt(2,             userId);
            pstmt.setInt(3,             serviceId);

            pstmt.executeUpdate();

            //  Log Activity
            logActivity(conn, ActivityType.NEW_TAG, userId, serviceId);
            conn.commit();

        } catch (Exception e) {
            log.warn(e);
            rollback(conn);
            throw new DataStoreException(e);

        } finally {
            close(rs);
            close(pstmt);
            close(conn);
        }
    }




    public static final List<Activity> getRecentActivities() {
                
        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Activity> activities;

        try {

            conn = getConnection();

            //  Control registration
            pstmt = conn.prepareStatement("SELECT u.id, r.firstName, r.lastName, a.type, a.service_id, a.created " +
            		"FROM activities a, users u, registrations r WHERE u.id = a.user_id and " +
            		"u.reg_id = r.id ORDER BY CREATED DESC LIMIT 15");

            rs = pstmt.executeQuery();

            activities  = new ArrayList<Activity>();


            Activity activitiy;

            int index = 0;

            while (rs.next()) {

                activitiy = new Activity(rs, index);

                activities.add(activitiy);

                index++;              

            }


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);

        }

        return activities;


    }

    public static final List<TopRatedUser> getTopRatedUsers() {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<TopRatedUser> topRatedUsers;

        try {

            conn = getConnection();
           
            pstmt = conn.prepareStatement("SELECT u.id, u.rating, r.firstname, r.lastname FROM users u, registrations r WHERE u.reg_id = r.id ORDER BY u.rating DESC LIMIT 5");

            rs = pstmt.executeQuery();

            topRatedUsers  = new ArrayList<TopRatedUser>();

            TopRatedUser topRatedUser;

            int index = 0;

            while (rs.next()) {

                topRatedUser = new TopRatedUser(rs, index);

                topRatedUsers.add(topRatedUser);

                index ++;

            }


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);

        }

        return topRatedUsers;

    }

    public static final List<TopRatedService> getTopRatedServices() {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<TopRatedService> topRatedServices;

        try {

            conn = getConnection();

            //  Control registration
            pstmt = conn.prepareStatement("SELECT s.id, s.provider_id, i.title, s.viewcount FROM services s, serviceInfos i WHERE i.service_id = s.id ORDER BY s.viewcount DESC LIMIT 5");

            rs = pstmt.executeQuery();

            topRatedServices  = new ArrayList<TopRatedService>();


            TopRatedService topRatedService;

            int index = 0;

            while (rs.next()) {

                topRatedService = new TopRatedService(rs, index);

                topRatedServices.add(topRatedService);

                index ++;

            }


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);

        }

        return topRatedServices;

    }

    public static final void completeEditProfile(int userId,String email,String firstName,String lastName) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;

        try {

            conn = getConnection();

            pstmt = conn.prepareStatement("UPDATE registrations r,users u SET r.email='"+email+"', r.firstname='"+firstName+"', r.lastname='"+lastName+"' WHERE u.id="+userId+" and u.reg_id = r.id;");

            pstmt.executeUpdate();
            conn.commit();
            
        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {
            close(pstmt);
            close(conn);

        }

    }
    
    public static final void completeEditService(Service service,String userId,String serviceId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;

        try {

            conn = getConnection();

            pstmt = conn.prepareStatement("UPDATE services SET title='"+service.getTitle()+"', " +
            				"description='"+service.getDescription() +
            				"WHERE id='"+serviceId+"' and " +
            				"provider_id = '"+userId+"';");

            pstmt.executeUpdate();
            conn.commit();
            
        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {
            close(pstmt);
            close(conn);

        }
    }

    public static final List<Service> getUserOnRollServices(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Service> services;

        try {

            conn = getConnection();                        

            String sql = new StringBuilder().
                    append("SELECT s.id, i.title, r.firstname, r.lastname, u.id as requester_id, s.status, s.provider_id  ").
                    append("FROM services s, registrations r, users u, serviceInfos i ").
                    append("WHERE s.provider_id = ? AND s.status = ? AND r.id = u.reg_id AND u.id = s.requester_id AND i.service_id = s.id").
                    toString();

            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1,     userId);
            pstmt.setString(2,  ServiceStatus.WAITING_TO_BE_RESOLVED.name());

            rs = pstmt.executeQuery();

            services = new ArrayList<Service>();

            Service service;

            int index = 0;

            while(rs.next()) {

                service = Service.newOnRollService(rs, index);

                services.add(service);

                index ++;
                
            }


            return services;


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);

        }

        

    }
    
    
    public static final List<Service> getUserPostedServices(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Service> services          = null;

        try {

            conn = getConnection();
                          
            
            String sql = new StringBuilder().
                    append("SELECT s.id, s.created, s.status, i.title FROM services s, serviceInfos i ").
                    append("WHERE s.provider_id = ? AND status = ? AND i.service_id = s.id ").
                    append("ORDER BY s.created DESC LIMIT 15").                            
                    toString();


            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1,     userId);
            pstmt.setString(2,  ServiceStatus.WAITING_FOR_REQUESTS.name());

            rs = pstmt.executeQuery();

            services = new ArrayList<Service>();


            Service service;

            int index = 0;

            while (rs.next()) {

                service = Service.newMyService(rs, index);

                service.setRequestCount(getServiceCurrentRequestCount(conn, service.getId()));
                service.setProviderId(userId);

                services.add(service);

                index ++;

            }


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);

        }

        return services;

    }
    
    public static final List<Skill> getUserSkills(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Skill> skills          = null;

        try {

            conn = getConnection();

            //  Control registration
            pstmt = conn.prepareStatement("SELECT title,description FROM skills WHERE user_id = ? LIMIT 15");

            pstmt.setInt(1, userId);

            rs = pstmt.executeQuery();

            skills = new ArrayList<Skill>();

            Skill skill;

            int index = 0;

            while (rs.next()) {

                skill = new Skill(rs, userId, index);

                skills.add(skill);

                index ++;

            }


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);

        }

        return skills;

    }

    public static final int getServiceCurrentRequestCount(Connection conn, int serviceId) {

        PreparedStatement pstmt     = null;
        ResultSet rs                = null;

        int count = 0;

        try {

            pstmt = conn.prepareStatement("SELECT COUNT(id) FROM serviceRequests WHERE service_id = ?");

            pstmt.setInt(1, serviceId);

            rs = pstmt.executeQuery();

            while(rs.next()) {

                count = rs.getInt(1);
                
            }

        } catch(Exception e) {

            log.warn(e,e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
        }

        return count;

    }

    public static final List<Service> getServices(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Service> services          = null;

        try {

            conn = getConnection();

            pstmt = conn.prepareStatement("SELECT s.id, s.created, s.status, s.provider_id, r.firstname, r.lastname, i.title FROM services s, registrations r, users u, serviceInfos i " +
                    "WHERE s.provider_id != ? AND s.status = ? AND r.id = u.reg_id AND u.id = s.provider_id AND i.service_id = s.id " +
                    "ORDER BY s.created DESC LIMIT 50");

            pstmt.setInt(1,     userId);
            pstmt.setString(2,  "WAITING_FOR_REQUESTS");
            
            rs = pstmt.executeQuery();

            services = new ArrayList<Service>();


            Service service;

            int index = 0;

            while (rs.next()) {
                                              
                service = Service.newProvidedService(rs, index);

                service.setRequestCount(getServiceCurrentRequestCount(conn, service.getId()));

                services.add(service);

                index ++;

            }


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);

        }

        return services;

    }

    public static final Service getServiceDetail(int serviceId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        Service service                 = null;

        try {

            conn = getConnection();

            String sql = "select s.id AS SERVICE_ID, s.created, s.provider_id, s.requester_id, s.status, s.viewCount, i.*, r.firstname, r.lastname, il.name AS SEHIR, ilc.name AS ILCE, sem.name AS SEMT " +
                    "from services s, registrations r, users u, serviceInfos i " +
                    "LEFT JOIN iller il ON il.id = i.sehir_id " +
                    "LEFT JOIN ilceler ilc ON ilc.id = i.ilce_id " +
                    "LEFT JOIN semtler sem ON sem.id = i.semt_id " +
                    "WHERE s.id = ? AND r.id = u.reg_id AND u.id = s.provider_id AND i.service_id = s.id LIMIT 1";
            
            /*pstmt = conn.prepareStatement("select s.created, s.provider_id, s.requester_id, s.status, s.viewCount, i.*, r.firstname, r.lastname, il.name AS SEHIR, ilc.name AS ILCE, sem.name AS SEMT " +
                    "from services s, registrations r, users u, serviceInfos i, iller il, ilceler ilc, semtler sem " +
                    "WHERE s.id = ? AND r.id = u.reg_id AND u.id = s.provider_id AND i.service_id = s.id AND il.id = i.sehir_id AND ilc.id = i.ilce_id AND sem.id = i.semt_id LIMIT 1");
              */

            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, serviceId);

            rs = pstmt.executeQuery();


            while (rs.next()) {
                
                service = Service.newDetailedService(rs);

                service.setComments(getServiceComments(conn, serviceId));
                service.setTags(getServiceTags(conn, serviceId));
               
            }



            //  Increment View count
            incrementViewCount(conn, service);            

            conn.commit();


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);

        }

        return service;

    }

    public static final void incrementViewCount(Connection conn, Service service) {

        PreparedStatement pstmt         = null;

        try {

            pstmt = conn.prepareStatement("UPDATE services SET viewCount = viewCount+1 WHERE id = ?");            

            pstmt.setInt(1, service.getId());

            pstmt.executeUpdate();            

        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(pstmt);

        }


    }

    public static final List<Comment> getServiceComments(int serviceId) {

        Connection conn = null;

        List<Comment> comments = null;

        try {

            conn = DAO.getConnection();

            comments = getServiceComments(conn, serviceId);

        } catch (Exception e){

            log.warn(e,e);

        } finally {

            close(conn);
        }

        return comments;

    }



    public static final List<Tag> getServiceTags(int serviceId) {
        Connection conn = null;
        List<Tag> tags = null;

        try {
            conn = DAO.getConnection();
            tags = getServiceTags(conn, serviceId);

        } catch (Exception e){
            log.warn(e,e);

        } finally {
            close(conn);
        }

        return tags;
    }



    public static final List<Comment> getServiceComments(Connection conn, int serviceId) {

        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Comment> comments          = null;

        try {

            pstmt = conn.prepareStatement("SELECT c.id, c.text, c.created, c.user_id, r.firstname, r.lastname FROM comments c, registrations r, users u WHERE r.id = u.reg_id AND u.id = c.user_id AND c.service_id = ? ORDER BY c.created DESC LIMIT 30");

            pstmt.setInt(1, serviceId);

            rs = pstmt.executeQuery();

            comments = new ArrayList<Comment>();

            Comment comment;

            int index = 0;

            while (rs.next()) {

                comment = Comment.newServiceComment(rs, index);

                comments.add(comment);

                index ++;

            }


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);            

        }

        return comments;


    }





    public static final List<Tag> getServiceTags(Connection conn, int serviceId) {

        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Tag> tags          = null;

        try {

            pstmt = conn.prepareStatement("SELECT c.id, c.text, c.created, c.user_id, r.firstname, r.lastname FROM tags c, registrations r, users u WHERE r.id = u.reg_id AND u.id = c.user_id AND c.service_id = ? ORDER BY c.created DESC LIMIT 30");

            pstmt.setInt(1, serviceId);

            rs = pstmt.executeQuery();

            tags = new ArrayList<Tag>();

            Tag tag;

            int index = 0;

            while (rs.next()) {

                tag = Tag.newServiceTag(rs, index);

                tags.add(tag);

                index ++;

            }


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);            

        }

        return tags;


    }








    public static final List<Comment> getUserLatestComments(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Comment> comments          = null;

        try {

            conn = getConnection();
           
            pstmt = conn.prepareStatement("SELECT c.*, i.title FROM comments c, serviceInfos i WHERE c.service_id = i.id AND c.user_id = ? ORDER BY c.created DESC LIMIT 5");

            pstmt.setInt(1, userId);

            rs = pstmt.executeQuery();

            comments = new ArrayList<Comment>();


            Comment comment;

            int index = 0;

            while (rs.next()) {

                comment = new Comment(rs, index);

                comments.add(comment);

                index ++;

            }


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(rs);
            close(pstmt);
            close(conn);

        }

        return comments;


    }
    

}
