package Library;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.itextpdf.text.*;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import Authentication.Session;
import Authentication.UserType;
import Books.Book;
import assign1.MasterController;
import assign1.ViewType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;

public class LibraryEditDetailController implements Initializable{
	private static Logger logger = LogManager.getLogger();
	public static final String DEST = "Report.pdf";
	
	private Library library;
	private int id;
	private String libName;
	private List<LibraryBook> books;
	private ObservableList<LibraryBook> items;
	
	/** VIEW Variables **/
	@FXML private TextField edLibTitle;
	@FXML private Button bSaveLib;
	@FXML private Button bAuditLib;
	@FXML private Button bInvReport;
	@FXML private Button bAddBook;
	@FXML private Button bDelLibBook;
	@FXML private Button bAddQuantity;
	@FXML private Button bSubQuantity;
	@FXML private ListView<LibraryBook> libBookList;
	
	private boolean saveSuccess;
	private boolean newBookValid;
	private boolean newBookExist;
	private int indexOfExisting;
	public LibraryEditDetailController(Library library){
		this.library = library;
	}
	
	public void handleButtonAction(Event event) throws IOException, DocumentException{
		/*HANDLE BUTTON ACTIONS*/
		if(event.getSource() == bSaveLib){
			logger.error("Save Library Button Pressed");
			try{
				logger.error("Name : " + edLibTitle.getText());
				saveSuccess = library.validate(id, edLibTitle.getText(), books);
				
				if(saveSuccess == false){
					MasterController.getInstance().checkSave();
					library.setIsValid(true);
				}
				MasterController.getInstance().setLibOldRecord(library);
			}catch(StringIndexOutOfBoundsException  | NullPointerException e){
				e.printStackTrace();
				MasterController.getInstance().checkSave();
				logger.error("****Library Save Aborted No Books in Library*****");
			}
			
			if(library.getIsValid()==false){
				library.setIsValid(true);
			}
		}else if(event.getSource() == bAddBook){
			logger.error("Add Book Button Pressed");
			LibraryBook newLibBook = showNewBookDialog();
			if(newBookValid == true && newBookExist != true){
				addToBookList(newLibBook);
			}else if(newBookValid == false){
				newBookValid = true;
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Input Not Valid");
				alert.setHeaderText(null);
				alert.setContentText("Inputs chosen are not valid please try again");
				alert.showAndWait();
			/*book entered is new and had a quantity of 0*/
			} 
			if(newBookValid == true && newBookExist == true){ /**book is valid but already in library just update quantity */
				newBookExist = false;
				books.set(indexOfExisting, newLibBook);
				items = libBookList.getItems();
				items.set(indexOfExisting, newLibBook);
				indexOfExisting = 0;
			}else if(newBookExist == true && newLibBook != null && newLibBook.getQuantity() == 0){
				newBookExist = false;
				deleteLibraryBook(newLibBook);
				indexOfExisting = 0;
			}
		}else if(event.getSource() == bAddQuantity){
			logger.error("Add to Selected Button Pressed");
			addSubQuantity(1);
			
		}else if(event.getSource() == bSubQuantity){
			logger.error("Subtract from Selected Button Pressed");
			addSubQuantity(-1);
			
		}else if(event.getSource() == bAuditLib){
			logger.error("Audit Entries Button Pressed");
			MasterController.getInstance().changeView(ViewType.LIB_DETAIL, ViewType.LIB_AUDIT, library);
		}else if(event.getSource() == bInvReport){
			logger.error("Invoice Report Button Pressed");
			createInvoiceReport();
		}else if(event.getSource() == bDelLibBook){
			logger.error("Delete Book from Library Button Pressed");
			deleteLibraryBook(libBookList.getSelectionModel().getSelectedItem());
		}
		
	}
	public void createInvoiceReport() throws FileNotFoundException, DocumentException{
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Invoice Report Created");
		alert.setHeaderText("Invoice Report Created for Library");
		alert.setContentText("NOTE: Information Reflected is from Program.\nFor Changes to Remain, Press SAVE Before Quitting");
		alert.showAndWait();
		
		/**initial file set up */
		File report = new File(DEST);
		Document document = new Document();
		document.setPageSize(PageSize.LETTER.rotate());
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(DEST));
		document.open();
		
		/*create rectangle from header*/
        PdfContentByte canvas = writer.getDirectContent();
        Rectangle rect = new Rectangle(36, document.getPageSize().getHeight() - 36
        		, document.getPageSize().getWidth() - 36, document.getPageSize().getHeight() - 76);
        rect.setBorder(Rectangle.BOX);
        rect.setBorderWidth(2);
        rect.setBackgroundColor(BaseColor.LIGHT_GRAY);
        canvas.rectangle(rect);
        
