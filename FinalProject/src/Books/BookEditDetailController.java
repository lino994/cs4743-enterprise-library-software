package Books;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import assign1.MasterController;
import assign1.ViewType;
import AuditTrailSQL.AuditTrailEntry;
import Authentication.Session;
import Authentication.UserType;
import Authors.Author;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;


public class BookEditDetailController implements Initializable{
	private static Logger logger = LogManager.getLogger();
	
	@FXML private Button bSaveBookBtn;
	@FXML private Button bAuditBook;
	@FXML private TextField edTitle;
	@FXML private TextField edPublisher;
	@FXML private DatePicker edDatePub;
	@FXML private TextArea edSumText;
	@FXML private ComboBox<Author> edAuthorPicker; 
	
	private Book book;
	private AuditTrailEntry bookAudit;
	boolean saveSuccess;
	
	/*GUI Fields*/
	private int id;
	private String title;
	private String publisher;
	private String summary;
	private LocalDate datePublished;
	private Author author;
	
	public BookEditDetailController(Book book){
		this.book = book;
	}
	public void handleButtonAction(Event event) throws IOException{
		if(event.getSource() == bSaveBookBtn){
			try{
				logger.error("SAVE PRESSED");
				saveSuccess = book.validate(edTitle.getText(), edPublisher.getText()
						, edSumText.getText()
						, Date.valueOf(edDatePub.getValue()) 
						, edAuthorPicker.getValue());
				
				if(saveSuccess == false){
					MasterController.getInstance().checkSave();
				}
				MasterController.getInstance().setBookOldRecord(book);
			}catch(StringIndexOutOfBoundsException  | NullPointerException e){
				MasterController.getInstance().checkSave();
				logger.error("****BOOK Save Aborted Date Empty Try Again*****");
			}
			
			if(book.isValid() == false){
				logger.error("***AFTER SAVE BOOK Values Not Valid: Save Aborted***");
				book.setIsValid(true);
			}
		}else if (event.getSource() == bAuditBook){
			logger.error("AUDIT PRESSSED");
			MasterController.getInstance().changeView(ViewType.BOOK_DETAIL,ViewType.BOOK_AUDIT, book);
		}
	}
	
	public Book getEdits(){
		Book edit = new Book();
		try{
			edit = new Book(edTitle.getText(), edPublisher.getText()
					, Date.valueOf(edDatePub.getValue())
					, edSumText.getText() , edAuthorPicker.getValue());
			edit.setId(book.getId());
			edit.setLastModified(book.getLastModified());
		}catch(StringIndexOutOfBoundsException  | NullPointerException e){
			logger.error("EDIT NOT FOUND");
			edit = new Book();
		}
		
		return edit;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		logger.error("Book Detail View Initiated");
		id = book.getId();
		Session session;
		try {
			session = MasterController.getInstance().getCurrentSession();
			setPermissions(session);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("ERROR: Session Not FOUND when Accessing Book List");
		}
		//edAuthorPicker = new ComboBox<Author>();
		try {
			List<Author> authors = MasterController.getInstance().getAuthorGateway().getAuthors();
			ObservableList<Author> items = FXCollections.observableArrayList();
			for(Author a: authors){
				items.add(a);
			}
			edAuthorPicker.setItems(items);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Could Not Get Author List For Book ComboBox");
			e.printStackTrace();
		}

		if(id != 0){
			title = book.getTitle();
			publisher = book.getPublisher();
			summary = book.getSummary();
			datePublished = book.getDatePublished().toLocalDate();
			author = book.getAuthor();
			
			edTitle.setText(title);
			edPublisher.setText(publisher);
			edSumText.setText(summary);
			edDatePub.setValue(datePublished);
			edAuthorPicker.setValue(author);
			
			bookAudit = new AuditTrailEntry(title, null, "MESSAGE");
		}else{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText(null);
			alert.setContentText("New BOOKS have no Audit Trail"
					+ "\nPress SAVE on a valid input first!");
			alert.showAndWait();
		}
	}
	
	private void setPermissions(Session session) {
		// TODO Auto-generated method stub
		UserType type = session.getUser().getUserType();
		
		if(type ==UserType.INTERN){
				bSaveBookBtn.setDisable(true);
				//bAuditBook.setDisable(true);
				edTitle.setDisable(true);
				edPublisher.setDisable(true);
				edDatePub.setDisable(true);
				edSumText.setDisable(true);
				edAuthorPicker.setDisable(true);
		}	
	}

}
