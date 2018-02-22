package assign1;
/*******
 * This controller is used when a user is logged in to show
 * their information in the main page for the current session
 * so this is only set as a controller when the main page is active\
 * and their is a user loggged in
 * */
import java.net.URL;
import java.util.ResourceBundle;

import Authentication.Session;
import Authentication.UserType;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

/*only used when a user is logged in*/
public class AppMainController implements Initializable{
	private Session currentSession;
	@FXML private Label userRealNameTag;
	@FXML private Label userNameTag;
	@FXML private Label userTypeTag;
	@FXML private Label sessionTag;
	@FXML private Label loggedInTag;
	
	public AppMainController(Session currentSession){
		this.currentSession = currentSession;
	}
	
	/**
	 *update the user info in the main page  
	 *when the main view is initalized with this controller
	 *and a user logged in 
	 **/
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		updateStage();
	}
	
	
	/* set text for labels in main view */
	public void updateStage(){
		UserType currentType = currentSession.getUser().getUserType();
		
		loggedInTag.setText("Welcome, " + currentSession.getUser().getName());
		String type = "";
		userRealNameTag.setText("Name:		"+currentSession.getUser().getName());
		userNameTag.setText("Username:	" + currentSession.getUser().getUserName());
		if(currentType == UserType.ADMIN){
			type = "Administrator";
		}else if(currentType == UserType.LIBRARIAN){
			type = "Librarian";
		}else if(currentType == UserType.INTERN){
			type = "Intern";
		}
		userTypeTag.setText("Status:		"+ type);
		sessionTag.setText("Session Id: 	"+ currentSession.getSessionId());
	}
}