        /*set text in header*/
        ColumnText ct = new ColumnText(canvas);
        Rectangle rect2 = new Rectangle(36, document.getPageSize().getHeight() - 29
        		, document.getPageSize().getWidth() - 36, 40);
        ct.setSimpleColumn(rect2);
        Font f = new Font(FontFamily.HELVETICA, 25.0f, Font.BOLD, BaseColor.BLACK);
        Chunk c = new Chunk(library.getLibraryName(), f);
        Paragraph p = new Paragraph(c);
        p.setAlignment(Element.ALIGN_CENTER);
        ct.addElement(p);
        ct.go();
        
        /* to start text under header */
        f = new Font(FontFamily.HELVETICA, 45.0f, Font.BOLD, BaseColor.BLACK);
        c = new Chunk(" ", f);
        document.add(new Paragraph(c));
        /*create a table of book info*/
	    /*  Library Name (in a report header)
	        Book Title (in a tabular/detail section of the report)
	        Book’s Author’s Full Name (in a tabular/detail section of the report)
	        Book Publisher (in a tabular/detail section of the report)
	        Quantity of Book in that Library (in a tabular/detail section of the report)
        */
        PdfPTable table = new PdfPTable(2);
        /*initial table header*/
        f = new Font(FontFamily.HELVETICA, 20.0f, Font.BOLD, BaseColor.BLACK);
        c = new Chunk("Book Name\n\n", f);
        table.addCell(new Paragraph(c));
        c = new Chunk("Book Information\n\n", f);
        table.addCell(new Paragraph(c));
        table.completeRow();
        
        for(LibraryBook lb: books){
        	String BookInfo = "Author: " + lb.getBook().getAuthor()+"\n" 
        			+ "Book Publisher: " + lb.getBook().getPublisher()+"\n"
        			+ "Quantity: " + lb.getQuantity() + "\n\n";
        	table.addCell(lb.getBook().getTitle());
        	table.addCell(BookInfo);
        	table.completeRow();
        }
        document.add(table);
        
