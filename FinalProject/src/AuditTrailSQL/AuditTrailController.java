package AuditTrailSQL;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import Authors.Author;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
/*controller for audit trail list*/
public class AuditTrailController implements Initializable {
	private Author author;
	private List<AuditTrailEntry> audits;
	private ObservableList<AuditTrailEntry> items;
	@FXML ListView auditListView;
	@FXML Label authorLabel;
	public AuditTrailController(List<AuditTrailEntry> audits){
		this.audits = audits;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		authorLabel.setText(audits.get(0).getRecordDescriptor());
		items = auditListView.getItems();
		for(AuditTrailEntry a : audits){
			items.add(a);
		}
	}

}
