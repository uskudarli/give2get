package models;


import java.util.Date;
import java.util.UUID;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.boun.give2get.registration.RegistrationStatus;


/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 8, 2011
 * Time: 2:55:01 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Registration {

    private int id;
    private String code;
    private String email;
    private String firstname;
    private String lastname;
    private String password;
    private Date created;
    private Date activated;
    private RegistrationStatus status;


    public static Registration create(ResultSet rs) throws SQLException {

        Registration registration = new Registration();

        registration.id             = rs.getInt("id");
        registration.code           = rs.getString("code");
        registration.email          = rs.getString("email");
        registration.created        = rs.getTimestamp("created");
        registration.activated      = rs.getTimestamp("activated");
        registration.firstname      = rs.getString("firstname");
        registration.lastname       = rs.getString("lastname");
        registration.password       = rs.getString("password");
        registration.status         = RegistrationStatus.fromString(rs.getString("status"));

        return registration;

    }


    public static Registration create(String email, String firstname, String lastname, String password) {

        Registration registration = new Registration();

        registration.email          = email;
        registration.firstname      = firstname;
        registration.lastname       = lastname;
        registration.password       = password;
        registration.code           = UUID.randomUUID().toString();

        return registration;
    }


    public int getId() {
        return id;
    }

    public String getFullName() {
        return firstname.concat(" ").concat(lastname);
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPassword() {
        return password;
    }

    public Date getCreated() {
        return created;
    }

    public Date getActivated() {
        return activated;
    }

    @Override
    public String toString() {
        return "Registration{" +
                "code='" + code + '\'' +
                ", email='" + email + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", password='" + password + '\'' +
                ", created=" + created +
                ", activated=" + activated +
                '}';
    }
}
