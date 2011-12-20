package models;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.boun.give2get.db.DAO;

/**
 * User: orcunguducu
 */

public final class Skill extends ListableModel{

    private final String title;
    private final String description;
    private final int userId;

    
    public Skill(String title,String description,int userId){
    	this.title = title;
    	this.description = description;
    	this.userId = userId;
    }
   
    public Skill(ResultSet rs,int userId, int index) throws SQLException {
        title          = rs.getString("title");
        description	   = rs.getString("description");
        this.userId	   	   = userId;
        
        oddOrEven       = index %2 == 0 ? "odd" :"even";

    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
    
    public int getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "Skill{" +
                ", title=" + title +
                ", description=" + description +
                '}';
    }
}
