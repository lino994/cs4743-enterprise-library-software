package Books;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import assign1.MasterController;
import assign1.ViewType;
import Authentication.Session;
import Authentication.UserType;
import Authors.Author;

public class BookListController implements Initializable {
	private static Logger logger = LogManager.getLogger();
	
	@FXML private ListView<Book> bookListView;
	@FXML private TextField edBookName;
	@FXML private TextField edPublisher;
	@FXML private DatePicker edDatePublished;
	@FXML private Button bBookDelete;
	@FXML private Button bSearch;
	
	private ObservableList<Book> items;
	private List<Book> books;
	
	public BookListController(List<Book> books){
		this.books = books;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		Session session;
		try {
			session = MasterController.getInstance().getCurrentSession();
			setPermissions(session);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("ERROR: Session Not FOUND when Accessing Book List");
		}
		logger.error("Book List Controller Started");
	    items = bookListView.getItems();
	    for(Book b : books){
	    	items.add(b);
	    }

	}
	
	/*set permissions so interns and librarians cannot delete*/
	public void setPermissions(Session session){
		UserType type = session.getUser().getUserType();
		if(type == UserType.LIBRARIAN || type ==UserType.INTERN){
			bBookDelete.setDisable(true);
		}
		
	}
	public void handleMouseClick(MouseEvent event) throws IOException{
		if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2){			
			Book selected = bookListView.getSelectionModel().getSelectedItem();		//checks author selected
			logger.error("Pressed : " + selected + " For Detailed View");
			MasterController.getInstance().changeView(ViewType.BOOK_LIST, ViewType.BOOK_DETAIL, selected);
		}
	}
	
	public void handleButtonAction(Event event) throws IOException{
		if(event.getSource() == bBookDelete){
			Book selected = bookListView.getSelectionModel().getSelectedItem();
			if(selected != null){
				Alert bookDelAlert = new Alert(AlertType.CONFIRMATION);
				bookDelAlert.setTitle("Delete Book?");
				bookDelAlert.setHeaderText(null);
				bookDelAlert.setContentText("Are you sure you want to delete this book?");
				Optional<ButtonType> result = bookDelAlert.showAndWait();
				
				if(result.get() == ButtonType.OK){
					MasterController.getInstance().getBookGateway().deleteBook(selected);
					items = bookListView.getItems();
					logger.error("Removed : "+ selected + " From List!");
					items.remove(selected);
				}else{
					logger.error("Delete on " + selected + "Aborted!");
				}
			}else{
				logger.error("****No Book Selected to Delete!****");
			}
		}else if(event.getSource() == bSearch){
			initSearch();
		}
}

	private void initSearch() throws IOException{
		List<Book> bookSearch = null;
		String bookName = edBookName.getText();
		String publisher = edPublisher.getText();
		LocalDate datePub = edDatePublished.getValue();
	
		/*eliminate trailing and leading white space*/
		bookName = bookName.trim();
		publisher = publisher.trim();

		/*get results from database*/
		if(datePub == null){	//date is empty
			bookSearch = MasterController.getInstance().getBookGateway().search(bookName, publisher,  null);
		}else{					//date is not empty
			bookSearch = MasterController.getInstance().getBookGateway().search(bookName, publisher,  Date.valueOf(datePub));
		}
		
		/*check if all fields where empty to reload to default*/
		if(bookSearch == null){
			MasterController.getInstance().changeView(ViewType.BOOK_LIST, ViewType.BOOK_LIST, null);
		}else{
			/*check if no results where found*/
			if(bookSearch.isEmpty()){
				Alert edAlert = new Alert(AlertType.INFORMATION);
				edAlert.setTitle("No Results Found!");
				edAlert.setHeaderText(null);
				edAlert.setContentText("No matches found.. Please Try Again!");
				edAlert.show();
				MasterController.getInstance().changeView(ViewType.BOOK_LIST, ViewType.BOOK_LIST, null);
			}else{
				MasterController.getInstance().changeView(ViewType.BOOK_LIST, ViewType.BOOK_LIST, bookSearch);
			}
		}
	}
}//end class