package models;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Nov 13, 2011
 * Time: 4:01:04 PM
 * To change this template use File | Settings | File Templates.
 */
public final class TopRatedService extends ListableModel{

    private final int providerId;
    private final String title;
    private final int viewCount;

    public TopRatedService(ResultSet rs, int index) throws SQLException {

        id              = rs.getInt("id");
        providerId      = rs.getInt("provider_id");
        title           = rs.getString("title");
        viewCount       = rs.getInt("viewCount");

        oddOrEven       = index %2 == 0 ? "odd" :"even";

    }

    public String getTitle() {
        return title;
    }

    public int getProviderId() {
        return providerId;
    }

    public int getViewCount() {
        return viewCount;
    }

    @Override
    public String toString() {
        return "TopRatedService{" +
                "id=" + id +
                ", providerId=" + providerId +
                ", viewCount=" + viewCount +
                '}';
    }
}
