package Authors;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Authentication.Session;
import Authentication.UserType;
import assign1.AppMain;
import assign1.MasterController;
import assign1.ViewType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class AuthorListController implements Initializable  {
	private static Logger logger = LogManager.getLogger();
	@FXML private ListView<Author> authorListView;
	@FXML private Button bDelete;
	private ObservableList<Author> items; 
	private List<Author> authors;
	
	public AuthorListController(List <Author> authors){
		this.authors = authors;
	}
	
	/**
	 * Will load the view for author's detail view
	 * @param author
	 * @throws IOException
	 */
	public void displayAuthor(Author author) throws IOException{
		MasterController.getInstance().changeView(ViewType.AUTHOR_LIST, ViewType.AUTHOR_DETAIL, author);
	}

	/**
	 * Initialize the author list view
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		logger.error("Author List Controller Started");
		Session session;
		try {
			session = MasterController.getInstance().getCurrentSession();
			setPermissions(session);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("ERROR: Session Not FOUND when Accessing Book List");
		}
	    items = authorListView.getItems();
	    	//add authors to listview
	    for(Author a : authors){
	    	items.add(a);
	    }
	    
		//logger.error("List View Populated");
	}
	
	private void setPermissions(Session session) {
		// TODO Auto-generated method stub
		UserType type = session.getUser().getUserType();
		
		if(type == UserType.LIBRARIAN || type ==UserType.INTERN){
			bDelete.setDisable(true);
		}	
	}

	public void handleMouseClick(MouseEvent event) throws IOException{
		if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2){			
			Author selected = authorListView.getSelectionModel().getSelectedItem();		//checks author selected
			logger.error("Pressed : " + selected + " For Detailed View");
			displayAuthor(selected);													//will display the detail view of author
			
		}
	}
	
	/**
	 * checks when user presses delete an author from the 
	 * list provided
	 * @param event
	 * @throws IOException
	 */
	public void handleButtonAction(Event event) throws IOException{
		if(event.getSource() == bDelete){
			//logger.error("Pressed : Delete Button Button");
				/* checks and updates values */
			Author selected = authorListView.getSelectionModel().getSelectedItem();
			if(selected != null){
				
				/**
				 * show user a dialog box to confirm deletion
				 */
				Alert deleteAlert = new Alert(AlertType.CONFIRMATION);
				deleteAlert.setTitle("Delete Author?");
				deleteAlert.setHeaderText(null);
				deleteAlert.setContentText("Are you sure you want to delete\n" +selected + "\nfrom the database?");
				Optional<ButtonType> result = deleteAlert.showAndWait();
				if(result.get() == ButtonType.OK){
					MasterController.getInstance().getAuthorGateway().deleteAuthor(selected); 			//deletes author in database
					items = authorListView.getItems();						
					logger.error("Removed : "+ selected + " From List!");
					items.remove(selected);		
				}else{
					logger.error("Delete on " + selected + "Aborted!");
				}
			}else{
				logger.error("**** Error : No Author Selected*****");
			}
		}
	}
}
		
