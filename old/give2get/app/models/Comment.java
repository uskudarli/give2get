package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;
import java.util.*;

@Entity
public class Comment extends Model {
	@ManyToOne
	public User commenter;
	
	@ManyToOne
	public Service service;
	public String comment;
}
