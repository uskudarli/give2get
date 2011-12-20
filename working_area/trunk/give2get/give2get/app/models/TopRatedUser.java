package models;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 5, 2011
 * Time: 2:29:22 AM
 * To change this template use File | Settings | File Templates.
 */
public final class TopRatedUser extends ListableModel{
    
    private final String fullName;
    private int rating;

    public TopRatedUser(ResultSet rs, int index) throws SQLException {

        id                  = rs.getInt("id");
        fullName            = rs.getString("firstname").concat(" ").concat(rs.getString("lastname"));
        rating              = rs.getInt("rating");

        oddOrEven       = index %2 == 0 ? "odd" :"even";

    }

    public String getFullName() {
        return fullName;
    }

    public int getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return "TopRatedUser{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", rating=" + rating +
                '}';
    }
}
