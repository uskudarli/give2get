package models;

import play.*;
import play.data.validation.*;
import play.db.jpa.*;


import javax.persistence.*;

import net.sf.oval.constraint.DateRange;

import java.sql.Timestamp;
import java.util.*;

@Entity
public class Service extends Model {
    @Required
    public User provider;
	
    public User consumer;
    
	@Required
    public String title;
    
    @Required
    @Lob
    public String description;
    
    public String image;
    
    public Timestamp creationDate;
    
    public String status;
    public boolean provided;
    public boolean consumed;
    
    @OneToMany
    public List<User> candidate;
    
    @OneToMany
    public List<Rating> rating;
    
}
