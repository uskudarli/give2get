package models;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 13, 2011
 * Time: 11:52:31 PM
 * To change this template use File | Settings | File Templates.
 */
public final class Tag extends ListableModel{

    private String text;
    private int ownerId;
    private int serviceId;
    private String serviceTitle;
    private String created;

    private String ownerFullName;

    public Tag() {       
    }

    public Tag(ResultSet rs, int index) throws SQLException {

        id              = rs.getInt("id");
        text            = rs.getString("text");
        ownerId         = rs.getInt("user_id");
        serviceId       = rs.getInt("service_id");
        created         = rs.getString("created");
        serviceTitle    = rs.getString("title");

        oddOrEven   = index %2 == 0 ? "odd" :"even";

    }

    public static Tag newServiceTag(ResultSet rs, int index) throws SQLException {

        Tag tag = new Tag();

        tag.id              = rs.getInt("id");
        tag.text            = rs.getString("text");
        tag.created         = rs.getString("created");
        tag.ownerId         = rs.getInt("user_id");
        tag.ownerFullName   = rs.getString("firstname").concat(" ").concat(rs.getString("lastname"));        

        tag.oddOrEven       = index %2 == 0 ? "odd" :"even";

        return tag;

    }

    public String getOwnerFullName() {
        return ownerFullName;
    }

    public String getText() {
        return text;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public String getServiceTitle() {
        return serviceTitle;
    }

    public String getCreated() {
        return created;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "text='" + text + '\'' +
                ", ownerId=" + ownerId +
                ", serviceId=" + serviceId +
                ", serviceTitle='" + serviceTitle + '\'' +
                ", created='" + created + '\'' +
                '}';
    }
}
