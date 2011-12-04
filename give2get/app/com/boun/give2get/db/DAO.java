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

import play.db.DB;
import models.*;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
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

    private static void rollback(Connection conn) {

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

        try {

            conn = getConnection();

            //  Control registration
            pstmt = conn.prepareStatement("SELECT u.id as userId, r.* FROM users u, registrations r WHERE u.id = ?");

            pstmt.setInt(1, userId);

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

    public static final User getUserDetailedInfo(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        User user                       = null;

        try {

            conn = getConnection();

            //  Control registration
            pstmt = conn.prepareStatement("SELECT u.id as userId, r.*, COUNT(c.id) as numOfComments FROM users u, registrations r, comments c WHERE u.id = ? AND c.user_id = u.id AND u.reg_id = r.id");

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

            pstmt = conn.prepareStatement("SELECT s.*,r.* FROM services s,registrations r,users u WHERE " +
            							  "s.provider_id = u.id and u.reg_id = r.id and " +
            							  "(s.title like '%"+keyword+"%' or s.description like '%"+keyword+"%') and " +
            							  "r.firstname like '%" +provider+ "%'" + dateSql);
            

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
            
            System.out.println(e);
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
            pstmt = conn.prepareStatement("SELECT u.id as userId, r.* FROM users u, registrations r WHERE u.reg_id = r.id AND r.email = ? AND r.password = ?");

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
            pstmt = conn.prepareStatement("SELECT u.id as userId, r.firstname, r.lastname FROM users u, registrations r WHERE u.id = ?");

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

            System.out.println("username=" + userName);

            if (provideServiceId) {

                String serviceName = getServiceFullName(conn, serviceId);

                System.out.println("serviceName=" + serviceName);

                pstmt.setString(3,  ActivityType.getText(type, userName, serviceName));
                pstmt.setInt(4,     serviceId);

            } else {

                pstmt.setString(3,      ActivityType.getText(type, userName));
            }


            pstmt.executeUpdate();

            System.out.println("Log completed!");


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
            pstmt = conn.prepareStatement("SELECT title from services WHERE id = ?");

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

            pstmt = conn.prepareStatement("INSERT INTO services (provider_id, title, description, location, period, duration, start, end) VALUES (?,?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS);


            pstmt.setInt(1,         userId);
            pstmt.setString(2,      service.getTitle());
            pstmt.setString(3,      service.getDescription());
            pstmt.setString(4,      service.getLocation());
            pstmt.setString(5,      service.getPeriod());
            pstmt.setString(6,      service.getDuration());
            pstmt.setString(7,      service.getStartDate());
            pstmt.setString(8,      service.getEndDate());


            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();

            int serviceId = -1;

            while(rs.next()) {
                serviceId = rs.getInt(1);
            }

            if (serviceId == -1) throw new Exception("Can't store the service! Sorry!");

            System.out.println("serviceId=" + serviceId);


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

    public static final List<Activity> getRecentActivities() {
                
        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Activity> activities;

        try {

            conn = getConnection();

            //  Control registration
            pstmt = conn.prepareStatement("SELECT u.id,r.firstName,r.lastName,a.type,a.service_id,a.created " +
            		"FROM activities a,users u,registrations r WHERE u.id = a.user_id and " +
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

    public static final List<TopRatedService> getTopRatedServices() {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<TopRatedService> topRatedServices;

        try {

            conn = getConnection();

            //  Control registration
            pstmt = conn.prepareStatement("SELECT id, provider_id, title, viewcount FROM services ORDER BY viewcount DESC LIMIT 5");

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

            System.out.println("userIdddd:"+userId);
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

            System.out.println("userIdddd:"+userId);
            pstmt = conn.prepareStatement("UPDATE services SET title='"+service.getTitle()+"', " +
            				"description='"+service.getDescription()+"', location='"+service.getLocation()+"', " +
            				"duration='"+service.getDuration()+"' WHERE id='"+serviceId+"' and " +
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
    
    
    public static final List<Service> getUserPostedServices(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Service> services          = null;

        try {

            conn = getConnection();

            //  Control registration
            pstmt = conn.prepareStatement("SELECT id, title, created, status FROM services WHERE provider_id = ? ORDER BY created DESC LIMIT 15");

            pstmt.setInt(1, userId);

            rs = pstmt.executeQuery();

            services = new ArrayList<Service>();


            Service service;

            int index = 0;

            while (rs.next()) {

                service = Service.newMyService(rs, index);

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

    public static final List<Service> getServices(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Service> services          = null;

        try {

            conn = getConnection();

            pstmt = conn.prepareStatement("SELECT s.id, s.title, s.created, s.status, s.provider_id, r.firstname, r.lastname FROM services s, registrations r, users u " +
                    "WHERE s.provider_id != ? AND s.status = ? AND r.id = u.reg_id AND u.id = s.provider_id " +
                    "ORDER BY s.created DESC LIMIT 50");

            pstmt.setInt(1,     userId);
            pstmt.setString(2,  "WAITING_FOR_REQUESTS");
            
            rs = pstmt.executeQuery();

            services = new ArrayList<Service>();


            Service service;

            int index = 0;

            while (rs.next()) {

                service = Service.newProvidedService(rs, index);

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
            
            pstmt = conn.prepareStatement("select s.*, r.firstname, r.lastname from services s, registrations r, users u WHERE s.id = ? AND r.id = u.reg_id AND u.id = s.provider_id LIMIT 1");

            pstmt.setInt(1, serviceId);

            rs = pstmt.executeQuery();


            while (rs.next()) {

                service = Service.newDetailedService(rs);     

                service.setComments(getServiceComments(conn, serviceId));

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

            pstmt = conn.prepareStatement("UPDATE services SET viewCount = ? WHERE id = ?");

            pstmt.setInt(1, service.getViewCount() + 1);
            pstmt.setInt(2, service.getId());

            pstmt.executeUpdate();           


        } catch (Exception e) {

            log.warn(e);

            rollback(conn);

            throw new DataStoreException(e);

        } finally {

            close(pstmt);

        }


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

    public static final List<Comment> getUserLatestComments(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Comment> comments          = null;

        try {

            conn = getConnection();
           
            pstmt = conn.prepareStatement("SELECT c.*, s.title FROM comments c, services s WHERE c.service_id = s.id AND c.user_id = ? ORDER BY c.created DESC LIMIT 5");

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
