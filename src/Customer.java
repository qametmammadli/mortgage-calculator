import java.sql.*;
import java.time.LocalDate;

public class Customer {
    private long id;
    private String name;
    private String surname;
    private LocalDate birth_date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public LocalDate getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(LocalDate birth_date) {
        this.birth_date = birth_date;
    }

    public void addCustomer(Connection connection){
        ResultSet rs = null;
        PreparedStatement ps = null;
        String sql = "insert into customer(id, name, surname, birth_date)\n" +
                " values(customer_seq.nextval, ?, ?, ?)";
        try {
            String[] generatedColumns = { "id" };
            ps = connection.prepareStatement(sql, generatedColumns);
            ps.setString(1, getName());
            ps.setString(2, getSurname());
            ps.setDate(3, Date.valueOf(getBirth_date()));

            int rows = ps.executeUpdate();
            if(rows == 1){
                rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    setId(rs.getLong(1));
                }
            }

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            System.out.println("Something went wrong. Couldn't add customer to DB");
            e.printStackTrace();
        }
        finally {
            JdbcUtil.close(rs, ps, null);
        }
    }
}

