package repository;

import domain.User;
import org.sqlite.SQLiteDataSource;
import java.sql.*;
import java.util.List;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SQLUserRepository extends Repository<User> {
    private Connection connection = null;
    private final String databaseLocation;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SQLUserRepository(String databaseLocation) {
        this.databaseLocation = "jdbc:sqlite:" + databaseLocation;
        openConnection();
        createSchema();
        loadData();
       // initializeWithSampleData(List.of(
           //     new User(1, "admin", "adminpass", true),
           //     new User(2, "user", "userpass", false)
      //  ));
    }

    private void openConnection() {
        try {
            SQLiteDataSource dataSource = new SQLiteDataSource();
            dataSource.setUrl(databaseLocation);
            if (connection == null || connection.isClosed()) {
                connection = dataSource.getConnection();
                System.out.println("Connection established successfully.");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
                System.out.println("Connection closed successfully.");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    void createSchema() {
        try (final Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users(id int, username varchar(255), password varchar(255), admin boolean);");
        } catch (SQLException exception) {
            System.err.println(exception.getMessage());
        }
    }

    private void loadData() {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM users"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                User user = new User(resultSet.getInt("id"), resultSet.getString("username"), resultSet.getString("password"), resultSet.getBoolean("admin"));
                this.elements.add(user);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void initializeWithSampleData(List<User> initialUsers) {
        for (User user : initialUsers) {
            try {
                addElement(user);
            } catch (DuplicateObjectException e) {
                System.err.println("Error adding user: " + e.getMessage());
            }
        }
    }

    @Override
    public void addElement(User user) throws DuplicateObjectException {
        super.addElement(user);
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO users VALUES (?, ?, ?, ?)")) {
            statement.setInt(1, user.getId());
            statement.setString(2, user.getUsername());
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            statement.setString(3, encodedPassword);
            statement.setBoolean(4, user.getAdmin());
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void removeElement(int id) throws RepositoryException {
        super.removeElement(id);
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id=?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void updateElement(int id, User user) throws RepositoryException {
        super.updateElement(id, user);
        removeElement(id);
        addElement(user);
    }


}
