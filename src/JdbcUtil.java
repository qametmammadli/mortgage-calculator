import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public final class JdbcUtil {
    private static String driver;
    private static String url;
    private static String username;
    private static String password;

    private static void loadConfiguration() throws IOException {
        Properties config = new Properties();
            config.load(new FileReader("config.properties"));
            driver = config.getProperty("jdbc.driver");
            url = config.getProperty("jdbc.url");
            username = config.getProperty("jdbc.username");
            password = config.getProperty("jdbc.password");
    }

    public static Connection getConnection() throws IOException, ClassNotFoundException, SQLException {
        loadConfiguration();
        Class.forName(driver);
        return DriverManager.getConnection(url, username, password);
    }

    public static void close(ResultSet rs, PreparedStatement ps, Connection connection) {
        try {
            if (rs != null) {
                rs.close();
            }

            if (ps != null) {
                ps.close();
            }

            if (connection != null) {
                connection.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
