package models;

import play.*;
import play.db.jpa.*;

import javax.persistence.*;

import java.util.*;

@Entity
public class Candidate extends Model {
	@ManyToOne
	public User candidate;
	
	@ManyToOne
	public Service service;
}
