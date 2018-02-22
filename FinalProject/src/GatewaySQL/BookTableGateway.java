package GatewaySQL;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import assign1.MasterController;
import assign1.ViewType;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import AuditTrailSQL.AuditTrailEntry;
import Authors.Author;
import Books.Book;

public class BookTableGateway {
	private static Logger logger = LogManager.getLogger();
	
	private Connection connection;

	private int id;
	private String title;
	private String publisher;
	private Date datePublished;
	private String summary;
	private Author author;
	private LocalDateTime lastModified;
	private Alert alert;
	
	public BookTableGateway() throws GatewayException{
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
	 * get Books
	 */
	
	
	public List<Book> getBooks(){
		List<Book> bookList = new ArrayList<Book>();
		Book book;
		Author author;
		PreparedStatement st = null;
		ResultSet rs = null;
		
		String sqlQuery = "SELECT * FROM bookTable "
				+ "INNER JOIN authorTable b ON b.id = author_id";
		try {
			st = connection.prepareStatement(sqlQuery);
			rs = st.executeQuery();
			
			while(rs.next()){
				/*get author*/
				author = new Author(rs.getInt("author_id"), 
				          rs.getString("first_name"),
				          rs.getString("last_name"),
				          rs.getDate("dob"), 
				          rs.getString("gender").charAt(0),
				          rs.getString("website"));
				author.setLastModified(rs.getTimestamp(14).toLocalDateTime());
				
				
				/*get book*/
				book = new Book(rs.getInt(1), rs.getString("title")
							, rs.getString("publisher")
							, rs.getDate("date_published"), rs.getString("summary"), author);
				book.setLastModified(rs.getTimestamp(7).toLocalDateTime());
				//logger.error(book.getTitle());
				
				bookList.add(book);
			}
			
		}catch(SQLException e){
			logger.error("Book List Connection: could not be retrieved ");
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

		return bookList;
		
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
	
	/**
	 * Delete Book from Database
	 * also deletes all library_book 
	 * references to book since
	 * it no longer exists
	 */
	public void deleteBook(Book delete) {
		// TODO Auto-generated method stub
		PreparedStatement st = null;
		PreparedStatement stAudit = null;
		PreparedStatement stLib = null;
		ResultSet rs = null;
		String sqlQuery = "DELETE FROM bookTable WHERE id = ?";
		String sqlAudit = "DELETE FROM audit_trail WHERE record_id = ? AND record_type = ?";
		
		/*library_book entry*/
		String sqlLibBook = "DELETE FROM library_book WHERE book_id = ?";
		try {
			connection.setAutoCommit(false);
			
			stAudit = connection.prepareStatement(sqlAudit);
			stAudit.setInt(1, delete.getId());
			stAudit.setString(2, String.valueOf('B'));
			stAudit.executeUpdate();
			
			stLib = connection.prepareStatement(sqlLibBook);
			stLib.setInt(1, delete.getId());
			stLib.executeUpdate();
			
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
	
	/**DO THESE*/
	public void insertBook(Book newBook) {
		// TODO Auto-generated method stub
		AuditTrailEntry addAudit;
		
		publisher = newBook.getPublisher();
		title = newBook.getTitle();
		summary = newBook.getSummary();
		author = newBook.getAuthor();
		datePublished = newBook.getDatePublished();
		//logger.error(publisher);
		//logger.error(title);
		//logger.error(summary);
		//logger.error(author);
		//logger.error(datePublished);
		newBook.setLastModified(null);
		
		PreparedStatement st = null;
		PreparedStatement stAudit = null;
		ResultSet rs = null;
		addAudit = new AuditTrailEntry(newBook.getTitle(), new Timestamp(System.currentTimeMillis()), "Added" );
		String sqlQuery = "INSERT bookTable SET "
				+ " title = ? "
				+ ", publisher = ? "
				+ ", date_published = ? "
				+ ", summary = ? "
				+ ", author_id = ? ";
		
		String sqlAuditQuery = "INSERT audit_trail SET "
				+ " record_type = ?"
				+ ", record_id = ? "
				+ ", entry_msg = ? ";
		try{
			connection.setAutoCommit(false);
			
			st = connection.prepareStatement(sqlQuery);
			st.setString(1, title);
			st.setString(2, publisher);
			st.setDate(3, datePublished);
			st.setString(4, summary);
			st.setInt(5, author.getId());
			
			st.executeUpdate();
			newBook.setId(getKey(newBook));
			newBook.setLastModified(getBookById(newBook.getId()).getLastModified());
			
			stAudit = connection.prepareStatement(sqlAuditQuery);
			stAudit.setString(1, String.valueOf('B'));
			stAudit.setInt(2, newBook.getId());
			stAudit.setString(3, addAudit.getMessage());
			stAudit.executeUpdate();

			try{
				connection.commit();
			}catch (SQLException e){
				logger.error("Book :" + newBook.getTitle() + " Failed, when commiting, to be Inserted into Database");
				connection.rollback();
			}
			
			connection.setAutoCommit(true);
			
		}catch(SQLException e){
			logger.error("Book :" + newBook.getTitle() + " Failed, when commiting, to be Inserted into Database");
			e.printStackTrace();
		}finally{
			try {
				if(rs != null)
					rs.close();
				if(st != null) 
					st.close();
				if(stAudit != null)
					stAudit.close();
			} catch (SQLException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void updateBook(Book update) throws IOException{
		Book oldRecord = getBookById(update.getId());
		List<AuditTrailEntry> auditUpdate = new ArrayList<AuditTrailEntry>();
		AuditTrailEntry newUpdate = new AuditTrailEntry();
		
		boolean isChanged = false;
		String updateMsg;
		
		id = update.getId();
		//logger.error(id);
		title = update.getTitle();
		publisher = update.getPublisher();
		summary = update.getSummary();
		author = update.getAuthor();
		datePublished = update.getDatePublished();
		
		/**
		 *   ORL for books does not work ATM gives
		 *  me error with date even if only one is open
		 *  but assignment does not say if we have to implement it
		 **/
		if(!oldRecord.getLastModified().equals(update.getLastModified())){
			logger.error("***Updates Do Not Match***");
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setHeaderText(null);
			alert.setTitle("Save Failed");
			alert.setContentText("Last Modified does not match\nTry re-selecting Book from List\nFor fresh data");
				
			ButtonType okButton = new ButtonType("OK");
			alert.getButtonTypes().setAll(okButton);
				
			Optional<ButtonType> result = alert.showAndWait();
			if(result.get() == okButton){
				MasterController.getInstance().changeView(ViewType.BOOK_DETAIL,ViewType.BOOK_LIST,null);
			}
			return;
		}
		
		/**
		 * check for what values got updated
		 * for chosen author
		 */
		if(!title.equals(oldRecord.getTitle())){
			logger.error(oldRecord.getTitle()+ " CHANGED");
			updateMsg = "Title changed From " + oldRecord.getTitle() + " To " + title;
			newUpdate = new AuditTrailEntry(update.getTitle(), new Timestamp(System.currentTimeMillis()), updateMsg );
			auditUpdate.add(newUpdate);
			isChanged = true;
		}
		
		if(!publisher.equals(oldRecord.getPublisher())){
			logger.error(oldRecord.getPublisher()+ " CHANGED");
			updateMsg = "Publisher changed From " + oldRecord.getPublisher() + " To " + publisher;
			newUpdate = new AuditTrailEntry(update.getTitle(), new Timestamp(System.currentTimeMillis()), updateMsg );
			auditUpdate.add(newUpdate);
			isChanged = true;
		}
		/* does not print out the complete change for summary since it would be to long */
		if(!summary.equals(oldRecord.getSummary())){
			logger.error("Summary CHANGED & UPDATED");
			updateMsg = "Summary Changed";
			newUpdate = new AuditTrailEntry(update.getTitle(), new Timestamp(System.currentTimeMillis()), updateMsg );
			auditUpdate.add(newUpdate);
			isChanged = true;
		}
		if(author.getId() != oldRecord.getAuthor().getId()){
			logger.error(oldRecord.getAuthor()+ " CHANGED & UPDATED");
			updateMsg = "Author changed From " + oldRecord.getAuthor() + " To " + author;
			newUpdate = new AuditTrailEntry(update.getTitle(), new Timestamp(System.currentTimeMillis()), updateMsg );
			auditUpdate.add(newUpdate);
			isChanged = true;
		}
		if(!datePublished.equals(oldRecord.getDatePublished())){
			logger.error(oldRecord.getDatePublished()+ " CHANGED");
			updateMsg = "Date Published changed From " + oldRecord.getDatePublished() + " To " + datePublished;
			newUpdate = new AuditTrailEntry(update.getTitle(), new Timestamp(System.currentTimeMillis()), updateMsg );
			auditUpdate.add(newUpdate);
			isChanged = true;
		}
		
		
		String sqlQuery = "UPDATE bookTable SET title = ? "
				+ ", publisher = ? "
				+ ", summary = ? "
				+ ", author_id = ? "
				+ ", date_published = ?"
				+ " WHERE id = ?";
		String sqlAuditQuery = "INSERT audit_trail SET "
				+ "record_type = ?, "
				+ "record_id = ?, "
				+ "entry_msg = ?";
		
		PreparedStatement st = null; 
		PreparedStatement stAudit = null;
		ResultSet rs = null;
		
		if(isChanged == true){
			alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Change in Book Record");
			alert.setHeaderText(null);
			alert.setContentText("You changed some values in the BOOK Record"
					+ "\nRemember to Save before leaving this page");
		}
		
		try {
			connection.setAutoCommit(false);
			st = connection.prepareStatement(sqlQuery, PreparedStatement.RETURN_GENERATED_KEYS);
			st.setString(1, title);
			st.setString(2, publisher);
			st.setString(3, summary);
			st.setInt(4, author.getId());
			st.setDate(5, datePublished);
			st.setInt(6, id);
			
			st.executeUpdate();
			rs = st.getGeneratedKeys();
			
			/**
			 * insert into audit entry table what was updated
			 */
			stAudit = connection.prepareStatement(sqlAuditQuery);
			for(AuditTrailEntry a : auditUpdate){
				stAudit.setString(1, String.valueOf('B'));
				stAudit.setInt(2,update.getId());
				stAudit.setString(3,a.getMessage());
				
				stAudit.addBatch();
			}
			stAudit.executeBatch();
			
			try{
				connection.commit();
				//logger.error("Commited Book: " + update.getTitle() + " Update to Database");
			}catch(SQLException e){
				logger.error("Error Book: " + update.toString() + " Update");
				connection.rollback();
			}
			
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("Update on " + update.toString() + " Failed" );
			e.printStackTrace();
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(st != null) 
					st.close();
				if(stAudit != null)
					stAudit.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	/*get book by id*/
	public Book getBookById(int id){
		Book bookRecord = null;
		PreparedStatement st = null;
		ResultSet rs = null;
		String sqlQuery = "SELECT * from bookTable b INNER JOIN"
				+ " authorTable a WHERE a.id = author_id AND b.id = ?";
		
		try {
			st = connection.prepareStatement(sqlQuery);
			st.setInt(1, id);
			rs = st.executeQuery();
			
			rs.first();
			author = new Author(rs.getInt("author_id"), 
			          rs.getString("first_name"),
			          rs.getString("last_name"),
			          rs.getDate("dob"), 
			          rs.getString("gender").charAt(0),
			          rs.getString("website"));
			author.setLastModified(rs.getTimestamp(14).toLocalDateTime());
			
			//logger.error(author + "lastMOD = " + author.getLastModified());
			bookRecord = new Book(rs.getInt("id"), 
			          rs.getString("title"),
			          rs.getString("publisher"),
			          rs.getDate("date_published"), 
			          rs.getString("summary"),
			          author);
			
			bookRecord.setLastModified(rs.getTimestamp("last_modified").toLocalDateTime());
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
		return bookRecord;
	}
	
	
	/*get id of new Book in db*/
	private int getKey(Book newBook) {
		// TODO Auto-generated method stub
		PreparedStatement st = null;
		ResultSet rs = null;
		String sqlQuery = "SELECT id FROM bookTable WHERE title = ?"
						   + " and publisher = ?"
						   + " and date_published = ?"
						   + " and summary = ?"
						   + " and author_id = ?";
		try {
			title = newBook.getTitle();
			publisher = newBook.getPublisher();
			datePublished = newBook.getDatePublished();
			summary = newBook.getSummary();
			int authorId = newBook.getAuthor().getId();
			
			st = connection.prepareStatement(sqlQuery);
		    st.setString(1, title);
			st.setString(2, publisher);
			st.setDate(3, datePublished);
			st.setString(4, summary);
			st.setInt(5, authorId);
			
			rs = st.executeQuery();
			rs.next();
			id = rs.getInt("id");
			
			logger.error("New Book Id : " + id);
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
	public String setSearchQuery(boolean noName, boolean noPub, boolean noDate){
		String searchQuery ="";

		if(!noName && !noPub && !noDate){
			logger.error("all fields full");
			searchQuery = "SELECT * FROM bookTable a INNER JOIN authorTable b ON b.id = a.author_id"
					+ " AND a.title LIKE ? AND a.publisher LIKE ? AND a.date_published >= ?";
		}
		
		/*single search param*/
		if(!noName && noPub && noDate){
			/*just name*/
			searchQuery = "SELECT * FROM bookTable a INNER JOIN authorTable b ON b.id = a.author_id"
						+ " AND a.title LIKE ?";
			logger.error("just book entered");
		}if(noName && !noPub && noDate){
			/*just publisher*/
			searchQuery = "SELECT * FROM bookTable a INNER JOIN authorTable b ON b.id = a.author_id"
					+ " AND a.publisher LIKE ?";
			logger.error("just publisher entered");
		}if(noName && noPub && !noDate){
			/*just date*/
			searchQuery = "SELECT * FROM bookTable a INNER JOIN authorTable b ON b.id = a.author_id"
					+ " AND a.date_published >= ?";
			logger.error("just date entered");
		}
		
		/*two params*/
		if(!noName && !noPub && noDate){
			/*just name and pub*/
			searchQuery = "SELECT * FROM bookTable a INNER JOIN authorTable b ON b.id = a.author_id"
					+ " AND a.title LIKE ? AND a.publisher LIKE ?";
			logger.error("name and publisher");
		}if(!noName && noPub && !noDate){
			/*just name and date*/
			searchQuery = "SELECT * FROM bookTable a INNER JOIN authorTable b ON b.id = a.author_id"
					+ " AND a.title LIKE ? AND a.date_published >= ?";
			logger.error("name and date");
		}if(noName && !noPub && !noDate){
			/*just date and pub*/
			searchQuery = "SELECT * FROM bookTable a INNER JOIN authorTable b ON b.id = a.author_id"
					+ " AND a.publisher LIKE ? AND a.date_published >= ?";
			logger.error("publisher and date");
		}
		return searchQuery;
	}
	
	
	public List<Book> search(String bookName
			, String publisher, Date datePub) throws IOException{
		//logger.error(datePub);
		boolean noName = false;
		boolean noPub = false;
		boolean noDate = false;
		Author author;
		Book book;
		List<Book> searchResults = new ArrayList<Book>();
		String searchQuery = "";
		
		if(bookName == null || bookName.isEmpty()){
			noName = true;
		}
		if(publisher == null || publisher.isEmpty()){
			noPub = true;
		}	
		if(datePub == null){
			noDate = true;
		}
		
		/*fields are all empty*/
		if(noName && noPub && noDate){
			logger.error("All Fields Empty Return to Default");
			//MasterController.getInstance().changeView(ViewType.BOOK_LIST, ViewType.BOOK_LIST, null);
			return null;
		}
		
		/*set the search query to be used based on the fields entered*/
		searchQuery = setSearchQuery(noName, noPub, noDate);

		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			
			st = connection.prepareStatement(searchQuery);
			if(!noName && !noPub && !noDate){
				st.setString(1,bookName + "%");
				st.setString(2, publisher + "%");
				st.setDate(3, datePub);
			}
			
			/*single search param*/
			if(!noName && noPub && noDate){
				/*just name*/
				st.setString(1,bookName + "%");
			}if(noName && !noPub && noDate){
				/*just publisher*/
				st.setString(1, publisher + "%");
			}if(noName && noPub && !noDate){
				/*just date*/
				logger.error("setting date");
				st.setDate(1, datePub);
			}
			
			/*two parameters*/
			if(!noName && !noPub && noDate){
				/*just name and pub*/
				st.setString(1,bookName + "%");
				st.setString(2, publisher + "%");
			}if(!noName && noPub && !noDate){
				/*just name and date*/
				st.setString(1,bookName + "%");
				st.setDate(2, datePub);
			}if(noName && !noPub && !noDate){
				/*just date and pub*/
				st.setString(1, publisher + "%");
				st.setDate(2, datePub);
			}
			
			rs = st.executeQuery();
			
			while(rs.next()){
				/*get author for book*/
				author = new Author(rs.getInt("author_id"), 
				          rs.getString("first_name"),
				          rs.getString("last_name"),
				          rs.getDate("dob"), 
				          rs.getString("gender").charAt(0),
				          rs.getString("website"));
				author.setLastModified(rs.getTimestamp(14).toLocalDateTime());
				
				/*get book*/
				book = new Book(rs.getInt(1), rs.getString("title")
							, rs.getString("publisher")
							, rs.getDate("date_published"), rs.getString("summary"), author);
				book.setLastModified(rs.getTimestamp(7).toLocalDateTime());
				//logger.error(book.getTitle());
				
				searchResults.add(book);
				System.out.println("Book Gotten" + book.getTitle());
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
		return searchResults;
	}
	/*retrieve audit trails for book selected*/
	public List<AuditTrailEntry> getAuditTrails(Book book) {
		// TODO Auto-generated method stub
		List<AuditTrailEntry> audits =  new ArrayList<AuditTrailEntry>();
		
		PreparedStatement st = null;
		ResultSet rs = null;
		String sqlQuery = "SELECT * "
				+ "FROM audit_trail "
				+ "INNER JOIN bookTable b ON b.id = record_id "
				+ "WHERE b.id = ? "
				+ "AND record_type = ?" // + author.getId() + " "
				+ "ORDER BY date_added ASC";
		try {
			st = connection.prepareStatement(sqlQuery);
			st.setInt(1,book.getId());
			st.setString(2, String.valueOf('B'));
			rs = st.executeQuery();
			
			while(rs.next()) {
				//create an author obj from record
				//logger.error("fistName = " + rs.getString("first_name"));
				 AuditTrailEntry audit = new AuditTrailEntry(rs.getString("title"),
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
		return audits;
	}	
	
}
