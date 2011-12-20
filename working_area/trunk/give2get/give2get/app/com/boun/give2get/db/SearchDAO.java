package com.boun.give2get.db;

import models.*;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.boun.give2get.exceptions.DataStoreException;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 19, 2011
 * Time: 9:18:42 AM
 * To change this template use File | Settings | File Templates.
 */
public final class SearchDAO {

    private static final Logger log = Logger.getLogger(SearchDAO.class);

    public static final List<Sehir> getSehirler() {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Sehir> sehirler            = null;

        try {

            conn = DAO.getConnection();

            pstmt = conn.prepareStatement("SELECT id, name FROM iller");


            rs = pstmt.executeQuery();

            sehirler = new ArrayList<Sehir>();


            Sehir sehir;

            while (rs.next()) {

                sehir = new Sehir(rs);

                sehirler.add(sehir);

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

        return sehirler;

    }

    public static final List<Ilce> getIlceler(int sehirId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Ilce> ilceler              = null;

        try {

            conn = DAO.getConnection();

            pstmt = conn.prepareStatement("SELECT * FROM ilceler WHERE il_id = ?");

            pstmt.setInt(1, sehirId);

            rs = pstmt.executeQuery();

            ilceler = new ArrayList<Ilce>();

            Ilce ilce;

            while (rs.next()) {

                ilce = new Ilce(rs);

                ilceler.add(ilce);

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

        return ilceler;

    }

    public static final List<Semt> getSemtler(int ilceId) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Semt> semtler              = null;

        try {

            conn = DAO.getConnection();

            pstmt = conn.prepareStatement("SELECT * FROM semtler WHERE ilce_id = ?");

            pstmt.setInt(1,     ilceId);

            rs = pstmt.executeQuery();

            semtler = new ArrayList<Semt>();

            Semt semt;

            while (rs.next()) {

                semt = new Semt(rs);

                semtler.add(semt);

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

        return semtler;

    }


    public static final List<Service> advanceSearch(String keyword, boolean lookInTitle, boolean lookInDescription) {

        Connection conn                 = null;
        PreparedStatement pstmt         = null;
        ResultSet rs                    = null;

        List<Service> services          = null;

        try {

            conn = DAO.getConnection();

            String sql =
                    "SELECT i.service_id, i.title, i.description, s.provider_id, s.created, s.viewCount, u.id as PROVIDER_ID, u.rating, u.postedCount, u.providedCount, r.firstname, r.lastname " +
                            "FROM serviceInfos i " +
                            "INNER JOIN services s ON s.id = i.service_id " +
                            "INNER JOIN users u ON u.id = s.provider_id " +
                            "INNER JOIN registrations r ON u.reg_id = r.id " +                           
                            "WHERE MATCH (" + (lookInTitle ? "title" : "") + (lookInDescription ? (lookInTitle ? ",description) " : "description) ") : ") ") +
                            "AGAINST (? IN NATURAL LANGUAGE MODE) ORDER BY u.postedCount ASC, u.providedCount ASC, u.rating DESC";

            System.out.println(sql);

            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1,  keyword);

            rs = pstmt.executeQuery();


            services = new ArrayList<Service>();

            Service service;

            int index = 0;

            while (rs.next()) {

                service = Service.newAdvancedSearched(rs, index++);

                service.setRequestCount(DAO.getServiceCurrentRequestCount(conn, service.getId()));

                services.add(service);                

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


}
