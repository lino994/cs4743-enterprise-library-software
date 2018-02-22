package Authors;

import java.io.IOException;
import java.sql.Date;





import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import AuditTrailSQL.AuditTrailEntry;
import GatewaySQL.AuthorTableGateway;
import assign1.AppMain;
import assign1.MasterController;


/**
 * will hold Author Class Later 
 * @author linos
 *
 */
public class Author {
	private static Logger logger = LogManager.getLogger();
	
		/*values of author*/
	private int id;
	private String firstName;
	private String lastName;
	private Date DOB;
	private char gender;
	private String webSite;
	private LocalDateTime lastMod;
	
	private boolean isValid;
	
	/* empty constructor */
	public Author(){
		id = 0;
		firstName = "";
		lastName  = "";
		DOB = null;
		gender = ' ';
		webSite = "";
		isValid = true;
		lastMod = null;

	}
	
	/*constructor with no id */
	public Author (String firstName, String lastName, Date DOB, char gender, String webSite){
		this.firstName = firstName;
		this.lastName = lastName;
		this.DOB = DOB;
		this.gender = Character.toUpperCase(gender);
		this.webSite = webSite;	
		isValid = true;
	}
	
	/*constructor with id */
	public Author (int id, String firstName, String lastName, Date DOB, char gender, String webSite){
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.DOB = DOB;
		this.gender = Character.toUpperCase(gender);
		this.webSite = webSite;
		isValid = true;
	}
	
	/* auto-generated getters and setters */
	
	public void setLastModified(LocalDateTime lastMod){
		this.lastMod = lastMod;
	}
	public LocalDateTime getLastModified(){
		return lastMod;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public Date getDOB() {
		return DOB;
	}
	public void setDOB(Date DOB) {
		this.DOB = DOB;
	}
	public char getGender() {
		return gender;
	}
	public void setGender(char gender) {
		this.gender = gender;
	}
	public String getWebSite() {
		return webSite;
	}
	public void setWebSite(String webSite) {
		this.webSite = webSite;
	}
	public boolean isValid(){
		return isValid;
	}
	public void setIsValid(boolean isValid){
		this.isValid = isValid;
	}
	
	public boolean isDifferentFrom(Author target){
		boolean isDiff = true;
		if( firstName.equals(target.getFirstName()) 
				&& lastName.equals(target.getLastName())
				&& webSite.equals(target.getWebSite()) 
				&& DOB.equals(target.getDOB())
				&& gender == target.getGender() ){
			
			isDiff = false;
		}


		return isDiff;
	}
	/**
	 *  check if data meets author requirements
	 *  save new author data when edited 
	 * @throws IOException 
	 *  */
	public boolean save(String first, String last, char gen, String web, Date dateOfBirth) throws IOException{
		
			//check if id is valid
		if(id < 0){
			isValid = false;
		}
			//check if names are valid
		if(first.equals("") || last.equals("")){
			isValid = false;
		}
			//check if gender is valid
		gen = Character.toUpperCase(gen);
		if(gen != 'M' && gen != 'F' && gen != 'U'){
			isValid = false;
		}
			//check if website length is valid
		if(web.length() > 100){
			isValid = false;
		}
		
			//if valid then update or insert author in db
		if(isValid == true){
			setFirstName(first);
			setLastName(last);
			setWebSite(web);
			setGender(gen);
			setDOB(dateOfBirth);
			
			if(id == 0){	
				logger.error("Values Are Valid now Inserting");
				MasterController.getInstance().getAuthorGateway().insertAuthor(this);
				logger.error("id after insert: " + getId());
			}else{
				logger.error("Values Are Valid now Updating");
				MasterController.getInstance().getAuthorGateway().updateAuthor(this);
			}
		}
		return isValid;
	}
	
	public List<AuditTrailEntry> getAuditTrails(){
		List<AuditTrailEntry> audits = null;
		try {
			audits = MasterController.getInstance().getAuthorGateway().getAuditTrails(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return audits;
	}
		
	/* what will be shown in the list view */		 
	@Override
	public String toString() {
		return lastName + ", " + firstName;
	}
	
	public String getRecordDesc(){
		return firstName + ", " + lastName;
	}
}
