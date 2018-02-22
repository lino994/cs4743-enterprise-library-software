package Books;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import assign1.MasterController;
import AuditTrailSQL.AuditTrailEntry;
import Authors.Author;

public class Book {
		/*values of Book*/
	private static Logger logger = LogManager.getLogger();
	private int id;
	private String title;
	private String publisher;
	private String summary;
	private Author author;
	private Date datePublished;
	private LocalDateTime lastModified;
	boolean isValid;
	
	public Book(){
		id = 0;
		title = "";
		publisher = "";
		datePublished = null;
		summary = "";
		author = null;
		lastModified = null;
		isValid = true;
	}
		/*constructor with no id */
	public Book (String title, String publisher, Date datePublished, 
			String summary, Author author){
		//int id = 0;
		this.title = title;
		this.publisher = publisher;
		this.datePublished = datePublished;
		this.summary = summary;
		this.author = author;
		isValid = true;
	}
	
	public Book (int id, String title, String publisher, Date datePublished, 
			String summary, Author author){
		this.id = id;
		this.title = title;
		this.publisher = publisher;
		this.datePublished = datePublished;
		this.summary = summary;
		this.author = author;
		isValid = true;
	}
	
	public boolean validate(String title, String publisher
			, String summary, Date datePublished, Author author) throws IOException{
		if( id < 0){
			isValid = false;
		}
		
		if(title.equals("") || publisher.equals("") || summary.equals("")){
			isValid = false;
		}
		
		if(author.getId() <= 0){
			isValid = false;
		}
		
		if(isValid == true){
			setTitle(title);
			setPublisher(publisher);
			setSummary(summary);
			setAuthor(author);
			setDatePublished(datePublished);
			
			if(id == 0){
				logger.error("BOOK Values Valid INSERTING");
				MasterController.getInstance().getBookGateway().insertBook(this);
				logger.error("id after insert: " + getId());
			}else{
				logger.error("BOOK Values Valid UPDATING");
				MasterController.getInstance().getBookGateway().updateBook(this);
			}
		}
		return isValid;
	}
	@Override
	public String toString(){
		return "Book: "+ title+"\nAuthor : " + author.getRecordDesc();
	}
	public void setIsValid(boolean isValid){
		this.isValid = isValid;
	}
	public boolean isValid(){
		return isValid;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public Date getDatePublished() {
		return datePublished;
	}
	public void setDatePublished(Date datePublished) {
		this.datePublished = datePublished;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public Author getAuthor() {
		return author;
	}
	public void setAuthor(Author author) {
		this.author = author;
	}
	public LocalDateTime getLastModified() {
		return lastModified;
	}
	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}
	
	public List<AuditTrailEntry> getAuditTrails() {
		// TODO Auto-generated method stub
		List<AuditTrailEntry> audits = null;
		try {
			audits = MasterController.getInstance().getBookGateway().getAuditTrails(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return audits;
	}
	
	
	public boolean isDifferentFrom(Book target) {
		// TODO Auto-generated method stub
		boolean isDiff = true;
		if( title.equals(target.getTitle()) 
				&& publisher.equals(target.getPublisher())
				&& author.getId() == target.getAuthor().getId()
				&& summary.equals(target.getSummary()) 
				&& datePublished.equals(target.datePublished) ){
			isDiff = false;
		}
		return isDiff;
	}
}
