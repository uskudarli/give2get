package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class User extends Model {
    public String name;
    public String surname;
    public String username;
    public String password;
    public String status;
    
    public User(String raw_name, String raw_surname, String raw_username, String raw_password) {
    	this.name = raw_name;
    	this.surname = raw_surname;
    	this.username = raw_username;
    	this.password = raw_password;
    }
}
