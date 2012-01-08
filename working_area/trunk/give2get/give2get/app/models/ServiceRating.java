package models;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 18, 2011
 * Time: 4:14:20 PM
 * To change this template use File | Settings | File Templates.
 */
public final class ServiceRating extends ListableModel{

    private String code;
    private int value;
    private int serviceId;
    private int userId;

    private final String comment;

    private String created;
    private String givenBy;
    private String serviceTitle;

    public ServiceRating(String code, int value, int serviceId, int userId, String comment) {

        this.code                   = code;
        this.value                  = value;
        this.serviceId              = serviceId;
        this.userId                 = userId;
        this.comment                = comment;

    }

    public ServiceRating(ResultSet rs, int index) throws SQLException {

        this.value          = rs.getInt("value");
        this.comment        = rs.getString("comment");
        this.created        = rs.getString("created");
        this.givenBy        = rs.getString("firstname") + " " + rs.getString("lastname");
        this.serviceTitle   = rs.getString("title");

        oddOrEven           = index %2 == 0 ? "odd" :"even";

    }

    public String getCreated() {
        return created;
    }

    public String getGivenBy() {
        return givenBy;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return value;
    }

    public int getServiceId() {
        return serviceId;
    }

    public int getUserId() {
        return userId;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return "ServiceRating{" +
                "code='" + code + '\'' +
                ", value=" + value +
                ", serviceId=" + serviceId +
                ", userId=" + userId +
                ", comment='" + comment + '\'' +
                ", created='" + created + '\'' +
                ", givenBy='" + givenBy + '\'' +
                ", serviceTitle='" + serviceTitle + '\'' +
                '}';
    }
}
