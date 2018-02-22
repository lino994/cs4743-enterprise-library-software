package assign1;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import AuditTrailSQL.AuditTrailController;
import AuditTrailSQL.AuditTrailEntry;
import Authentication.Session;
import Authors.Author;
import Authors.AuthorEditDetailController;
import Authors.AuthorListController;
import Books.Book;
import Books.BookEditDetailController;
import Books.BookListController;
import GatewaySQL.AuthorTableGateway;
import GatewaySQL.BookTableGateway;
import GatewaySQL.GatewayException;
import GatewaySQL.LibraryTableGateway;
import Library.Library;
import Library.LibraryBook;
import Library.LibraryEditDetailController;
import Library.LibraryListController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;

public class MasterController {
	private static Logger logger = LogManager.getLogger();
	private static MasterController instance = null;
	
	//private ViewType oldView;
	private ViewType currentViewType;
	private Session currentSession;
	private AuthorEditDetailController EditControl;
	private BookEditDetailController bookEditControl;
	private LibraryEditDetailController libEditControl;
	
	private BorderPane root;
	private MenuBar menu;
	private Alert edAlert;
	private Alert saveAlert;
	private Author newRecord;
	private Author oldRecord;
	private Book newBookRecord;
	private Book oldBookRecord;
	private Library oldLibRecord;
	private Library newLibRecord;
	private boolean saveSuccess;
	private AuthorTableGateway authorGateway;
	private BookTableGateway bookGateway;
	private LibraryTableGateway libGateway;
	
	private MasterController(){
		try{
			authorGateway = new AuthorTableGateway();
			bookGateway = new BookTableGateway();
			libGateway = new LibraryTableGateway();
		}catch(GatewayException e){
			e.printStackTrace();
		}

	}
	public void setCurrentSession(Session session){
		currentSession = session;
	}
	
	public Session getCurrentSession(){
		return currentSession;
	}
	public void setNewRecord(Author newRecord){
		this.newRecord = newRecord;
	}
	
	public void setOldRecord(Author oldRecord){
		this.oldRecord = oldRecord;
	}
	
	public void setBookOldRecord(Book oldBookRecord){
		this.oldBookRecord = oldBookRecord;
	}
	
	public void setLibOldRecord(Library oldLibRecord){
		this.oldLibRecord = oldLibRecord;
	}
	
	public static MasterController getInstance() throws IOException{
		if(instance == null){
			instance = new MasterController();
		}
		return instance;
	}
	
	public ViewType getCurrentView(){
		if(currentViewType == null){
			currentViewType = ViewType.MAIN;
		}
		return currentViewType;
	}
	
	/**
	 * shows dialog box if user inputed an illegal value while saving
	 */
	public void checkSave(){
		saveAlert = new Alert(AlertType.INFORMATION);
		saveAlert.setHeaderText(null);
		saveAlert.setTitle("Save Failed");
		saveAlert.setContentText("You Inputted an Illegal Value Please Re-Enter Values");
		saveAlert.showAndWait();
	}
	
