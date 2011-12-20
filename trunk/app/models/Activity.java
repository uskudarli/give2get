package models;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 13, 2011
 * Time: 3:10:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Activity extends ListableModel{

    private final int userId;
    private final int serviceId;
    private final String text;
    private final String created;
    private final ActivityType type;    

    public Activity(ResultSet rs, int index) throws SQLException {

        id              = rs.getInt("id");
        userId          = rs.getInt("user_id");
        serviceId       = rs.getInt("service_id");
        text            = rs.getString("text");
        created         = rs.getString("created");
        type            = ActivityType.fromString(rs.getString("type"));

        oddOrEven       = index %2 == 0 ? "odd" :"even";

    }

    public int getUserId() {
        return userId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getText() {
        return text;
    }

    public String getCreated() {
        return created;
    }

    public ActivityType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + id +
                ", userId=" + userId +
                ", serviceId=" + serviceId +
                ", text='" + text + '\'' +
                ", created='" + created + '\'' +
                ", type=" + type +
                '}';
    }
}
