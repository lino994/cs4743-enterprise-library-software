package Library;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import AuditTrailSQL.AuditTrailEntry;
import assign1.MasterController;


public class Library {
	private static Logger logger = LogManager.getLogger();
	private int id;
	private String libraryName;
	private List<LibraryBook> books;
	private LocalDateTime lastModified;
	private boolean isValid;
	
	public Library(){
		id = 0;
		libraryName = "";
		books = null;
		isValid = true;
	}
	public Library (String libraryName, List<LibraryBook> books){
		id = 0;
		this.books = books;
		isValid = true;
		
	}
	public Library(int id, String libraryName, List<LibraryBook> books, LocalDateTime lastModified){
		this.id = id;
		this.libraryName = libraryName;
		this.books = books;
		this.lastModified = lastModified;
		isValid = true;
	}
	
	public boolean validate(int id, String name, List<LibraryBook> bookCheck) throws IOException{
		if(id < 0){
			isValid = false;
			logger.error("id fault");
		}
		
		if(name.equals("")){
			isValid = false;
			logger.error("name fault");
		}
		
		for(LibraryBook b: bookCheck){
			if(b.getQuantity() < 0){
				isValid = false;
				logger.error("quantity fault");
			}
		}
		if(isValid == true){
			setLibraryName(name);
			setBooks(bookCheck);
			if(id == 0){
				/**Insert Book **/
				MasterController.getInstance().getLibraryTableGateway().insertLibrary(this);
				logger.error("Library " + toString() + " to be inserted");
			}else{
				/**Update Book **/
				MasterController.getInstance().getLibraryTableGateway().updateLibrary(this);
				logger.error("Library " + toString() + " to be updated");
			}
		}
		
		return isValid;
	}
	
	public boolean getIsValid(){
		return isValid;
	}
	
	public void setIsValid(boolean isValid){
		this.isValid = isValid;
	}
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLibraryName() {
		return libraryName;
	}

	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}

	public List<LibraryBook> getBooks() {
		return books;
	}

	public void setBooks(List<LibraryBook> books) {
		this.books = books;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}
	
	public String toString(){
		return libraryName;
	}
	
	public List<AuditTrailEntry> getAuditTrails(){
		List<AuditTrailEntry> audits = null;
		try {
			audits = MasterController.getInstance().getLibraryTableGateway().getAuditTrails(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return audits;
	}
	public boolean isDifferentFrom(Library newLibRecord) {
		// TODO Auto-generated method stub
		boolean isDiff = true;
		if( libraryName.equals(newLibRecord.getLibraryName()) &&
						books.equals(newLibRecord.getBooks()) &&
						id == newLibRecord.getId() ){
			
			isDiff = false;
		}
		return isDiff;
	}
}
