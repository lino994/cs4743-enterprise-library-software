package assign1;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import util.CryptoStuff;
import Authentication.Session;
import Authentication.UserType;
import AuthenticationDB.AuthenticatorDB;
import AuthenticationDB.AuthenticatorRemote;
import Authors.Author;
import Authors.AuthorEditDetailController;
import Authors.AuthorListController;
import Books.Book;
import Library.Library;
import Library.LibraryBook;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Pair;

public class MenuController implements Initializable  {
	private static Logger logger = LogManager.getLogger();
	
	AuthenticatorRemote authenticator;
	UserType sessionType;
	Session currentSession;
	
	@FXML private MenuBar menuBar;			//menu bar
	
	@FXML private MenuItem menuMain;		//main view
	@FXML private MenuItem menuItem;		//author list
	@FXML private MenuItem menuExit;		//exit app
	@FXML private MenuItem menuLogin;		//login
	@FXML private MenuItem menuLogout;		//logout
	@FXML private MenuItem menuAddItem;		//add author
	@FXML private MenuItem bookList;		//book list
	@FXML private MenuItem addBook;			//add book
	@FXML private MenuItem libraryList;		//library list
	@FXML private MenuItem addLibrary;		//add library
	
	
	public MenuController(){
		authenticator = new AuthenticatorDB();			//authenticator for login / logout logic
		sessionType = UserType.NEW_USER;				//initial menu has restrictions until login
	}
	
	@FXML private void handleMenuAction(ActionEvent event) throws IOException {
		//provideAboutFunctionality();
		ViewType vType = MasterController.getInstance().getCurrentView();
		if(event.getSource() == menuItem) {
			MasterController.getInstance().changeView(vType,ViewType.AUTHOR_LIST, null);	
		} else if(event.getSource() == menuMain) {
			
			/*check is currentSession is active and their is a user logged in*/
			if(currentSession != null){
				MasterController.getInstance().changeView(vType,ViewType.MAIN, currentSession);
			}else{
				MasterController.getInstance().changeView(vType,ViewType.MAIN, null);
			}
			//return;
		}else if(event.getSource() == menuExit) {
			/*delete sessions in DB*/
			if(currentSession != null){
				authenticator.logout(currentSession);
			}
			System.exit(0);
		}else if(event.getSource() == menuLogin) {
			/*add actual login logic*/
			doLogin();
		}else if(event.getSource() == menuLogout) {
			/* delete session from database update GUI 
			 * and remove session from master controller*/
			doLogout();
			updateGUIRestrictions();
			MasterController.getInstance().changeView(vType,ViewType.MAIN, null);
		}else if(event.getSource() == menuAddItem) {
			MasterController.getInstance().changeView(vType,ViewType.AUTHOR_DETAIL, new Author());
			//return;
		}else if(event.getSource() == bookList){
			MasterController.getInstance().changeView(vType, ViewType.BOOK_LIST, null);
			//return;
		}else if(event.getSource() == addBook){
			MasterController.getInstance().changeView(vType, ViewType.BOOK_DETAIL, new Book());
			//return;
		}else if(event.getSource() == libraryList){
			MasterController.getInstance().changeView(vType, ViewType.LIB_LIST, new Library());
			//return;
		}else if(event.getSource() == addLibrary){
			MasterController.getInstance().changeView(vType, ViewType.LIB_DETAIL, new Library());
			//return;
		}
	}
	
	/* 
	 * delete current session from database
	 * and revert session type to new user
	 */
	private void doLogout(){
		if(currentSession != null){
			authenticator.logout(currentSession);
			sessionType = UserType.NEW_USER;
		}
	}
	private void doLogin(){
		Pair<String, String> userCred = LoginDialog.showLoginDialog();		//show login dialog
		
		if(userCred == null){
			return;
		}
		
		String userName = userCred.getKey();		//get username
		String pass =  userCred.getValue();			//get password
		//logger.error("userName is " + userName + ", password is " + pass);
		
		String pHash = CryptoStuff.sha256(pass);
		
		logger.error("hash: " + pHash);
		//authenticator.addUsersToDB();
		try{
			/*check credentials and set the session type to update restrictions*/
			currentSession = authenticator.loginCheck(userName, pHash);
			sessionType = currentSession.getSessionType();
			logger.error("User : " + currentSession.getUser().getName() + " Logged In!");
			logger.error("currentSession id = "+ currentSession.getSessionId());
			
		}catch(Exception e){
			/*if username and password where not found in the database*/
			Alert alert = new Alert(AlertType.WARNING);
			alert.getButtonTypes().clear();
			ButtonType buttonTypeOne = new ButtonType("OK");
			alert.getButtonTypes().setAll(buttonTypeOne);
			alert.setTitle("Login Failed");
			alert.setHeaderText("The username or password you provided do not match stored credentials.");
			alert.setContentText("Please Try Again!");
			alert.showAndWait();
			return;
		}
		updateGUIRestrictions();			//update menu restrictions
		try {
			if(MasterController.getInstance().getCurrentView() == ViewType.MAIN){
				MasterController.getInstance().changeView(ViewType.MAIN, ViewType.MAIN, currentSession);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("*****ERROR CHANGING LOGIN DETAILS IN MAIN***");
		}
		
		return;
	}
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		menuBar.setFocusTraversable(true);
		//set init restrictions
		updateGUIRestrictions();
	}
	
	/*  set the menu restrictions based
	 *  on the current session's user's role*/
	private void updateGUIRestrictions() {
		// TODO Auto-generated method stub

		if(sessionType == UserType.NEW_USER){
			menuLogin.setDisable(false);		//can login
			
			menuItem.setDisable(true);			//can't view authors
			menuLogout.setDisable(true);		//can't logout
			menuAddItem.setDisable(true);		//can't add authors
			bookList.setDisable(true);			//can't view book list
			addBook.setDisable(true);			//can't add books
			libraryList.setDisable(true);		//can't view library list
		    addLibrary.setDisable(true);		//can't add libraries
		    
		}else if(sessionType == UserType.ADMIN || sessionType == UserType.LIBRARIAN){
			logger.error("ADMIN UPDATING");
			menuLogin.setDisable(true);			//can't login twice!
			
			/*access to everything in menu*/
			menuItem.setDisable(false);
			menuExit.setDisable(false);
			menuLogout.setDisable(false);
			menuAddItem.setDisable(false);
			bookList.setDisable(false);
			addBook.setDisable(false);
			libraryList.setDisable(false);
			addLibrary.setDisable(false);
			
		}else if(sessionType == UserType.INTERN){
			menuLogin.setDisable(true);			//can't login twice
			menuAddItem.setDisable(true);		//can't add authors
			addBook.setDisable(true);			//can't add books
			addLibrary.setDisable(true);		//can't add libraries
			
			bookList.setDisable(false);			//can view book list
			libraryList.setDisable(false);		//can view libraries
			menuItem.setDisable(false);			//can view authors
			menuExit.setDisable(false);			//can exit
			menuLogout.setDisable(false);		//can logout
		}
	}
}
