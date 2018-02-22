package Library;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Authentication.Session;
import Authentication.UserType;
import Books.Book;
import assign1.MasterController;
import assign1.ViewType;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class LibraryListController implements Initializable{
	private static Logger logger = LogManager.getLogger();
	@FXML private Button bLibDelete;
	@FXML private ListView<Library> libListView;
	
	private List<Library> libraries;
	private ObservableList<Library> items;
	
	public LibraryListController(List<Library> libraries) {
		// TODO Auto-generated constructor stub
		this.libraries = libraries;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		logger.error("Library List Controller Started");
		Session session;
		try {
			session = MasterController.getInstance().getCurrentSession();
			setPermissions(session);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("ERROR: Session Not FOUND when Accessing Book List");
		}
	    items =libListView.getItems();
	    	//add library to listview
	    for(Library l : libraries){
	    	//logger.error("Book Added " + b.toString());
	    	items.add(l);
	    }
	}
	private void setPermissions(Session session) {
		// TODO Auto-generated method stub
		UserType type = session.getUser().getUserType();
		
		if(type == UserType.LIBRARIAN || type ==UserType.INTERN){
			bLibDelete.setDisable(true);
		}	
	}
	public void handleMouseClick(MouseEvent event) throws IOException{
		if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2){			
			Library selected = libListView.getSelectionModel().getSelectedItem();		//checks author selected
			logger.error("Pressed : " + selected);
			MasterController.getInstance().changeView(ViewType.LIB_LIST, ViewType.LIB_DETAIL, selected);
		}
	}
	
	public void handleButtonAction(Event event) throws IOException{
		if(event.getSource() == bLibDelete){
			Library selected = libListView.getSelectionModel().getSelectedItem();
			
			if(selected != null){
				Alert libDelAlert = new Alert(AlertType.CONFIRMATION);
				libDelAlert.setTitle("Delete Library?");
				libDelAlert.setHeaderText(null);
				libDelAlert.setContentText("Are you sure you want to delete this Library?");
				Optional<ButtonType> result = libDelAlert.showAndWait();
				if(result.get() == ButtonType.OK){
					logger.error("Deleteing Selected " + selected);
					MasterController.getInstance().getLibraryTableGateway().deleteLibrary(selected);
					items = libListView.getItems();
					logger.error("Removed : "+ selected + " From List!");
					items.remove(selected);
				}else{
					logger.error("Delete on " + selected + "Aborted!");
				}
			}else{
				logger.error("****No Library Selected to Delete!****");
			}
		}
	}
}
