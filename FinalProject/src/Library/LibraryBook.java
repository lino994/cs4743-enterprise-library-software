package Library;

import Books.Book;

public class LibraryBook {
	private Book book;
	private int quantity;
	private boolean newRecord;
	
	public LibraryBook(){
		book = new Book();
		quantity = 0;
		newRecord = true;
	}
	
	public LibraryBook(Book book, int quantity, boolean newRecord){
		this.book = book;
		this.quantity = quantity;
		this.newRecord = newRecord;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public boolean isNewRecord() {
		return newRecord;
	}

	public void setNewRecord(boolean newRecord) {
		this.newRecord = newRecord;
	}
	
	public String toString(){
		return "Title: " + book.getTitle() + "" +"\nQuantity: " + quantity;
	}
}
