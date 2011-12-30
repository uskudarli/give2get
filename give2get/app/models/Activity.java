package models;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.boun.give2get.db.DAO;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas,orcunguducu
 * Date: Nov 13, 2011
 * Time: 3:10:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Activity extends ListableModel{

    private final int userId;
    private final int serviceId;
    private final String name;
    private final String text;
    private String title;
    private final String created;
    private final ActivityType type;    

    public Activity(ResultSet rs, int index) throws SQLException {
    	title			= "";
        userId          = rs.getInt("id");
        name			= rs.getString("firstName")+' '+rs.getString("lastName");
        serviceId		= rs.getInt("service_id");
        if(serviceId != 0)
        	title			= DAO.getServiceDetail(serviceId).getTitle();
        created         = rs.getString("created");
        type            = ActivityType.fromString(rs.getString("type"));
        if(rs.getString("type").equals("NEW_USER"))
        {
        	text = " just joined";
        }
        else if (rs.getString("type").equals("NEW_SERVICE"))
        {
        	text = " just posted a new service ";
        }
        else if (rs.getString("type").equals("NEW_TAG"))
        {
        	text = " just added a new tag to the service ";
        }
       else
        {
        	text = " commented on the service ";
        }

        oddOrEven       = index %2 == 0 ? "odd" :"even";

    }

    public int getUserId() {
        return userId;
    }

    public int getServiceId() {
        return serviceId;
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
                ", userId=" + userId +
                ", firstName=" + name +
                ", title=" + title +
                ", serviceId=" + serviceId +
                ", created='" + created + '\'' +
                ", type=" + type +
                '}';
    }
}
