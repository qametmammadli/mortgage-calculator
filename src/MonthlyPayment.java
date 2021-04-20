import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;


public class MonthlyPayment {
    private long id;
    private long creditId;
    private LocalDate paymentDate;
    private BigDecimal baseAmount;
    private BigDecimal interestAmount;
    private BigDecimal totalAmount;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreditId() {
        return creditId;
    }

    public void setCreditId(long creditId) {
        this.creditId = creditId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public boolean addMonthlyPayment(Connection connection){
        PreparedStatement ps = null;

        String sql = "insert into monthly_payment(id, credit_id, payment_date,\n" +
            " base_amount, interest_amount, total_amount)\n" +
            " values(monthly_payment_seq.nextval, ?, ?, ?, ?, ?)";

        try {
            ps = connection.prepareStatement(sql);
            ps.setLong(1, getCreditId());
            ps.setDate(2, Date.valueOf(getPaymentDate()));
            ps.setBigDecimal(3, getBaseAmount());
            ps.setBigDecimal(4, getInterestAmount());
            ps.setBigDecimal(5, getTotalAmount());
            ps.executeUpdate();

            connection.commit();

            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            System.out.println("Something went wrong. Couldn't add monthly payment to DB");
            e.printStackTrace();
        } finally {
            JdbcUtil.close(null, ps, null);
        }

        return false;
    }
}
