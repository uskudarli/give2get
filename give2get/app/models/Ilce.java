package models;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: canelmas
 * Date: Dec 19, 2011
 * Time: 9:14:56 AM
 * To change this template use File | Settings | File Templates.
 */
public final class Ilce {

    private int id;
    private int ilId;
    private String name;

    public Ilce(int id, int ilId, String name) {

        this.id         = id;
        this.ilId       = ilId;
        this.name       = name;

    }

    public Ilce(ResultSet rs) throws SQLException {

        id      = rs.getInt("id");
        ilId    = rs.getInt("il_id");
        name    = rs.getString("name");

    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIlId(int ilId) {
        this.ilId = ilId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getIlId() {
        return ilId;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Ilce{" +
                "id=" + id +
                ", ilId=" + ilId +
                ", name='" + name + '\'' +
                '}';
    }
       
}
