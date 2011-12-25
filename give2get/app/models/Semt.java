package models;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 19, 2011
 * Time: 9:16:47 AM
 * To change this template use File | Settings | File Templates.
 */
public final class Semt {

    private final int id;
    private final String name;
    private final int ilceId;

    public Semt(ResultSet rs) throws SQLException {

        id      = rs.getInt("id");
        name    = rs.getString("name");
        ilceId  = rs.getInt("ilce_id");
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIlceId() {
        return ilceId;
    }

    @Override
    public String toString() {
        return "Semt{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", ilceId=" + ilceId +
                '}';
    }
}
