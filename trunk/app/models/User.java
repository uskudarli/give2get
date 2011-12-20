package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 8, 2011
 * Time: 2:52:26 PM
 * To change this template use File | Settings | File Templates.
 */

public final class User {
    
    private final int id;

    private final Registration registration;

    private int rating; // todo

    private int numOfPostedServices;
    private int numOfComments;


    public User (ResultSet rs) throws SQLException {

        id                  = rs.getInt("userId");
        registration        = Registration.create(rs);

    }

    public int getNumOfPostedServices() {
        return numOfPostedServices;
    }

    public void setNumOfPostedServices(int numOfPostedServices) {
        this.numOfPostedServices = numOfPostedServices;
    }

    public int getNumOfComments() {
        return numOfComments;
    }

    public void setNumOfComments(int numOfComments) {
        this.numOfComments = numOfComments;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getRating() {
        return rating;
    }

    public String getEmail() {
        return registration.getEmail();
    }

    public int getId() {
        return id;
    }

    public Registration getRegistration() {
        return registration;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", registration=" + registration +
                '}';
    }
}
