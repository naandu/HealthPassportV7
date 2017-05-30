package controllers;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import models.Task;
import play.*;
import play.data.DynamicForm;
import play.data.Form;
import play.data.validation.Constraints.Required;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import views.html.*;

public class Application extends Controller {
	

    public static Result index() {
        return ok(index.render("Health Passport"));
    }
    
    public static Result details(){
    	String[] postAction = request().body().asFormUrlEncoded().get("submit");
    	String action=postAction[0];
    	if ("Login".equals(action)) {
    		DynamicForm dynamicForm=Form.form().bindFromRequest();
        	String username=dynamicForm.get("username");
        	String password=dynamicForm.get("password");
        	String sql="SELECT email,password FROM register WHERE email=? AND password=?";
        	Connection connection=play.db.DB.getConnection();
			try {
				PreparedStatement stmt = connection.prepareStatement(sql);
				stmt.setString(1, username);
				stmt.setString(2, password);
				ResultSet rs=stmt.executeQuery();
				if(rs.next()){
					session("connected",username);
					return redirect(routes.Application.tasks());
				}
				else{
					return ok("Invalid Email-ID & Password");
				}
			} catch (SQLException e) {
				e.printStackTrace();
				return ok("SQL Exception");
			}
    	}
    	else if ("Register".equals(action)) {
    		return redirect(routes.Assets.at("html/register.html"));
    	}
    	else {
    	      return badRequest("This action is not allowed");
    	}
    }
    
    public static Result data(){
    	try{
    		MultipartFormData body = request().body().asMultipartFormData();
    	    FilePart picture = body.getFile("image");
    	    File image=picture.getFile();
			FileInputStream fis=new FileInputStream(image);
			if(fis!=null){
				DynamicForm dynamicForm = Form.form().bindFromRequest();
				String title=dynamicForm.get("text1");
				String sql = "INSERT INTO task (label,image) VALUES (?,?)";
				Connection connection=play.db.DB.getConnection();
				PreparedStatement stmt = connection.prepareStatement(sql);
				stmt.setString(1,title);
				stmt.setBinaryStream(2,  (InputStream)fis,(int)image.length());        	
				stmt.executeUpdate();
				fis.close();
				stmt.close();
			}
			
    	}
    	catch(FileNotFoundException fn){
    		return ok("Failed");
    	}
    	catch(SQLException  se){
    		return ok("SQL Exception");
    	}
    	catch(IOException ie){
    		return ok("IO Exception");
    	}
    	
    	return ok("File Uploaded Successfully...");
    }
    
    public static Result register(){
    	try{
				DynamicForm dynamicForm = Form.form().bindFromRequest();
				String fn=dynamicForm.get("fn");
				String ln=dynamicForm.get("ln");
				String email=dynamicForm.get("email");
				String passwd=dynamicForm.get("passwd");
				String repasswd=dynamicForm.get("repasswd");
				String gender=dynamicForm.get("gender");
				String mno=dynamicForm.get("mno");
				if(passwd.equals(repasswd)){
					String sql1="SELECT * FROM register WHERE email=? OR mno=?";
					String sql = "INSERT INTO register (fn,ln,email,password,gender,mno) VALUES (?,?,?,?,?,?)";
					Connection connection=play.db.DB.getConnection();
					PreparedStatement stmt1 = connection.prepareStatement(sql1);
					stmt1.setString(1,email);
					stmt1.setString(2,mno);
					ResultSet rs=stmt1.executeQuery();
					if(rs.next()){
						return ok("User already exist with Email ID or Mobile no...");
					}
					else{
						PreparedStatement stmt = connection.prepareStatement(sql);
						stmt.setString(1,fn);
						stmt.setString(2,ln);
						stmt.setString(3,email);
						stmt.setString(4,passwd);
						stmt.setString(5,gender);
						stmt.setString(6,mno);        	
						stmt.executeUpdate();
						stmt.close();
						return redirect(routes.Assets.at("html/RegisterSuccess.html"));
					}
				}
				else{
					return ok("Password doesn't match...");
				}
    	}
    	catch(SQLException  se){
    		return ok("SQL Exception");
    	}
   
    }
    
  	public static Result logout() {
    	session().remove("connected");
    	flash("success","You've been logged out");
    	return redirect(routes.Application.index());
	}
  	
  	static Form<Task> taskForm = Form.form(Task.class);
	  
	  public static Result tasks() {
		  return ok(views.html.success.render(Task.all(),taskForm));
	  }
	  
	  /*public static Result newTask() {
		  Form<Task> filledForm = taskForm.bindFromRequest();
		  if(filledForm.hasErrors()) {
			  return badRequest(views.html.success.render(Task.all(), filledForm));
		  } else {
			  Task.create(filledForm.get());  
		  }
	  	return redirect(routes.Application.tasks());
	  }*/
	  
	  public static Result deleteTask(Long id) {
		  Task.delete(id);
		  return redirect(routes.Application.tasks());
	  }
	  
	  public static Result upload(){
		  return ok(views.html.upload.render());
	  }
	  
	  public static Result renderImage(Long id) {
	        Task image = Task.byId(id);
	        return ok(image.image).as("image/jpeg");
	}
}
