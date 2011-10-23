package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class User extends Model {
	
	public Date registrationDate;
	
    public String name;
    public String surname;
    public String username;
    public String password;
    public String email;
    public String status;
    
    public User(String name, String surname, String username, String password, String email) {
    	this.name = name;
    	this.surname = surname;
    	this.username = username;
    	this.password = password;
    	this.email = email;
    	this.status = "inactive";
    	this.registrationDate = new Date();
    }
    
    public static User connect(String username, String password) {
        return find("byUsernameAndPassword", username, password).first();
    }
}
