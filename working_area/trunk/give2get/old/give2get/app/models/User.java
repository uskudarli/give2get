package models;

import play.*;
import play.data.validation.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class User extends Model {
	
	public Date registrationDate;
	
	@Required
	@MaxSize(100)
    public String name;
	
	@Required
	@MaxSize(100)
    public String surname;
    
	@Required
	@MaxSize(100)
	public String username;
    
	@Required
	@MaxSize(100)
	@Min(4)
	public String password;
    
    @Required
    @MaxSize(100)
    @Email
    public String email;
    
    public String status;
    public int reputation;
    
    public String phone;
    public Boolean avatar;
    
    public String participantRating;
    public String serviceRating;
    
    @OneToMany
    public List<Service> services;
    
    public User(String name, String surname, String username, String password, String email) {
    	this.name = name;
    	this.surname = surname;
    	this.username = username;
    	this.password = password;
    	this.email = email;
    	this.status = "inactive";
    	this.registrationDate = new Date();
    }
    
    public static User getUser(String username, String password) {
        return find("byUsernameAndPassword", username, password).first();
    }
}
