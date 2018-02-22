package GatewaySQL;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import assign1.MasterController;
import assign1.ViewType;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import AuditTrailSQL.AuditTrailEntry;
import Authors.Author;
import Books.Book;

public class AuthorTableGateway {
	private static Logger logger = LogManager.getLogger();
	private Connection connection;

	private int id;
	private String first;
	private String last;
	private String web;
	private char gen;
	private Date dob;
	private Alert alert;
	/**
	 * deletes author from database
	 * @param delete
	 */
	public void deleteAuthor(Author delete){
		int bookId;
		PreparedStatement st = null;
		PreparedStatement stAudit = null;
		PreparedStatement stBook = null;
		PreparedStatement stBookDel = null;

		ResultSet rs = null;
		String sqlQuery = "DELETE FROM authorTable WHERE id = ? ";
		String bookQuery = "DELETE FROM bookTable WHERE id = ?";
		String sqlAuditQuery = "DELETE FROM audit_trail WHERE record_id = ? AND record_type = ?";
		String sqlQueryBook = "SELECT id FROM bookTable WHERE author_id = ?";
		
		try {
			connection.setAutoCommit(false);
			
			/*get book id for audit deletion*/
			stBook = connection.prepareStatement(sqlQueryBook);
			stBook.setInt(1, delete.getId());;
			rs = stBook.executeQuery();
			rs.next();
			bookId = rs.getInt(1);
			
			/*delete book queries */
			stAudit = connection.prepareStatement(sqlAuditQuery);
			stAudit.setInt(1, bookId);
			stAudit.setString(2, String.valueOf('B'));
			stAudit.executeUpdate();
			
			/*delete author queries*/
			stAudit = connection.prepareStatement(sqlAuditQuery);
			stAudit.setInt(1,delete.getId());
			stAudit.setString(2, String.valueOf('A'));
			stAudit.executeUpdate();
			
			/*delete book */
			stBookDel = connection.prepareStatement(bookQuery);
			stBookDel.setInt(1, bookId);
			stBookDel.executeUpdate();
			
			/*delete author */
			st = connection.prepareStatement(sqlQuery);
			st.setInt(1,delete.getId());
			st.executeUpdate();
			

			try{
				connection.commit();
			}catch(SQLException e){
				connection.rollback();
				logger.error("Error Commiting to Database for " + delete.toString() );
			}
			
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error("Error Deleteing " + delete.toString() + " from Database");
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(st != null) 
					st.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * gets authors from Database and 
	 * returns them in a list
	 * @return authors
	 */
	public List<Author> getAuthors(){
		List<Author> authors = new ArrayList<Author>();
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			st = connection.prepareStatement("select * from authorTable");
			rs = st.executeQuery();
			
			while(rs.next()) {
				//create an author obj from record
				//logger.error("fistName = " + rs.getString("first_name"));
				Author author = new Author(rs.getInt("id"), 
						          rs.getString("first_name"),
						          rs.getString("last_name"),
						          rs.getDate("dob"), 
						          rs.getString("gender").charAt(0),
						          rs.getString("website"));
				author.setLastModified(rs.getTimestamp("last_modified").toLocalDateTime());
				authors.add(author);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(st != null) 
					st.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		/*for(Author a : authors){
			logger.error("Authors Gotten: "+ a.getFirstName());
		}*/
		return authors;
	}
	
	
	public List<AuditTrailEntry> getAuditTrails(Author author){
		List<AuditTrailEntry> audits =  new ArrayList<AuditTrailEntry>();
		
		PreparedStatement st = null;
		ResultSet rs = null;
		String sqlQuery = "SELECT * "
				+ "FROM audit_trail "
				+ "INNER JOIN authorTable b ON b.id = record_id "
				+ "WHERE b.id = " + author.getId() + " "
				+ "ORDER BY date_added ASC";
		try {
			st = connection.prepareStatement(sqlQuery);
			rs = st.executeQuery();
			
			while(rs.next()) {
				//create an author obj from record
				//logger.error("fistName = " + rs.getString("first_name"));
				 AuditTrailEntry audit = new AuditTrailEntry(rs.getString("first_name") + " " + rs.getString("last_name"),
						 									rs.getTimestamp("date_added"),
						 									rs.getString("entry_msg"));
				 audits.add(audit);	 
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(st != null) 
					st.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		for(AuditTrailEntry a : audits){
			logger.error("Authors Gotten: "+ a.getRecordDescriptor() +"\nMSG: " + a.getMessage());
		}
		return audits;
	}
	/**
	 * get new id for author being inserted for first time and 
	 * update the model accordingly
	 * @param newAuthor
	 * @return
	 */
	public int getKey(Author newAuthor){
		PreparedStatement st = null;
		ResultSet rs = null;
		String sqlQuery = "SELECT id FROM authorTable WHERE first_name = ?"
						   + "and last_name = ?"
						   + "and dob = ?"
						   + "and gender = ?"
						   + "and website = ?";
		try {
			first = newAuthor.getFirstName();
			last = newAuthor.getLastName();
			gen = newAuthor.getGender();
			web = newAuthor.getWebSite();
			dob = newAuthor.getDOB();
			
			st = connection.prepareStatement(sqlQuery);
		    st.setString(1, first);
			st.setString(2, last);
			st.setDate(3, dob);
			st.setString(4, String.valueOf(gen));
			st.setString(5, web);
			
			rs = st.executeQuery();
			rs.next();
			id = rs.getInt("id");
			
			logger.error("New Author Id : " + id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(st != null) 
					st.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return id;
	}
	
	
		/**
		 * inserts new author into the database
		 * and updates the id of the newAuthor 
		 * so it is no longer 0
		 * 
		 * @param newAuthor
		 */
	public void insertAuthor(Author newAuthor){
		AuditTrailEntry addAudit;
		
		id = newAuthor.getId();
		first = newAuthor.getFirstName();
		last = newAuthor.getLastName();
		gen = newAuthor.getGender();
		web = newAuthor.getWebSite();
		dob = newAuthor.getDOB();
		newAuthor.setLastModified(null);
		
		addAudit = new AuditTrailEntry(newAuthor.getRecordDesc(), new Timestamp(System.currentTimeMillis()), "Added" );
		String sqlQuery =  "INSERT authorTable SET first_name = ?"
					   + ", last_name = ?"
					   + ", dob = ?"
					   + ", gender = ?"
					   + ", website = ?";
		
		String sqlAuditQuery = "INSERT audit_trail SET "
				+ "record_id = ?, "
				+ "entry_msg = ?";
		PreparedStatement st = null; 
		PreparedStatement stAudit = null;
		ResultSet rs = null;
			
		try {
			connection.setAutoCommit(false);
			st = connection.prepareStatement(sqlQuery, PreparedStatement.RETURN_GENERATED_KEYS);
			st.setString(1, first);
			st.setString(2, last);
			st.setDate(3, dob);
			st.setString(4, String.valueOf(gen));
			st.setString(5, web);
				
			st.executeUpdate();
			rs = st.getGeneratedKeys();	
			newAuthor.setId(getKey(newAuthor));
			newAuthor.setLastModified(getAuthorbyId(newAuthor.getId()).getLastModified());
			
			/**
			 * insert into audit entry table what was updated
			 */
			stAudit = connection.prepareStatement(sqlAuditQuery);
			stAudit.setInt(1,newAuthor.getId());
			stAudit.setString(2,addAudit.getMessage());
			stAudit.executeUpdate();
			
			try{
				connection.commit();
			}catch (SQLException e){
				logger.error("Author" + newAuthor.getRecordDesc() + "Failed, when commiting, to be Inserted into Database");
				connection.rollback();
			}
			
			connection.setAutoCommit(true);
			logger.error("Author: " + newAuthor.toString() + " has been inserted");
		} catch (SQLException e) {
				// TODO Auto-generated catch block
			logger.error("Author:" + newAuthor.toString() + "Failed to be Inserted");
			e.printStackTrace();
			
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(st != null) 
					st.close();
			} catch (SQLException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
		/**
		 * updates author when user inputs new info
		 * in detail view if valid
		 * @param update
		 * @throws IOException 
		 */
	public void updateAuthor(Author update) throws IOException{
		Author oldRecord = getAuthorbyId(update.getId());
		List<AuditTrailEntry> auditUpdate = new ArrayList<AuditTrailEntry>();
		AuditTrailEntry newUpdate = new AuditTrailEntry();

		boolean isChanged = false;
		String updateMsg;
		id = update.getId();
		//logger.error(id);
		first = update.getFirstName();
		last = update.getLastName();
		gen = update.getGender();
		web = update.getWebSite();
		dob = update.getDOB();
		
		//logger.error("***OLD REC : " + oldRecord.getLastModified() + "****");
		//logger.error("***NEW REC : " + update.getLastModified() + "****");
		if(!oldRecord.getLastModified().equals(update.getLastModified())){
			logger.error("***Updates Do Not Match***");
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setHeaderText(null);
			alert.setTitle("Save Failed");
			alert.setContentText("Last Modified does not match\nTry re-selecting Author from List\nFor fresh data");
			
			ButtonType okButton = new ButtonType("OK");
			alert.getButtonTypes().setAll(okButton);
			
			Optional<ButtonType> result = alert.showAndWait();
			if(result.get() == okButton){
				MasterController.getInstance().changeView(ViewType.AUTHOR_DETAIL,ViewType.AUTHOR_LIST,null);
			}
			return;
		}
		/**
		 * check for what values got updated
		 * for chosen author
		 */
		if(!first.equals(oldRecord.getFirstName())){
			logger.error(oldRecord.getFirstName()+ " CHANGED");
			updateMsg = "First Name changed From " + oldRecord.getFirstName() + " To " + first;
			newUpdate = new AuditTrailEntry(update.getRecordDesc(), new Timestamp(System.currentTimeMillis()), updateMsg );
			auditUpdate.add(newUpdate);
			isChanged = true;
		}
		if(!last.equals(oldRecord.getLastName())){
			logger.error(oldRecord.getLastName()+ " CHANGED");
			updateMsg = "Last Name changed From " + oldRecord.getLastName() + " To " + last;
			newUpdate = new AuditTrailEntry(update.getRecordDesc(), new Timestamp(System.currentTimeMillis()), updateMsg );
			auditUpdate.add(newUpdate);
			isChanged = true;
		}
		if(gen != oldRecord.getGender()){
			logger.error(oldRecord.getGender()+ " CHANGED & UPDATED");
			updateMsg = "Gender changed From " + oldRecord.getGender() + " To " + gen;
			newUpdate = new AuditTrailEntry(update.getRecordDesc(), new Timestamp(System.currentTimeMillis()), updateMsg );
			auditUpdate.add(newUpdate);
			isChanged = true;
		}
		if(!web.equals(oldRecord.getWebSite())){
			logger.error(oldRecord.getWebSite()+ " CHANGED & UPDATED");
			updateMsg = "Website changed From " + oldRecord.getWebSite() + " To " + web;
			newUpdate = new AuditTrailEntry(update.getRecordDesc(), new Timestamp(System.currentTimeMillis()), updateMsg );
			auditUpdate.add(newUpdate);
			isChanged = true;
		}
		if(!dob.equals(oldRecord.getDOB())){
			logger.error(oldRecord.getDOB()+ " CHANGED");
			updateMsg = "Date of Birth changed From " + oldRecord.getGender() + " To " + dob;
			newUpdate = new AuditTrailEntry(update.getRecordDesc(), new Timestamp(System.currentTimeMillis()), updateMsg );
			auditUpdate.add(newUpdate);
			isChanged = true;
		}
		/**
		 * update author in db
		 * update author audit trail
		 */
		String sqlQuery =  "UPDATE authorTable SET first_name = ?"
				+ ", last_name = ?"
				+ ", dob = ?"
				+ ", gender = ?"
				+ ", website = ?"
				+ "WHERE id = ?";
		
		String sqlAuditQuery = "INSERT audit_trail SET "
				+ "record_id = ?, "
				+ "entry_msg = ?";
		PreparedStatement st = null; 
		PreparedStatement stAudit = null;
		ResultSet rs = null;
		
		if(isChanged == true){
			alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Change in Author Record");
			alert.setHeaderText(null);
			alert.setContentText("You changed some values in the Author Record"
					+ "\nRemember to Save before leaving this page"
					+ "\nTo save changes");
		}
		try {
			connection.setAutoCommit(false);
			st = connection.prepareStatement(sqlQuery, PreparedStatement.RETURN_GENERATED_KEYS);
			st.setString(1, first);
			st.setString(2, last);
			st.setDate(3, dob);
			st.setString(4, String.valueOf(gen));
			st.setString(5, web);
			st.setInt(6, id);
			
			st.executeUpdate();
			rs = st.getGeneratedKeys();
			
			/**
			 * insert into audit entry table what was updated
			 */
			stAudit = connection.prepareStatement(sqlAuditQuery);
			for(AuditTrailEntry a : auditUpdate){
				
				stAudit.setInt(1,update.getId());
				stAudit.setString(2,a.getMessage());
				
				stAudit.addBatch();
			}
			stAudit.executeBatch();
			
			try{
				connection.commit();
				logger.error("Commited Author: " + update.toString() + " Update to Database");
			}catch(SQLException e){
				logger.error("Error Committing Author: " + update.toString() + " Update");
				connection.rollback();
			}
			
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("Update on " + update.toString() + " Failed" );
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(st != null) 
					st.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Grabs current author record from database using id
	 * @param id
	 * @return
	 */
	public Author getAuthorbyId(int id) {
		Author authorRecord = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		String sqlQuery = "SELECT * from authorTable WHERE id = " + id;
		try {
			st = connection.prepareStatement(sqlQuery);
			rs = st.executeQuery();
			
			rs.first();
			authorRecord = new Author(rs.getInt("id"), 
			          rs.getString("first_name"),
			          rs.getString("last_name"),
			          rs.getDate("dob"), 
			          rs.getString("gender").charAt(0),
			          rs.getString("website"));
			
			authorRecord.setLastModified(rs.getTimestamp("last_modified").toLocalDateTime());
			//logger.error("author before update grabbed: " + authorRecord);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(st != null) 
					st.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return authorRecord;
	}
	/**
	 * create a connection with authors database
	 * create datasource
	 * and read database credentials from properties file
	 * @return
	 */
	public AuthorTableGateway() throws GatewayException{
		connection = null;
		Properties props = new Properties();
		FileInputStream  fis = null;
		
        try {
        		/* file only worked when place within assign2 
        		 * project folder....
        		 */
			fis = new FileInputStream("db.properties");
	        props.load(fis);
	        fis.close();

	        //create the datasource
	        MysqlDataSource ds = new MysqlDataSource();
	        ds.setURL(props.getProperty("MYSQL_AUTHOR_DB_URL"));
	        ds.setUser(props.getProperty("MYSQL_AUTHOR_DB_USERNAME"));
	        ds.setPassword(props.getProperty("MYSQL_AUTHOR_DB_PASSWORD"));

			//create the connection
			connection = ds.getConnection();
        } catch (IOException | SQLException e) {
			e.printStackTrace();
			throw new GatewayException(e);
		}
	}

	/**
	 * close the connection
	 */
	public void close() {
		if(connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
