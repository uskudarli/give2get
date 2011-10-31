package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Rating extends Model {
	@OneToOne
    public Service service;
    public User rater;
    public int rating;
    public String status; //Rated, Awaiting Rating
}
