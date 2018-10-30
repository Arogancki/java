package mvc.model;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;
import java.util.TreeMap;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;

import mvc.model.Worker.Dyrektor;
import mvc.model.Worker.Handlowiec;
import mvc.model.Worker.Worker;

public class DAO {
	private final static String DBURL = "jdbc:mysql://127.0.0.1:3306";
	private final static String DBUSER = "user1";
	private final static String DBPASS = "password";
	private final static String DBDRIVER = "com.mysql.jdbc.Driver";
	private java.sql.PreparedStatement getAllWorkers;
	private java.sql.PreparedStatement getWorker;
	private java.sql.PreparedStatement inserWorker;
	java.sql.Connection connection;
	public DAO(){
		try {
			Class.forName(DBDRIVER).newInstance();
			connection = DriverManager.getConnection(DBURL, DBUSER, DBPASS);
			inserWorker = connection.prepareStatement(
					"REPLACE INTO `java`.`workers`(`pesel`,`name`,`lastName`,`income`,`phone`,`limit`,`provision`,`addictonal`,`card`) VALUES (?,?,?,?,?,?,?,?,?)"
			);
			getWorker = connection.prepareStatement(
					"SELECT * FROM `java`.`workers` WHERE `workers`.`pesel` = ?"
			);
			getAllWorkers = connection.prepareStatement(
					"SELECT * FROM `java`.`workers`"
			);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			System.out.println("Nie uda³o siê po³¹czyæ z baz¹ danych");
		}
	}
	private void setInserWorker(Worker worker) throws SQLException {
		inserWorker.setString(1, worker.getPesel());
		inserWorker.setString(2, worker.getName());
		inserWorker.setString(3, worker.getLastName());
		inserWorker.setBigDecimal(4, worker.getIncome());
		inserWorker.setInt(5, worker.getPhone());
		inserWorker.setBigDecimal(6, worker.getLimit());
		if (worker instanceof Dyrektor) {
			inserWorker.setNull(7, Types.INTEGER);
			inserWorker.setBigDecimal(8, ((Dyrektor)worker).getAddictonal());
			inserWorker.setString(9, ((Dyrektor)worker).getCard());
		}
		else if (worker instanceof Handlowiec) {
			inserWorker.setInt(7, ((Handlowiec)worker).getProvision());
			inserWorker.setNull(8, Types.DECIMAL);
			inserWorker.setNull(9, Types.VARCHAR);
		}
	}
	public void saveWorkers(Worker worker) throws SQLException {
		setInserWorker(worker);
		inserWorker.executeQuery();
	}
	public void saveWorkers(TreeMap<String, Worker> persons) throws SQLException {
		try {
			connection.setAutoCommit(false);
			for(Map.Entry<String, Worker> entry : persons.entrySet()) {
				setInserWorker(entry.getValue());
			    inserWorker.executeUpdate();
			}
			connection.commit();
			 try {
				 connection.setAutoCommit(true);
			} catch (SQLException e) {}
		} catch (SQLException e ) {
	        if (connection != null) {
	            try {
	            	connection.rollback();
	            	connection.setAutoCommit(true);
	            } catch(SQLException excep) {}
	            throw e;
	        }
	    }
	}
	public Worker getWorkers(String pesel) throws SQLException {
		getWorker.setString(1, pesel);
		java.sql.ResultSet rs = getWorker.executeQuery();
		if (rs.next()) {
			return Worker.create(
					pesel, rs.getString("name"), rs.getString("lastName"), 
					rs.getBigDecimal("income"), rs.getBigDecimal("limit"), rs.getInt("phone"), 
					rs.getObject("provision")==null ? null : rs.getInt("provision"),
					rs.getObject("card")==null ? null : rs.getString("card"), 
					rs.getObject("addictonal")==null ? null : rs.getBigDecimal("addictonal")
			);
		}
		return null;
	}
	public TreeMap<String, Worker> getWorkers() throws SQLException{
		java.sql.ResultSet rs = getAllWorkers.executeQuery();
		TreeMap<String, Worker> workers = new TreeMap<String, Worker>();
		while (rs.next()) {
			String pesel = rs.getString("pesel");
			workers.put(pesel, Worker.create(
					pesel, rs.getString("name"), rs.getString("lastName"), 
					rs.getBigDecimal("income"), rs.getBigDecimal("limit"), rs.getInt("phone"), 
					rs.getObject("provision")==null ? null : rs.getInt("provision"),
					rs.getObject("card")==null ? null : rs.getString("card"), 
					rs.getObject("addictonal")==null ? null : rs.getBigDecimal("addictonal"))
					
			);
		}
		return workers;
	}
	protected void finalize() {
		try {
			connection.close();
		} catch (SQLException e) {
			
		}
	 }
}
