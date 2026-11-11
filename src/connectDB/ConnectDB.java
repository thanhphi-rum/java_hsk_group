package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectDB {
	public static Connection con = null;
	private static ConnectDB instance = new ConnectDB();

	public static ConnectDB getInstance() {
		return instance;
	}

	public void connect() {
		String url = "jdbc:sqlserver://localhost:1433;databasename=QL_RapPhim";
		String user = "sa";
		String password = "sapassword"; // đổi thành mật khẩu SQL Server trên máy bạn
		try {
			if (con == null || con.isClosed()) {
				con = DriverManager.getConnection(url, user, password);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void discon() {
		if (con != null) {
			try {
				con.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static Connection getConnection() {
		return con;
	}
}