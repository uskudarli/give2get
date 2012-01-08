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
public final class Comment extends ListableModel{

    private String text;
    private int ownerId;
    private int serviceId;
    private String serviceTitle;
    private String created;

    private String ownerFullName;

    public Comment() {       
    }

    public Comment(ResultSet rs, int index) throws SQLException {

        id              = rs.getInt("id");
        text            = rs.getString("text");
        ownerId         = rs.getInt("user_id");
        serviceId       = rs.getInt("service_id");
        created         = rs.getString("created");
        serviceTitle    = rs.getString("title");

        oddOrEven   = index %2 == 0 ? "odd" :"even";

    }

    public static Comment newServiceComment(ResultSet rs, int index) throws SQLException {

        Comment comment = new Comment();

        comment.id              = rs.getInt("id");
        comment.text            = rs.getString("text");
        comment.created         = rs.getString("created");
        comment.ownerId         = rs.getInt("user_id");
        comment.ownerFullName   = rs.getString("firstname").concat(" ").concat(rs.getString("lastname"));        

        comment.oddOrEven       = index %2 == 0 ? "odd" :"even";

        return comment;

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
        return "Comment{" +
                "text='" + text + '\'' +
                ", ownerId=" + ownerId +
                ", serviceId=" + serviceId +
                ", serviceTitle='" + serviceTitle + '\'' +
                ", created='" + created + '\'' +
                '}';
    }
}
