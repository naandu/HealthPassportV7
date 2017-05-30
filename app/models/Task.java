package models;
import java.awt.Image;
import java.util.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.*;

import com.mysql.jdbc.Blob;

import play.data.validation.Constraints.Required;
import play.db.ebean.*;

@Entity
public class Task extends Model{
    
		@Id
		public Long id;

		@Required
		public String label;
		
		@Lob
		public byte[] image;
	  
		public static Finder<Long,Task> find=new Finder(Long.class,Task.class);
	  
		public static List<Task> all() {
			return find.all();
		}
	  
		public static void delete(Long id) {
			find.ref(id).delete();
		}
		
		public static Task byId(Long id) {
	        return find.byId(id);
	    }
  
}