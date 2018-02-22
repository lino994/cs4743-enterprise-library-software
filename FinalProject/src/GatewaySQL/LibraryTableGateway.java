package GatewaySQL;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import Library.*;

public class LibraryTableGateway {
	private static Logger logger = LogManager.getLogger();
	private Connection connection;
	
	List<Library> libraries;
	LibraryBook libraryBook;
	
	/**
	 * retrieve a library from database based on 
	 * id provided
	 * @param id
	 * @return
	 * @throws IOException
	 */
	public Library getLibraryById(int id) throws IOException{
		int book_id;
		int quantity;
		Book book;
		List<LibraryBook> booksAtLibrary = new ArrayList<LibraryBook>();
		
		Library library = new Library();
		LibraryBook book_record;
		PreparedStatement st = null;
		PreparedStatement stLibrary = null;
		ResultSet rs = null;
		String sqlQuery = "SELECT * FROM library_book WHERE library_id = ?";
		String sqlLibrary = "SELECT * FROM library WHERE id = ?";
		String libraryName;
		
		try {
			st = connection.prepareStatement(sqlQuery);
			st.setInt(1, id);
			rs = st.executeQuery();
			
			while(rs.next()){
				book_id = rs.getInt("book_id");
				quantity = rs.getInt("quantity");
				
				book = MasterController.getInstance().getBookGateway().getBookById(book_id);
				book_record = new LibraryBook(book, quantity, false);
				booksAtLibrary.add(book_record);
				
			}
			
			stLibrary = connection.prepareStatement(sqlLibrary);
			stLibrary.setInt(1,id);
			
			rs = stLibrary.executeQuery();
			while(rs.next()){
				library = new Library(id, rs.getString("library_name")
						, booksAtLibrary
						, rs.getTimestamp("last_modified").toLocalDateTime());	
			}
			
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
		
		
		return library;
		
	}
	
	/**
	 * get list of all the libraries in database
	 * @return
	 * @throws IOException
	 */
	public List<Library> getLibraries() throws IOException{
		List<Library> libraryList = new ArrayList<Library>();
		PreparedStatement st = null;
		ResultSet rs = null;
		
		int libID;
		try{
			st = connection.prepareStatement("SELECT * FROM library");
			rs = st.executeQuery();
			while(rs.next()){
				libID = rs.getInt("id");
				libraryList.add(getLibraryById(libID));
			}
			
		}catch(SQLException e){
			logger.error("Error Getting Libraries");
			e.printStackTrace();
		}finally{
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
		return libraryList;
		
	}
	/**
	 * init table gateway
	 * @throws GatewayException
	 */
	public LibraryTableGateway() throws GatewayException{
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
	 * insert new library into database
	 * @param library
	 * @throws IOException
	 */
	public void insertLibrary(Library library) throws IOException{
			AuditTrailEntry addLibAudit;
			List<AuditTrailEntry> auditMessages = new ArrayList<AuditTrailEntry>();
			PreparedStatement st = null;
			PreparedStatement stAudit = null;
			
			PreparedStatement lbInsert = null;
			
			addLibAudit = new AuditTrailEntry (library.getLibraryName(), new Timestamp(System.currentTimeMillis()), "Added" );
			auditMessages.add(addLibAudit);
			/*since library is new and is being inserted all books should be new too */
			List<LibraryBook> libBooks = library.getBooks();
			
			String sqlLibBook = "INSERT library_book SET "
					+ "library_id = ?"
					+ ", book_id = ?"
					+ ", quantity = ?";
			
			String sqlQuery = "INSERT library SET "
					+ "library_name = ?";
			
			String sqlAudit = "INSERT audit_trail SET "
					+ " record_type = ?"
					+ ", record_id = ? "
					+ ", entry_msg = ? ";
			try{
				connection.setAutoCommit(false);
				
				st = connection.prepareStatement(sqlQuery);
				st.setString(1, library.getLibraryName());
				st.executeUpdate();
				/*add library to database */
				library.setId(getKey(library));
				library.setLastModified(getLibraryById(library.getId()).getLastModified());
				
				/*add library books to database*/
				lbInsert = connection.prepareStatement(sqlLibBook);
				for(LibraryBook lb: libBooks){
					/*audit message to be inserted also*/
					addLibAudit = new AuditTrailEntry(lb.getBook().getTitle(), new Timestamp(System.currentTimeMillis()), lb.getBook().getTitle() + ": Added");
					auditMessages.add(addLibAudit);
					
					/*actually insert book*/
					lbInsert.setInt(1, library.getId());
					lbInsert.setInt(2, lb.getBook().getId());
					lbInsert.setInt(3, lb.getQuantity());
					lbInsert.addBatch();
				}
				lbInsert.executeBatch();
				
				/*add audit trails to database*/
				stAudit = connection.prepareStatement(sqlAudit);
				for(AuditTrailEntry a : auditMessages){
					stAudit.setString(1, String.valueOf('L'));
					stAudit.setInt(2, library.getId());
					stAudit.setString(3, a.getMessage());
					stAudit.addBatch();
				}
				stAudit.executeBatch();

				try{
					connection.commit();
				}catch (SQLException e){
					logger.error("Library:" + library + " Failed, when commiting, to be Inserted into Database");
					connection.rollback();
				}
				
				connection.setAutoCommit(true);
				
			}catch(SQLException e){
				logger.error("Library :" + library + " Failed, when commiting, to be Inserted into Database");
				e.printStackTrace();
			}finally{
				try {
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
	/**
	 * Update Existing Library in Database depending
	 * on what values are changed
	 * @param update
	 * @throws IOException
	 */
	public void updateLibrary(Library update) throws IOException{
		Library oldLibrary = getLibraryById(update.getId());
		
		List<AuditTrailEntry> auditUpdate = new ArrayList<AuditTrailEntry>();
		AuditTrailEntry newUpdate = new AuditTrailEntry();
		
		
		boolean hasNewBooks = false;
		boolean hasNewQuan = false;
		boolean hasNewName = false;
		
		String updateMsg;
		
		int id = update.getId();
		String name = update.getLibraryName();
		List<LibraryBook> books = update.getBooks();
		
		/** ORL IMPLEMENTATION **/
		if(!oldLibrary.getLastModified().equals(update.getLastModified())){
			logger.error("***Updates Do Not Match***");
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setHeaderText(null);
			alert.setTitle("Save Failed");
			alert.setContentText("Last Modified does not match\nTry re-selecting Book from List\nFor fresh data");
				
			ButtonType okButton = new ButtonType("OK");
			alert.getButtonTypes().setAll(okButton);
				
			Optional<ButtonType> result = alert.showAndWait();
			if(result.get() == okButton){
				MasterController.getInstance().changeView(ViewType.LIB_DETAIL,ViewType.LIB_LIST,null);
			}
			return;
		}
		
		
		/*check values to be updates */
		if(!name.equals(oldLibrary.getLibraryName())){
			logger.error(oldLibrary.getLibraryName()+ " CHANGED");
			updateMsg = "Title changed From " + oldLibrary.getLibraryName() + " To " + name;
			newUpdate = new AuditTrailEntry(update.toString(), new Timestamp(System.currentTimeMillis()), updateMsg );
			auditUpdate.add(newUpdate);
			hasNewName = true;
		}
		
		/**CHECK FOR BOOK CHANGES
		 * IMPLEMENT ADDING AND DELTEING BOOKS FIRST**/
		List<LibraryBook> oldBooks = oldLibrary.getBooks();
		List<LibraryBook> newBooks = update.getBooks();
		List<LibraryBook>booksToAdd = new ArrayList<LibraryBook>();
		/*book elements have been added*/
		for(LibraryBook lb : newBooks){
			if(lb.isNewRecord()){
				logger.error("Will Add:" + lb);
				booksToAdd.add(lb);
				hasNewBooks = true;
			}
		}
		
		/*if pro-deletion is implemented put here*/
		
		/*Check if Quantities of Books have changed
		 * should only check books that are not new*/
		
		for(int ndx = 0; ndx < oldBooks.size(); ndx++){
			int newQuan = newBooks.get(ndx).getQuantity();
			int oldQuan = oldBooks.get(ndx).getQuantity();
			if (newQuan != oldQuan){
				logger.error(newBooks.get(ndx).getBook().getTitle() + " Quantity Changed From " +  oldQuan + " to " + newQuan);
				updateMsg = newBooks.get(ndx).getBook().getTitle() + " Quantity Changed From " +  oldQuan + " to " + newQuan;
				newUpdate = new AuditTrailEntry(update.toString(), new Timestamp(System.currentTimeMillis()), updateMsg );
				auditUpdate.add(newUpdate);
				hasNewQuan = true;		
			}
		}
		
		String sqlQueryLib = "UPDATE library SET"
				+ " library_name = ?"
				+ " WHERE id = ?";
		String sqlQueryQuan = "UPDATE library_book SET"
				+ " quantity = ?"
				+ " WHERE library_id = ? AND book_id = ?";
		
		String sqlQueryNewBooks = "INSERT library_book SET"
				+ " library_id = ? "
				+ ", book_id = ? "
				+ ", quantity = ? ";
		
		String sqlAudit = "INSERT audit_trail SET "
				+ "record_type = ?, "
				+ "record_id = ?, "
				+ "entry_msg = ?";
		PreparedStatement stQueryLib = null;
		PreparedStatement stQueryQuan = null;
		PreparedStatement stQueryBook = null;
		PreparedStatement stAudit = null;
		ResultSet rs = null;
		try{
			connection.setAutoCommit(false);
			/*if there is a new name */
			if(hasNewName){
				stQueryLib = connection.prepareStatement(sqlQueryLib);
				stQueryLib.setString(1, update.getLibraryName());
				stQueryLib.setInt(2, update.getId());
				stQueryLib.executeUpdate();
			}
			/*if there is new books*/
			if(hasNewBooks){
				stQueryBook = connection.prepareStatement(sqlQueryNewBooks);
				for(LibraryBook lb : booksToAdd){
					stQueryBook.setInt(1, update.getId());
					stQueryBook.setInt(2, lb.getBook().getId());
					stQueryBook.setInt(3, lb.getQuantity());
					stQueryBook.addBatch();
				}
				stQueryBook.executeBatch();
			}
			/*if quantity has changed*/
			if(hasNewQuan){
				stQueryQuan = connection.prepareStatement(sqlQueryQuan);
				for(LibraryBook lb: newBooks){
					stQueryQuan.setInt(1, lb.getQuantity());
					stQueryQuan.setInt(2, update.getId());
					stQueryQuan.setInt(3, lb.getBook().getId());
					stQueryQuan.addBatch();
				}
				stQueryQuan.executeBatch();
				
			}
			
			/*add audits*/
			stAudit = connection.prepareStatement(sqlAudit);
			for(AuditTrailEntry a : auditUpdate){
				stAudit.setString(1, String.valueOf('L'));
				stAudit.setInt(2, update.getId());
				stAudit.setString(3, a.getMessage());
				stAudit.addBatch();
			}
			
			stAudit.executeBatch();

			try{
				connection.commit();
			}catch (SQLException e){
				logger.error("Library:" + update + " Failed, when commiting UPDATE on Database");
				connection.rollback();
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(stQueryQuan != null) 
					stQueryQuan.close();
				if(stAudit != null)
					stAudit.close();
				if(stQueryBook != null)
					stQueryBook.close();
				if(stQueryLib != null)
					stQueryLib.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	/**
	 * Returns Id of Library in Database
	 * used when a new library is added to get its new id
	 * @param library
	 * @return id
	 */
	public int getKey(Library library) {
		// TODO Auto-generated method stub
		PreparedStatement st = null;
		ResultSet rs = null;
		int id = 0;
		
		String sqlQuery = "SELECT id FROM library WHERE library_name = ?";
		
		try {
			st = connection.prepareStatement(sqlQuery);
		    st.setString(1, library.getLibraryName());
			
			rs = st.executeQuery();
			rs.next();
			id = rs.getInt("id");
			
			//logger.error("New Library Id : " + id);
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
	 * Deletes complete library records from database
	 * including library book records and audit trails
	 * @param selected
	 */
	public void deleteLibrary(Library selected) {
		// TODO Auto-generated method stub
		int id = selected.getId();
		logger.error("Lib Delete ID: " + id);
		PreparedStatement st = null;
		PreparedStatement stDelBooks = null;
		PreparedStatement stAudit;
		ResultSet rs = null;
		
		String sqlQuery = "DELETE FROM library WHERE id = ?";
		String sqlBooks = "DELETE FROM library_book WHERE library_id = ?";
		String sqlAuditDel = "DELETE FROM audit_trail WHERE record_id = ? AND record_type = ?";
		try{
			connection.setAutoCommit(false);
			st = connection.prepareStatement(sqlQuery);
			st.setInt(1, id);
			st.executeUpdate();
			
			
			stDelBooks = connection.prepareStatement(sqlBooks);
			stDelBooks.setInt(1, id);
			stDelBooks.executeUpdate();
			
			
			stAudit = connection.prepareStatement(sqlAuditDel);
			stAudit.setInt(1,selected.getId());
			stAudit.setString(2, String.valueOf('L'));
			stAudit.executeUpdate();
			try{
				connection.commit();
			}catch(SQLException e){
				connection.rollback();
				logger.error("Error Commiting Delete to Library or Library Books in Database");
			}
		}catch(SQLException e){
			logger.error("Error Getting Libraries");
			e.printStackTrace();
		}finally{
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
	 * Return List of Audit Trail Entries from Database
	 * @param library
	 * @return audits
	 */
	public List<AuditTrailEntry> getAuditTrails(Library library) {
		List<AuditTrailEntry> audits =  new ArrayList<AuditTrailEntry>();
		PreparedStatement st = null;
		ResultSet rs = null;
		String sqlQuery = "SELECT * "
				+ "FROM audit_trail "
				+ "INNER JOIN library l ON l.id = record_id "
				+ "WHERE l.id = ? "
				+ "AND record_type = ?"
				+ "ORDER BY date_added ASC";
		try {
			st = connection.prepareStatement(sqlQuery);
			st.setInt(1,library.getId());
			st.setString(2, String.valueOf('L'));
			rs = st.executeQuery();
			
			while(rs.next()) {
				 AuditTrailEntry audit = new AuditTrailEntry(rs.getString("library_name"),
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
	
	/**
	 * Delete Library Book Record From Database
	 * Was to be used for real deletion but it 
	 * was decided not to implement
	 * @param selected
	 */
	public void deleteLibraryBook(LibraryBook selected, int libId) {
		// TODO Auto-generated method stub
		PreparedStatement st = null;
		PreparedStatement stAudit = null;
		
		String sqlQuery = "DELETE FROM library_book WHERE book_id = ? AND library_id = ?";
		String sqlAudit = "INSERT audit_trail SET "
				+ "record_type = ?, "
				+ "record_id = ?, "
				+ "entry_msg = ?";
	}
	
}

