package models;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 4, 2011
 * Time: 7:58:15 PM
 * To change this template use File | Settings | File Templates.
 */
public final class ServiceRequest extends ListableModel{

    private final int requesterId;
    private final String requesterFullName;
    private final String requesterEmail;
    private final String requestedAt;
    private final int requesterRating;

    private final String serviceTitle;


    public ServiceRequest(ResultSet rs, int index) throws SQLException {

        id                          = rs.getInt("id");
        requesterId                 = rs.getInt("requester_id");
        requesterEmail              = rs.getString("email");
        requesterFullName           = rs.getString("firstname").concat(" ").concat(rs.getString("lastname"));
        requestedAt                 = rs.getString("created");
        requesterRating             = rs.getInt("rating");
        serviceTitle                = rs.getString("title");

        oddOrEven                   = index %2 == 0 ? "odd" :"even";

    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public String getRequesterFullName() {
        return requesterFullName;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public int getRequesterRating() {
        return requesterRating;
    }

    @Override
    public String toString() {
        return "ServiceRequest{" +
                "requesterId=" + requesterId +
                ", requesterFullName='" + requesterFullName + '\'' +
                ", requesterEmail='" + requesterEmail + '\'' +
                ", requestedAt='" + requestedAt + '\'' +
                ", requesterRating=" + requesterRating +
                '}';
    }
}