	public boolean changeView(ViewType oldView, ViewType vType, Object data) throws IOException {
		FXMLLoader loader = null;
		//this.oldView = oldView;
		currentViewType = vType;
		
		boolean stayHere = false;
		
		//logger.error("OLD VIEW " + oldView);
		//logger.error("NEW VIEW " + currentViewType);
		/*check for changes in library details*/
		/*check for changes in book detail*/
		if(oldView == ViewType.LIB_DETAIL && libEditControl != null){
			/*get new record*/
			newLibRecord = libEditControl.getEdits();
			/*compare new and old records*/
			if(newLibRecord.getId() != 0 && oldLibRecord.isDifferentFrom(newLibRecord) ){
				edAlert = new Alert(AlertType.CONFIRMATION);
				edAlert.setTitle("Library Record Changed");
				edAlert.setHeaderText(null);
				edAlert.setContentText("You have not saved.\n"
							+ "Would you like to save now?");
					
				/*Button Prompts*/
				ButtonType bSaveQuit = new ButtonType("Save & Quit");
				ButtonType bQuit = new ButtonType("Quit");
				ButtonType bCancel = new ButtonType("Cancel");
					
				edAlert.getButtonTypes().setAll(bSaveQuit, bQuit, bCancel);
				Optional<ButtonType> result = edAlert.showAndWait();
				if(result.get() == bSaveQuit){
					stayHere = false;
					try{
						saveSuccess = newLibRecord.validate(newLibRecord.getId()
								, newLibRecord.getLibraryName(), newLibRecord.getBooks());
						
						if(saveSuccess == false){
							checkSave();
							stayHere = true;
						}
							
					}catch(Exception e){
						checkSave();
						stayHere = true;	
					}
				}else if(result.get() == bQuit){
						stayHere = false;
				}else if(result.get() == bCancel){
						stayHere = true;
				}				
			}else if(newLibRecord.getId() == 0){
				edAlert = new Alert(AlertType.CONFIRMATION);
				edAlert.setTitle("New Library Record");
				edAlert.setHeaderText(null);
				edAlert.setContentText("You have not saved.\n"
							+ "Would you like to save now?");
					
				/*Button Prompts*/
				ButtonType bSaveQuit = new ButtonType("Save & Quit");
				ButtonType bQuit = new ButtonType("Quit");
				ButtonType bCancel = new ButtonType("Cancel");
					
				edAlert.getButtonTypes().setAll(bSaveQuit, bQuit, bCancel);
				Optional<ButtonType> result = edAlert.showAndWait();
				if(result.get() == bSaveQuit){
					stayHere = false;
					try{
						saveSuccess = newLibRecord.validate(newLibRecord.getId()
								, newLibRecord.getLibraryName(), newLibRecord.getBooks());
						
						if(saveSuccess == false){
							checkSave();
							stayHere = true;
						}
							
					}catch(Exception e){
						checkSave();
						stayHere = true;	
					}
				}else if(result.get() == bQuit){
						stayHere = false;
				}else if(result.get() == bCancel){
						stayHere = true;
				}
			}
		}
		if(oldView == ViewType.BOOK_DETAIL && bookEditControl != null){
			newBookRecord = bookEditControl.getEdits();
			
			if( newBookRecord.getId() != 0 && oldBookRecord.isDifferentFrom(newBookRecord) ){	
				edAlert = new Alert(AlertType.CONFIRMATION);
				edAlert.setTitle("Changes Detected in Author Record");
				edAlert.setHeaderText(null);
				edAlert.setContentText("You have not saved.\n"
							+ "Would you like to save now?");
					
				/*Button Prompts*/
				ButtonType bSaveQuit = new ButtonType("Save & Quit");
				ButtonType bQuit = new ButtonType("Quit");
				ButtonType bCancel = new ButtonType("Cancel");
					
				edAlert.getButtonTypes().setAll(bSaveQuit, bQuit, bCancel);
				Optional<ButtonType> result = edAlert.showAndWait();
				if(result.get() == bSaveQuit){
					stayHere = false;
					try{
						saveSuccess = newBookRecord.validate(newBookRecord.getTitle()
							, newBookRecord.getPublisher()
							, newBookRecord.getSummary()
							, newBookRecord.getDatePublished()
							, newBookRecord.getAuthor() );
						edAlert = new Alert(AlertType.INFORMATION);
						edAlert.setTitle("Save Failure");
						edAlert.setHeaderText(null);
						edAlert.setContentText("You have not saved.\n"
										+ "Would you like to save now?");
							
						if(saveSuccess == false){
							checkSave();
							stayHere = true;
						}
							
					}catch(Exception e){
							checkSave();
							stayHere = true;
							logger.error("Saving Error");
					}
				}else if(result.get() == bQuit){
						stayHere = false;
				}else if(result.get() == bCancel){
						stayHere = true;
				}
				
			}else if(newBookRecord.getId() == 0){
				edAlert = new Alert(AlertType.CONFIRMATION);
				edAlert.setTitle("New Book Record");
				edAlert.setHeaderText(null);
				edAlert.setContentText("You have not saved.\n"
							+ "Would you like to save now?");
					
				/*Button Prompts*/
				ButtonType bSaveQuit = new ButtonType("Save & Quit");
				ButtonType bQuit = new ButtonType("Quit");
				ButtonType bCancel = new ButtonType("Cancel");
					
				edAlert.getButtonTypes().setAll(bSaveQuit, bQuit, bCancel);
				Optional<ButtonType> result = edAlert.showAndWait();
				if(result.get() == bSaveQuit){
					stayHere = false;
					try{
						saveSuccess = newBookRecord.validate(newBookRecord.getTitle()
								, newBookRecord.getPublisher()
								, newBookRecord.getSummary()
								, newBookRecord.getDatePublished()
								, newBookRecord.getAuthor() );
						
						if(saveSuccess == false){
							checkSave();
							stayHere = true;
						}
							
					}catch(Exception e){
						checkSave();
						stayHere = true;	
					}
				}else if(result.get() == bQuit){
						stayHere = false;
				}else if(result.get() == bCancel){
						stayHere = true;
				}
			}
		}
		
		/*check for changes before exiting author detail*/
		if( oldView == ViewType.AUTHOR_DETAIL && EditControl != null ){
			newRecord = EditControl.getEdits();
			
			if( newRecord.getId() != 0 && oldRecord.isDifferentFrom(newRecord) ){	
				edAlert = new Alert(AlertType.CONFIRMATION);
				edAlert.setTitle("Changes Detected in Author Record");
				edAlert.setHeaderText(null);
				edAlert.setContentText("You have not saved.\n"
							+ "Would you like to save now?");
					
				/*Button Prompts*/
				ButtonType bSaveQuit = new ButtonType("Save & Quit");
				ButtonType bQuit = new ButtonType("Quit");
				ButtonType bCancel = new ButtonType("Cancel");
					
				edAlert.getButtonTypes().setAll(bSaveQuit, bQuit, bCancel);
				Optional<ButtonType> result = edAlert.showAndWait();
				if(result.get() == bSaveQuit){
					stayHere = false;
					try{
						saveSuccess = newRecord.save(newRecord.getFirstName()
							, newRecord.getLastName()
							, newRecord.getGender()
							, newRecord.getWebSite()
							, newRecord.getDOB() );
						edAlert = new Alert(AlertType.INFORMATION);
						edAlert.setTitle("Save Failure");
						edAlert.setHeaderText(null);
						edAlert.setContentText("You have not saved.\n"
										+ "Would you like to save now?");
							
						if(saveSuccess == false){
							checkSave();
							stayHere = true;
						}
							
					}catch(Exception e){
							checkSave();
							stayHere = true;
							logger.error("Saving Error");
					}
				}else if(result.get() == bQuit){
						stayHere = false;
				}else if(result.get() == bCancel){
						stayHere = true;
				}
				
			}else if(newRecord.getId() == 0){
				edAlert = new Alert(AlertType.CONFIRMATION);
				edAlert.setTitle("New Author Record");
				edAlert.setHeaderText(null);
				edAlert.setContentText("You have not saved.\n"
							+ "Would you like to save now?");
					
				/*Button Prompts*/
				ButtonType bSaveQuit = new ButtonType("Save & Quit");
				ButtonType bQuit = new ButtonType("Quit");
				ButtonType bCancel = new ButtonType("Cancel");
					
				edAlert.getButtonTypes().setAll(bSaveQuit, bQuit, bCancel);
				Optional<ButtonType> result = edAlert.showAndWait();
				if(result.get() == bSaveQuit){
					stayHere = false;
					try{
						saveSuccess = newRecord.save(newRecord.getFirstName()
								, newRecord.getLastName()
								, newRecord.getGender()
								, newRecord.getWebSite()
								, newRecord.getDOB() );
						
						if(saveSuccess == false){
							checkSave();
							stayHere = true;
						}
							
					}catch(Exception e){
						checkSave();
						stayHere = true;	
					}
				}else if(result.get() == bQuit){
						stayHere = false;
				}else if(result.get() == bCancel){
						stayHere = true;
				}
			}
			
			
		}
		
		/*
		 * if user decides to cancel out of exit it stays in same view
		 */
		if(stayHere){
			currentViewType = ViewType.AUTHOR_DETAIL;
			stayHere = false;
		}else{
			if(vType == ViewType.MAIN){
				logger.error("MAIN PRESSED");
				if(data == null){
					loader = new FXMLLoader(getClass().getResource("main.fxml"));
				}else{
					currentSession = (Session) data;
					loader = new FXMLLoader(getClass().getResource("main.fxml"));
					loader.setController(new AppMainController(currentSession));
				}
			}else if (vType == ViewType.AUTHOR_LIST){
				logger.error("AUTHOR LIST PRESSED");
				List<Author> authors = authorGateway.getAuthors();
		
				loader = new FXMLLoader(getClass().getResource("/Authors/authors.fxml"));
				loader.setController(new AuthorListController(authors));
				
			}else if(vType == ViewType.AUTHOR_AUDIT){
				logger.error("CHANGING TO AUTHOR AUDIT");
				loader = new FXMLLoader(getClass().getResource("/AuditTrailSQL/AuditTrailView.fxml"));
				Author authorData = (Author) data;
				List<AuditTrailEntry> auditData = authorData.getAuditTrails();
				loader.setController( new AuditTrailController( auditData ) );
				
			}else if(vType == ViewType.AUTHOR_DETAIL){
				logger.error("AUTHOR DETAIL CHOSEN");
				
				Author authorData = (Author) data;
				loader = new FXMLLoader(getClass().getResource("/Authors/authorEditDetailView.fxml"));
				EditControl = new AuthorEditDetailController(authorData);
				if(authorData.getId() != 0){
					oldRecord = authorGateway.getAuthorbyId(authorData.getId());
				}
				loader.setController(EditControl);
				
			}else if(vType == ViewType.BOOK_LIST){
				logger.error("CHANGING TO BOOK LIST VIEW");
				//List<Book> books = null;
				if(data != null){
					logger.error("Setting list after search");
					List<Book> bookSearch = (List<Book>) data;
					if(bookSearch.isEmpty()){
						logger.error("Search Empty ");
					}
					loader = new FXMLLoader(getClass().getResource("/Books/BookListView.fxml"));
					loader.setController(new BookListController(bookSearch));
				}else{
					List<Book> books = bookGateway.getBooks();
					loader = new FXMLLoader(getClass().getResource("/Books/BookListView.fxml"));
					loader.setController(new BookListController(books));
				}

			
			}else if(vType == ViewType.BOOK_DETAIL){
				logger.error("BOOK DETAIL CHOSEN");
				Book bookData = (Book) data;
				loader = new FXMLLoader(getClass().getResource("/Books/BookEditDetailView.fxml"));
				
				bookEditControl = new BookEditDetailController(bookData);
				if(bookData.getId() != 0){
					oldBookRecord = bookGateway.getBookById(bookData.getId());
				}
				loader.setController(bookEditControl);
			}else if(vType == ViewType.BOOK_AUDIT){
				logger.error("BOOK AUDIT CHOSEN");
				Book bookData = (Book) data;
				List<AuditTrailEntry> auditData = bookData.getAuditTrails();
				loader = new FXMLLoader(getClass().getResource("/AuditTrailSQL/AuditTrailView.fxml"));
				loader.setController( new AuditTrailController( auditData ) );
			
			
			}else if(vType == ViewType.LIB_LIST){
				List<Library> libraries = libGateway.getLibraries();
				loader = new FXMLLoader(getClass().getResource("/Library/LibraryListView.fxml"));
				loader.setController( new LibraryListController( libraries ) );
				
			}else if(vType == ViewType.LIB_DETAIL){
				Library selected = (Library) data;
				loader = new FXMLLoader(getClass().getResource("/Library/LibraryEditDetailView.fxml"));
				libEditControl = new LibraryEditDetailController( selected );
				if(selected.getId() != 0){
					oldLibRecord = libGateway.getLibraryById(selected.getId());
				}
				loader.setController(libEditControl);
			}else if(vType == ViewType.LIB_AUDIT){
				logger.error("Library Audit Chosen");
				Library libData = (Library) data;
				List<AuditTrailEntry> auditData = libData.getAuditTrails();
				loader = new FXMLLoader(getClass().getResource("/AuditTrailSQL/AuditTrailView.fxml"));
				loader.setController( new AuditTrailController( auditData ) );
			}
		
			Parent view = null;
			try{
				view = loader.load();
			}catch (IOException e){
				e.printStackTrace();
			}
			
			root.setCenter(view);
		}
		return false;
	}
	
	
	public BorderPane getRootPane(){
		return root;
	}
	
	public void setRootPane(BorderPane root){
		/**
		 * set top menu and menu controller so it is always present
		 */
		menu = null;
		try {
			menu = FXMLLoader.load(getClass().getResource("menu.fxml"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(menu == null){
			logger.error("Error Creating Menu");
		}
		root.setTop(menu);
		
		this.root = root;
	}
	public AuthorTableGateway getAuthorGateway() {
		return authorGateway;
	}

	public void setCarGateway(AuthorTableGateway authorGateway) {
		this.authorGateway = authorGateway;
	}
	
	/**
	 * clean up method to close gateways, etc.
	 */
	public void close() {
		authorGateway.close();
		bookGateway.close();
		libGateway.close();
	}
	public BookTableGateway getBookGateway() {
		// TODO Auto-generated method stub
		return bookGateway;
	}
	
	public LibraryTableGateway getLibraryTableGateway(){
		return libGateway;
	}
	
}