        document.close();
        
	}
	public void deleteLibraryBook(LibraryBook selected) throws IOException{
		/*set quantity to 0*/
		int ndx = books.indexOf(selected);
		selected.setQuantity(0);
		books.set(ndx, selected);
		items = libBookList.getItems();
		items.set(ndx,selected);
		/**
		 * THIS WAS MEANT FOR REAL DELETION BUT 
		 * RAN OUT OF TIME SO JUST 
		 * SET QUANTITY TO 0 WAS USED INSTEAD 
		 */
		//LibraryBook selected = new LibraryBook();
		/*try{
			selected = libBookList.getSelectionModel().getSelectedItem();
			inform user they are about to delete a book forever
			Alert bookDelAlert = new Alert(AlertType.CONFIRMATION);
			bookDelAlert.setTitle("Delete Library Book Record?");
			bookDelAlert.setHeaderText(null);
			bookDelAlert.setContentText("Are you sure you want to delete this book?\n"
					+ "The Database will no longer have a record of this Book at this Library");
			Optional<ButtonType> result = bookDelAlert.showAndWait();
			
			if(result.get() == ButtonType.OK){
				int ndx = books.indexOf(selected);
				logger.error("Removed : "+ selected + " From List in Library!");
				items.remove(selected);
			}else{
				logger.error("Delete on " + selected + "Aborted!");
			}
			
		}catch(NullPointerException e){
			logger.error("****No Book Selected to Delete!****");
		}*/
	}
	
	
	public void addSubQuantity(int x) throws IOException{
		int index;
		LibraryBook selected = new LibraryBook();
		try{
			selected = libBookList.getSelectionModel().getSelectedItem();
			index = books.indexOf(selected);
			if(selected.getQuantity() + x <= 0){
				deleteLibraryBook(selected);
			}else{
				selected.setQuantity(selected.getQuantity() + x);
				books.set(index, selected);
				items = libBookList.getItems();
				items.set(index,selected);
			}

		}catch(NullPointerException e){
			logger.error("****No Book Selected to Add or Subtract From!****");
		}
	}
	public void addToBookList(LibraryBook newLibBook){
		if(books  != null){
			books.add(newLibBook);
			items = libBookList.getItems();
			items.add(newLibBook);	
			
		}else{ /*if library is new and no book list has been made*/
			books = new ArrayList<LibraryBook>();
			books.add(newLibBook);
			items = libBookList.getItems();
			items.add(newLibBook);
		}

	}
	
	/*Not very pretty Dialog Box For Adding New Books to List
	 * 
	 * NOTE: IF YOU CHOOSE A BOOK ALREADY IN THE LIST IT
	 *       WILL REPLACE THE QUANTITY OF BOOK IN LIST   */
	public LibraryBook showNewBookDialog() throws IOException{
		LibraryBook newBook = new LibraryBook();
		
		List<Book> bookChoices = MasterController.getInstance().getBookGateway().getBooks();
		ObservableList<Book> items =  FXCollections.observableArrayList();
		for(Book b: bookChoices){
			items.add(b);
		}
		
		Dialog dialog = new Dialog<>();
		dialog.setTitle("Add New Book");
		dialog.setHeaderText("Add a New Book to library\nNote: Choosing an existing book will upadate quantity");
		GridPane grid  = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20,150,10,10));
		
		TextField edQuant = new TextField();
		ComboBox<Book> edBookPicker = new ComboBox();
		edBookPicker.setItems(items);
		
		ButtonType bOK = new ButtonType("OK");
		ButtonType bCancel = new ButtonType("Cancel");
		dialog.getDialogPane().getButtonTypes().addAll(bOK,bCancel);

		grid.add(new Label("Book: "), 0,0);
		grid.add(edBookPicker, 1, 0);
		grid.add(new Label("Quantity: "), 0, 1);
		grid.add(edQuant, 1, 1);
		
		dialog.getDialogPane().setContent(grid);
		Optional<ButtonType> result = dialog.showAndWait();
		if(result.get() == bOK){
			if(edQuant.getText() == ""){
				logger.error("invalid book quantity");
			}
			try{
				logger.error("Book: "+ edBookPicker.getValue().getTitle());
				logger.error("Quantity: " + edQuant.getText());
				/**check if book is already in list if it is just update the quantity */
				Book choice = edBookPicker.getValue();
				if(books != null){
					for(LibraryBook lb : books){
						if(lb.getBook().getId() == choice.getId()){
							indexOfExisting = books.indexOf(lb);
							logger.error("IN LIST!");
							newBookExist = true;
						}
					}
				}
				if(Integer.parseInt(edQuant.getText()) <= 0){
					if(newBookExist == true){
						newBookValid = true;
					}else{
						newBookValid = false;
					}
					
					newBook = new LibraryBook(edBookPicker.getValue(), 0,true);
				}else{
					newBook = new LibraryBook(edBookPicker.getValue(), Integer.parseInt(edQuant.getText()),true);
					newBookValid = true;
				}
			}catch(NumberFormatException  | NullPointerException e){
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Input Not Valid");
				alert.setHeaderText(null);
				alert.setContentText("Inputs chosen are not valid please try again");
				alert.showAndWait(); 
				newBookValid = false;
			}
		}
		if(result.get() == bCancel){
			newBookValid = false;
		}
		return newBook;
	}
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		id = library.getId();
		Session session;
		try {
			session = MasterController.getInstance().getCurrentSession();
			setPermissions(session);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("ERROR: Session Not FOUND when Accessing Book List");
		}
		
		if(id != 0){
			libName = library.getLibraryName();
			books = library.getBooks();
			
			edLibTitle.setText(libName);
			/*populate list of books*/
			items = libBookList.getItems();
			for(LibraryBook b: books){
				items.add(b);
			}
			
		}else{
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Information");
			alert.setHeaderText(null);
			alert.setContentText("New LIBRARIES have no Audit Trail"
					+ "\nPress SAVE on a valid input first!");
			alert.showAndWait();
			
		}
		
	}
	private void setPermissions(Session session) {
		// TODO Auto-generated method stub
		UserType type = session.getUser().getUserType();
		
		if(type ==UserType.INTERN){
			edLibTitle.setDisable(true);
			bSaveLib.setDisable(true);
			//bAuditLib.setDisable(true);
			bInvReport.setDisable(true);
			bAddBook.setDisable(true);
			bDelLibBook.setDisable(true);
			bAddQuantity.setDisable(true);
			bSubQuantity.setDisable(true);
		}	
	}
	public Library getEdits() {
		// TODO Auto-generated method stub
		Library libEdits = new Library();
		try{
			libEdits = new Library(library.getId(), edLibTitle.getText(), books, library.getLastModified());
		}catch(StringIndexOutOfBoundsException  | NullPointerException e){
			logger.error("EDIT ON LIBRARY NOT FOUND");
		}
		return libEdits;
	}

}
