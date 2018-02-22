package Authors;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import AuditTrailSQL.AuditTrailEntry;
import Authentication.Session;
import Authentication.UserType;
import assign1.MasterController;
import assign1.ViewType;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;

public class AuthorEditDetailController implements Initializable {
	private static Logger logger = LogManager.getLogger();
	
	/*Fields in GUI*/
	@FXML private Button edSaveBtn;
	@FXML private Button bAudit;
	@FXML private TextField edFirstName;
	@FXML private TextField edLastName;
	@FXML private TextField edGender;
	@FXML private TextField edWebsite;
	@FXML private DatePicker edDatePicker;
	
	private Author author;			//author to be displayed/updated/inserted
	private AuditTrailEntry authorAudit;
	boolean saveSuccess;
	
	/*GUI Fields Values*/
	private int id;
	private String authorFirstName;
	private String authorLastName;
	private LocalDate DOB;
	private Date sqlDOB;
	private char gender;
	private String website;

	public AuthorEditDetailController(Author author){
		this.author = author;
	}
	
	public Author getEdits(){
		Author edit = new Author();
		try{
			edit = new Author(edFirstName.getText(), edLastName.getText(),
					Date.valueOf(edDatePicker.getValue()),
							 edGender.getText().charAt(0),
							 edWebsite.getText());
			edit.setId(author.getId());
			edit.setLastModified(author.getLastModified());
		}catch(StringIndexOutOfBoundsException  | NullPointerException e){
			logger.error("EDIT NOT FOUND");
			edit = new Author();
		}

		return edit;
	}

	/**
	 * Populate List View of Author Detail View
	 */
	public void handleButtonAction(Event event) throws IOException{
		if(event.getSource() == edSaveBtn){
			logger.error("Pressed : Save Button");
				/* checks and updates values */			
			
			/**
			 * check for exception b/c of empty Gender or Date
			 * which cause exceptions when empty instead let the user try again
			 * if they are not empty they are sent to the model Author to check 
			 * for if all the values submitted are valid
			 */
			try{
				saveSuccess = author.save(edFirstName.getText(), edLastName.getText(), edGender.getText().charAt(0), edWebsite.getText(), Date.valueOf(edDatePicker.getValue()));
				if(saveSuccess == false){
					MasterController.getInstance().checkSave();
				}
				
				MasterController.getInstance().setOldRecord(author);

			}catch(StringIndexOutOfBoundsException  | NullPointerException e){
				MasterController.getInstance().checkSave();
				logger.error("****Save Aborted Gender and/or Date Empty Try Again*****");
			}
			
				/* reset is valid to true so the user is allowed to retry 
				 * the submission of data
				 */
			if(author.isValid() == false){
				logger.error("***Values Not Valid: Save Aborted***");
				author.setIsValid(true);

			}
			
		}else if(event.getSource() == bAudit){
			logger.error("AUDIT TRAIL BUTTON PRESSED!");
			MasterController.getInstance().changeView(ViewType.AUTHOR_DETAIL,ViewType.AUTHOR_AUDIT, author);
			
		}
	}


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
			id = author.getId();
			Session session;
			try {
				session = MasterController.getInstance().getCurrentSession();
				setPermissions(session);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				logger.error("ERROR: Session Not FOUND when Accessing Book List");
			}
			if(id != 0){
					/*grab author info to be displayed when doing detail view*/
				authorFirstName = author.getFirstName();
				authorLastName = author.getLastName();
				gender = author.getGender();
				website = author.getWebSite();
				sqlDOB = author.getDOB();
				DOB = sqlDOB.toLocalDate();
					
					/*set text in fields to be displayed in detailed view*/
				edFirstName.setText(authorFirstName);
				edLastName.setText(authorLastName);
				edGender.setText(gender + "");
				edWebsite.setText(website);
				edDatePicker.setValue(DOB);
				
					/* 
					 * set Label for audit trail entry
					 * PLACEHOLDER FOR NOW
					 * */
				authorAudit = new AuditTrailEntry(authorFirstName + " " + authorLastName, null, "MESSAGE");
				logger.error("Last Modified : " + author.getLastModified());
			}else{
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Information");
				alert.setHeaderText(null);
				alert.setContentText("New Authors have no Audit Trail"
						+ "\nPress SAVE on a valid input first!");
				alert.showAndWait();
			}
		
		logger.error("Author Detailed View Initiated");
	}
	
	private void setPermissions(Session session) {
		// TODO Auto-generated method stub
		UserType type = session.getUser().getUserType();
		/*intern cannot edit details only view*/
		if(type ==UserType.INTERN){
			edSaveBtn.setDisable(true);
			//bAudit.setDisable(true);
			edFirstName.setDisable(true);
			edLastName.setDisable(true);
			edGender.setDisable(true);
			edWebsite.setDisable(true);
			edDatePicker.setDisable(true);	
		}	
	}

}