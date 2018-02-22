/**
 * CS4743 Assignment 5 by Lino Perez
 * UTSA ID : iij790
 * This starts the main process to run the app
 * */
package assign1;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Authentication.Session;
import Authentication.UserType;
import AuthenticationDB.AuthenticatorDB;
import AuthenticationDB.AuthenticatorRemote;
import GatewaySQL.*;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class AppMain extends Application{
	private static Logger logger = LogManager.getLogger();

	public static BorderPane root;
	
	public static AuthorTableGateway authorGateway;
	
	public AppMain(){
		
	}
	@Override
	public void init() throws Exception {
		super.init();
		
		//create gateway and exit if problem
		logger.error("AppMain init called");
		//authorGateway = new AuthorTableGateway();
		MasterController.getInstance();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		
		//close gateway
		logger.error("AppMain stop called");
		
		/* if a user is logged in presses the X or stops the app
		 * it will delete current session from DB*/
		Session session;
		session = MasterController.getInstance().getCurrentSession();
		if(session != null){
			AuthenticatorRemote ab = new AuthenticatorDB(); 
			ab.logout(session);
		}
		MasterController.getInstance().close();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub

		FXMLLoader loader = new FXMLLoader(getClass().getResource("main.fxml"));
		Parent view = loader.load();
		MasterController.getInstance().setRootPane((BorderPane)view);
		
		Scene scene = new Scene(view);

		primaryStage.setTitle("Final Project");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

}
