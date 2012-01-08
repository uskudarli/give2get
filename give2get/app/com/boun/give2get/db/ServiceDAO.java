package com.boun.give2get.db;

import com.boun.give2get.exceptions.DataStoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import models.*;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 4, 2011
 * Time: 1:52:30 PM
 * To change this template use File | Settings | File Templates.
 */
public final class ServiceDAO {

    private static final Logger log = Logger.getLogger(ServiceDAO.class);

    public static final boolean isServiceRequestCancellable(int serviceId, int userId) {

        Connection conn             = null;
        PreparedStatement pstmt     = null;
        ResultSet rs                = null;        

        try {

            conn = DAO.getConnection();

            String sql = "SELECT status from serviceRequests WHERE status = ? AND service_id = ? AND requester_id = ?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1,  ServiceRequestStatus.PENDING.name());
            pstmt.setInt(2,     serviceId);
            pstmt.setInt(3,     userId);

            rs = pstmt.executeQuery();
                        
            return rs.next();

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

            DAO.close(pstmt);
            DAO.close(conn);
        }

    }

    public static final void unrequest(int serviceId, int userId)  {

        Connection conn             = null;
        PreparedStatement pstmt     = null;

        try {

            conn = DAO.getConnection();

            //  delete from serviceRequests
            String sql = "DELETE from serviceRequests WHERE service_id = ? and requester_id = ?";

            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, serviceId);
            pstmt.setInt(2, userId);

            pstmt.executeUpdate();


            //  refund the credit
            String refund = "UPDATE users SET credits = credits +1 WHERE id = ?";

            pstmt = conn.prepareStatement(refund);

            pstmt.setInt(1, userId);

            pstmt.executeUpdate();

            

            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

            DAO.close(pstmt);
            DAO.close(conn);
        }

    }


    public static final void makeServiceRequest(int serviceId, int providerId, int userId) {

        Connection conn             = null;
        PreparedStatement pstmt     = null;
        
        
        try {

            conn = DAO.getConnection();

            String insert = "INSERT INTO serviceRequests (provider_id, service_id, requester_id) VALUES (?,?,?)";

            String update = "UPDATE users SET credits = (credits - 1) WHERE id = ?"; 


            //  insert service request
            pstmt = conn.prepareStatement(insert);

            pstmt.setInt(1,     providerId);
            pstmt.setInt(2,     serviceId);
            pstmt.setInt(3,     userId);

            pstmt.executeUpdate();


            //  update user credit
            pstmt = conn.prepareStatement(update);

            pstmt.setInt(1, userId);

            pstmt.executeUpdate();


            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {
            
            DAO.close(pstmt);
            DAO.close(conn);
        }

    }

    public static final List<ServiceRequest> getServiceRequests(int serviceId) {

        Connection conn                         = null;
        PreparedStatement pstmt                 = null;
        ResultSet rs                            = null;

        List<ServiceRequest> serviceRequests    = null;

        try {

            conn = DAO.getConnection();

            String sql = new StringBuilder("SELECT sr.id, sr.requester_id, sr.created, r.email, r.firstname, r.lastname, u.rating, u.credits, i.title ").
                    append("FROM serviceRequests sr, registrations r, users u, services s, serviceInfos i ").
                    append("WHERE sr.service_id = ? AND u.id = sr.requester_id AND r.id = u.reg_id AND s.id = sr.service_id AND i.service_id = s.id").
                    toString();


            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, serviceId);

            rs = pstmt.executeQuery();

            serviceRequests = new ArrayList<ServiceRequest>();
            
            ServiceRequest sr;

            int index = 0;
            while(rs.next()) {

                sr = new ServiceRequest(rs, index);

                serviceRequests.add(sr);

                index++;

            }


           return serviceRequests;


        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

            DAO.close(rs);
            DAO.close(pstmt);
            DAO.close(conn);

        }        

    }

    public static final List<Service> getRequestedServices(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Service> services          = null;

        try {

            conn = DAO.getConnection();


            String sql = new StringBuilder().
                    append("SELECT r.firstname, r.lastname, i.title, s.id, sr.provider_id, sr.created AS requested_at, sr.status ").
                    append("FROM serviceRequests sr, registrations r, services s, users u, serviceInfos i ").
                    append("WHERE sr.requester_id = ? AND sr.status != ? AND s.id = sr.service_id AND u.id = sr.provider_id AND r.id = u.reg_id AND i.service_id = s.id ORDER BY sr.created").toString();

            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1,         userId);
            pstmt.setString(2,      ServiceRequestStatus.RESOLVED.name());

            rs = pstmt.executeQuery();

            services = new ArrayList<Service>();


            Service service;

            int index = 0;

            while (rs.next()) {

                service = Service.newRequestedService(rs, index);

                services.add(service);

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

        return services;

    }

    public static boolean isUserEligibleForNewRequests(int userId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        boolean userEligible = false;

        try {

            conn = DAO.getConnection();


            String sql = "SELECT credits FROM users WHERE id = ? LIMIT 1";


            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, userId);

            rs = pstmt.executeQuery();

            while (rs.next()) {

                userEligible = rs.getInt(1) >=1;

            }
            

            return userEligible;

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

            DAO.close(rs);
            DAO.close(pstmt);
            DAO.close(conn);

        }        

    }

    public static final void acceptConsumer(int requesterId, int serviceId, int providerId) {
        

        //  Service Request update status PENDING -> ACCEPTED

        //  Service Request update status PENDING -> DECLINED
        //  Refund the credits to the users and inform them

        //  update services set requester_id  and status      

        //  increment provider credit

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;       

        try {


            conn = DAO.getConnection();

            /**
             *  update serviceRequests
             *
             *  accepted one -> ACCEPTED
             *  those not accepted -> DECLINED
             *
             */
            String sql = new StringBuilder().
                    append("UPDATE serviceRequests ").
                    append("SET status = CASE ").
                    append("WHEN requester_id = ? AND service_id = ? THEN 'ACCEPTED'").
                    append("WHEN requester_id != ? AND service_id = ? THEN 'DECLINED'").
                    append("ELSE status ").
                    append("END").
                    toString();

            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, requesterId);
            pstmt.setInt(2, serviceId);
            pstmt.setInt(3, requesterId);
            pstmt.setInt(4, requesterId);

            pstmt.executeUpdate();



            //  Refund credits to those whose requests declined
            String refund = "UPDATE users SET credits = credits +1 WHERE id IN (SELECT requester_id FROM serviceRequests WHERE status = 'DECLINED' AND service_id = ?)";

            pstmt = conn.prepareStatement(refund);

            pstmt.setInt(1, serviceId);

            pstmt.executeUpdate();



            //  udpate services
            String updateServices = "UPDATE services SET requester_id = ?, status = ? WHERE id = ?";

            pstmt = conn.prepareStatement(updateServices);

            pstmt.setInt(1,     requesterId);
            pstmt.setString(2,  ServiceStatus.WAITING_TO_BE_RESOLVED.name());
            pstmt.setInt(3,     serviceId);

            pstmt.executeUpdate();
            


            //  increment provider credit
            String updateProvider = "UPDATE users SET credits = credits + 1 WHERE id  = ?";

            pstmt = conn.prepareStatement(updateProvider);

            pstmt.setInt(1,providerId);

            pstmt.executeUpdate();            

            
            conn.commit();
            

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

            DAO.close(rs);
            DAO.close(pstmt);
            DAO.close(conn);

        }

    }

    public static final int initializeServiceProgress(ServiceProgress progress) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        int progressId = -1; 

        try {

            conn = DAO.getConnection();

            String sql = "INSERT INTO serviceProgress (code, serviceRequest_id, status, created) VALUES (?, ?, ?, NOW())";

            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1,  progress.getCode());
            pstmt.setInt(2,     progress.getServiceRequestId());
            pstmt.setString(3,  progress.getStatus());

            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();

            while(rs.next()) {
                progressId = rs.getInt(1);
            }


            conn.commit();

            return progressId;


        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

            DAO.close(rs);
            DAO.close(pstmt);
            DAO.close(conn);

        }

    }

    public static final int getServiceRequestId(int serviceId, int providerId, int consumerId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        int serviceRequestId = -1;

        try {

            conn = DAO.getConnection();

            String sql = "SELECT id FROM serviceRequests where service_id = ? AND requester_id = ? AND provider_id = ? ";

            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1,     serviceId);
            pstmt.setInt(2,     consumerId);
            pstmt.setInt(3,     providerId);

            rs = pstmt.executeQuery();

            while(rs.next()) {
                serviceRequestId = rs.getInt(1);
            }


            conn.commit();

            return serviceRequestId;


        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

            DAO.close(rs);
            DAO.close(pstmt);
            DAO.close(conn);

        }

    }

    public static final String resolve(int serviceId, int providerId, int consumerId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        String progressCode = null;

        try {

            conn = DAO.getConnection();

            //  update services
            String sql = "UPDATE services SET status =? WHERE id = ? ";

            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1,      ServiceStatus.RESOLVED.name());
            pstmt.setInt(2,         serviceId);

            pstmt.executeUpdate();




            // update serviceProgress
            String updateServiceProgress = "UPDATE serviceProgress set status = ? WHERE servicerequest_id = (SELECT id FROM serviceRequests where service_id = ? AND requester_id = ? AND provider_id = ?)";

            pstmt = conn.prepareStatement(updateServiceProgress);

            pstmt.setString(1,  ServiceProgressStatus.READY_TO_BE_RATED.name());
            pstmt.setInt(2,     serviceId);
            pstmt.setInt(3,     consumerId);
            pstmt.setInt(4,     providerId);

            pstmt.executeUpdate();

            


            //  Update Service Request
            String updateServiceRequest = "UPDATE serviceRequests SET status = ? WHERE service_id = ? AND provider_id = ? AND requester_id = ?";

            pstmt = conn.prepareStatement(updateServiceRequest);

            pstmt.setString(1,      ServiceRequestStatus.RESOLVED.name());
            pstmt.setInt(2,         serviceId);
            pstmt.setInt(3,         providerId);
            pstmt.setInt(4,         consumerId);

            pstmt.executeUpdate();

            System.out.println("service request updated!");


            //  Progress Code
            String getProgressCode = "SELECT code FROM serviceProgress WHERE serviceRequest_id = (SELECT id FROM serviceRequests where service_id = ? AND requester_id = ? AND provider_id = ?)";

            pstmt = conn.prepareStatement(getProgressCode);

            pstmt.setInt(1, serviceId);
            pstmt.setInt(2, consumerId);
            pstmt.setInt(3, providerId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                progressCode = rs.getString("code");
            }

            System.out.println("Progress code received!");            

            conn.commit();

            return progressCode;


        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

            DAO.close(rs);
            DAO.close(pstmt);
            DAO.close(conn);

        }

    }


    public static final void giveConsumerRating(ServiceRating rating, int providerId) {

        //  rating given by consumer as providerRating

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        try {

            conn = DAO.getConnection();

            //  Persist new Rating
            String sql = "INSERT INTO serviceRatings (serviceId, userId, value, comment) VALUES (?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1,     rating.getServiceId());
            pstmt.setInt(2,     providerId);
            pstmt.setInt(3,     rating.getValue());
            pstmt.setString(4,  rating.getComment());

            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();

            int ratingId = -1;

            while(rs.next()) {
                ratingId = rs.getInt(1);
            }


            //  update Service Progress
            String update = "UPDATE serviceProgress SET providerRating_id = ?, status = CASE WHEN consumerRating_id IS NULL THEN 'READY_TO_BE_RATED' ELSE 'RATED' END WHERE code = ?";

            pstmt = conn.prepareStatement(update);


            pstmt.setInt(1,         ratingId);
            pstmt.setString(2,      rating.getCode());

            pstmt.executeUpdate();




            //  update user rating
            String userRating = "UPDATE users SET rating = rating + ? WHERE id = ?";            

            pstmt = conn.prepareStatement(userRating);

            pstmt.setInt(1,     rating.getValue());
            pstmt.setInt(2,     rating.getUserId());

            pstmt.executeUpdate();
                      


            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

            DAO.close(rs);
            DAO.close(pstmt);
            DAO.close(conn);

        }

    }

    public static final void giveProviderRating(ServiceRating rating, int consumer_id) {

        //  rating given by provider as consumerRating

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        try {

            conn = DAO.getConnection();

            //  Persist new Rating
            String sql = "INSERT INTO serviceRatings (serviceId, userId, value, comment) VALUES (?, ?, ?, ?)";

            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1,     rating.getServiceId());
            pstmt.setInt(2,     consumer_id);
            pstmt.setInt(3,     rating.getValue());
            pstmt.setString(4,  rating.getComment());

            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();

            int ratingId = -1;

            while(rs.next()) {
                ratingId = rs.getInt(1);
            }


            //  update Service Progress
            String update = "UPDATE serviceProgress SET consumerRating_id = ?, status = CASE WHEN providerRating_id IS NULL THEN 'READY_TO_BE_RATED' ELSE 'RATED' END WHERE code = ?";

            pstmt = conn.prepareStatement(update);

            pstmt.setInt(1,         ratingId);
            pstmt.setString(2,      rating.getCode());

            pstmt.executeUpdate();


            //  update user rating
            String userRating = "UPDATE users SET rating = rating + ? WHERE id = ?";

            pstmt = conn.prepareStatement(userRating);

            pstmt.setInt(1,     rating.getValue());
            pstmt.setInt(2,     consumer_id);

            pstmt.executeUpdate();
            

            conn.commit();

        } catch (Exception e) {

            log.warn(e);

            DAO.rollback(conn);

            throw new DataStoreException(e);

        } finally {

            DAO.close(rs);
            DAO.close(pstmt);
            DAO.close(conn);

        }

    }

    public static final List<ServiceRating> getServiceRatings(int userId) {

        Connection conn                     = null;
        PreparedStatement pstmt             = null;
        ResultSet rs                        = null;
        List<ServiceRating> serviceRatings  = null;

        try {

            conn = DAO.getConnection();

            String sql = "SELECT sr.value, sr.created, sr.comment, i.title, r.firstname, r.lastname " +
                    "FROM serviceRatings sr, serviceInfos i, services s, users u, registrations r " +
                    "WHERE sr.userId = ? AND s.id = sr.serviceId AND  i.service_id = sr.serviceId " +
                    "AND r.id = u.reg_id AND u.id = s.provider_id";

            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, userId);

            rs = pstmt.executeQuery();

            ServiceRating rating;

            serviceRatings = new ArrayList<ServiceRating>();

            int index = 0;
            while(rs.next()) {

                rating = new ServiceRating(rs, index++);

                serviceRatings.add(rating);

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

        return serviceRatings;


    }



    public static final List<ServiceProgress> getUnratedServiceProgressesByProvider(int providerId) {
        return null;
    }

    public static final List<ServiceProgress> getUnratedServiceProgressesByConsumer(int consumerId) {
        return null;
    }
   

}
