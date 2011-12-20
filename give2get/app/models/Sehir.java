package models;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 19, 2011
 * Time: 9:13:11 AM
 * To change this template use File | Settings | File Templates.
 */
public final class Sehir {

    private final int id;

    private final String name;

    public Sehir (ResultSet rs) throws SQLException {

        id          = rs.getInt("id");
        name        = rs.getString("name");
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Sehir{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}


