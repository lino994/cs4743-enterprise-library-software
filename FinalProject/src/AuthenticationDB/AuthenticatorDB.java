package AuthenticationDB;

import java.io.IOException;
import java.sql.SQLException;

import javax.ejb.Stateless;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Authentication.Session;
import Authentication.User;
import Authentication.UserType;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

@Stateless
public class AuthenticatorDB implements AuthenticatorRemote{
	private static Logger logger = LogManager.getLogger();
	private Connection connect = null;
	
	
	public AuthenticatorDB(){
		try {
			connectToDB();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//public
	public void connectToDB() throws IOException, SQLException{
		MysqlDataSource db = new MysqlDataSource();
		db.setURL("jdbc:mysql://easel2.fulgentcorp.com:3306/iij790");
		db.setUser("iij790");
	    db.setPassword("QWCkuDf2gJpeuanCJ253");
	    
		connect = db.getConnection();
	}
	
	
	
	/**
	 * used for initial testing adding needed users 
	 * that where stated in the projects requirements
	 **/
	
	public void addUsersToDB(){
		PreparedStatement st = null;
		ResultSet rs = null;
		
		List<User> addUsers = new ArrayList<User>();
		User u = new User("Wilma Williams","wilma","arugula",UserType.ADMIN, false);
		addUsers.add(u);
		
		User u2 = new User("Leroy Jenkins","leroy","wipeout",UserType.LIBRARIAN, false);
		addUsers.add(u2);
		
		User u3 = new User("Sasquatch Jones","sasquatch","spinach",UserType.INTERN, false);
		addUsers.add(u3);
		
		User u4 = new User("Bob Tester","bob","1234",UserType.ADMIN, false);
		addUsers.add(u4);
		
		if(connect != null){
			try{
				String sql = "INSERT user_table SET name = ?, login = ?, pass_hash = ?, type = ?";
				st = connect.prepareStatement(sql);
				for(User user: addUsers){
					st.setString(1, user.getName());
					st.setString(2, user.getUserName());
					st.setString(3, user.getPassHash());
					if(user.getUserType() == UserType.ADMIN){
						st.setString(4, "ADMIN");
					}else if(user.getUserType() == UserType.LIBRARIAN){
						st.setString(4, "LIBRARIAN");
					}else if(user.getUserType() == UserType.INTERN){
						st.setString(4, "INTERN");
					}
					st.addBatch();
				}
				
				st.executeBatch();
				
			}catch (SQLException e) {
				// TODO Auto-generated catch block
			//logger.error("Author:" + newAuthor.toString() + "Failed to be Inserted");
			e.printStackTrace();
			} finally {
				try {
					if(rs != null)
						rs.close();
					if(st != null) 
						st.close();
				} catch (SQLException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
	
	
	@Override
	public Session loginCheck(String uName, String pHash) {
		// TODO Auto-generated method stub
		User currentUser;
		Session currentSession = null;
		
		String userSql;
		String sessionSql;
		String realName;
		String userNameDB;
		String pHashDB;
		String usertype;
		UserType currentType = UserType.NEW_USER;
		PreparedStatement sessionSt = null;
		PreparedStatement userSt = null;
		ResultSet rs = null;
		userSql = "SELECT * FROM user_table WHERE login = ? AND pass_hash = ?";
		sessionSql = "INSERT session_table SET login = ?, session_id = ?";
		
		try{
			connect.setAutoCommit(false);
			userSt = connect.prepareStatement(userSql);
			userSt.setString(1,uName);
			userSt.setString(2, pHash);
			
			rs = userSt.executeQuery();
			//logger.error("RS DONE");
			rs.first();
			userNameDB = rs.getString("login");
			pHashDB = rs.getString("pass_hash");
			realName = rs.getString("name");
			usertype = rs.getString("type");
			
			if(usertype.equals("ADMIN")){
				currentType = UserType.ADMIN;
			}else if (usertype.equals("INTERN")){
				currentType = UserType.INTERN;
			}else if (usertype.equals("LIBRARIAN")){
				currentType = UserType.LIBRARIAN;
			}
			//logger.error("user gotten: "+ realName);
			currentUser = new User(realName,userNameDB, pHashDB, currentType, true);
			currentSession = new Session(currentUser);
			
			sessionSt = connect.prepareStatement(sessionSql);
			sessionSt.setString(1, userNameDB);
			sessionSt.setInt(2, currentSession.getSessionId());
			sessionSt.executeUpdate();
			
			try{
				connect.commit();
			}catch(SQLException e){
				logger.error("Error Commiting Session and Retrieving user");
				connect.rollback();
			}
			connect.setAutoCommit(true);
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("No Results Found in DB");
			//logger.error("Author:" + newAuthor.toString() + "Failed to be Inserted");
		//e.printStackTrace();
		} finally {
			try {
				if(rs != null)
					rs.close();
				if(userSt != null) 
					userSt.close();
				if(sessionSt != null){
					sessionSt.close();
				}
			} catch (SQLException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return currentSession;
	}
	@Override
	public User getUserBySession(User user) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void logout(Session session) {
		// TODO Auto-generated method stub
		
		PreparedStatement st = null;
		ResultSet rs = null;
		String sqlLogout = "DELETE FROM session_table WHERE session_id = ?";
		try{
			st = connect.prepareStatement(sqlLogout);
			st.setInt(1, session.getSessionId());
			
			st.executeUpdate();
		
		
		}catch(SQLException e){
			logger.error("Error in Logout SQLException");
		}finally{
			
			try{
				if(rs != null){
				rs.close();
				}
				if(st != null){
					st.close();
				}
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		
		
		
	}
		


}


