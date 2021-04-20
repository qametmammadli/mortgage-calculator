import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class Credit {
    private long id;
    private long customerId;
    private BigDecimal homePrice;
    private BigDecimal initialPayment;
    private BigDecimal creditAmount;
    private BigDecimal interestAmount;
    private LocalDate firstPaymentDate;
    private LocalDate lastPaymentDate;
    private LocalDate actionDate;
    private final List<MonthlyPayment> monthlyPaymentList = new ArrayList<>();

    public List<MonthlyPayment> getMonthlyPaymentList() {
        return monthlyPaymentList;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getHomePrice() {
        return homePrice;
    }

    public void setHomePrice(BigDecimal homePrice) {
        this.homePrice = homePrice;
    }

    public BigDecimal getInitialPayment() {
        return initialPayment;
    }

    public void setInitialPayment(BigDecimal initialPayment) {
        this.initialPayment = initialPayment;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    public LocalDate getFirstPaymentDate() {
        return firstPaymentDate;
    }

    public void setFirstPaymentDate(LocalDate firstPaymentDate) {
        this.firstPaymentDate = firstPaymentDate;
    }

    public LocalDate getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(LocalDate lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }

    public LocalDate getActionDate() {
        return actionDate;
    }

    public void setActionDate(LocalDate actionDate) {
        this.actionDate = actionDate;
    }

    public boolean apply(Connection connection, Customer customer, int years, BigDecimal homePrice) throws RetirementAgeException {
        boolean result = true;
        LocalDate actionDate = LocalDate.now();
        Period dateDiff = Period.between(customer.getBirth_date(), actionDate);
        int months = years * 12;
        int age = dateDiff.getYears();

        if(age>=65){
            throw new RetirementAgeException("Your age have reached retirement age : " + age);
        }

        LocalDate firstPaymentDate = actionDate.plusMonths(1);
        LocalDate lastPaymentDate  = firstPaymentDate.plusMonths(months - 1);
        age = Period.between(customer.getBirth_date(), lastPaymentDate).getYears();
        if(lastPaymentDate.compareTo(customer.getBirth_date().plusYears(65)) > 0){
            throw new RetirementAgeException("Your last payment date will reach retirement age : " + age
                    + "\nMaximum year can be:" + (years-(age-65)));
        }

        BigDecimal initialPayment = homePrice.multiply(BigDecimal.valueOf(0.3)).setScale(1, BigDecimal.ROUND_HALF_UP); // 400.000*0.3 = 120.000
        BigDecimal creditAmount   = homePrice.subtract(initialPayment);

        if(creditAmount.compareTo(BigDecimal.valueOf(150000))>0){
            initialPayment = initialPayment.add(creditAmount.subtract(BigDecimal.valueOf(150000)));
            creditAmount   = BigDecimal.valueOf(150000);
        }

        // interest amount
        double i = 0.08/12;
        double monthlyInterestRate = Math.pow(1 + i, months);
        double interestRate = monthlyInterestRate * i / (monthlyInterestRate - 1);

        BigDecimal monthlyInterestAmount = creditAmount.multiply(BigDecimal.valueOf(interestRate)).setScale(1, BigDecimal.ROUND_HALF_UP);
        BigDecimal annualInterestAmount = monthlyInterestAmount.multiply(BigDecimal.valueOf(months)).setScale(1, BigDecimal.ROUND_HALF_UP);

        setId(1);
        setCustomerId(customer.getId());
        setHomePrice(homePrice);
        setInitialPayment(initialPayment);
        setCreditAmount(creditAmount);
        setInterestAmount(annualInterestAmount);
        setFirstPaymentDate(firstPaymentDate);
        setLastPaymentDate(lastPaymentDate);
        setActionDate(actionDate);

        addCredit(connection);

        int seq = 1;
        BigDecimal baseAmount  = getCreditAmount().divide(BigDecimal.valueOf(months), 1, RoundingMode.HALF_UP);
        BigDecimal interestAmount = monthlyInterestAmount.subtract(baseAmount);
        LocalDate lastDate  = lastPaymentDate.plusMonths(1);
        for (LocalDate date = firstPaymentDate; date.isBefore(lastDate); date = date.plusMonths(1))
        {
            MonthlyPayment monthlyPayment = new MonthlyPayment();
            monthlyPayment.setId(seq++);
            monthlyPayment.setCreditId(getId());
            monthlyPayment.setBaseAmount(baseAmount);
            monthlyPayment.setInterestAmount(interestAmount);
            monthlyPayment.setTotalAmount(monthlyInterestAmount);
            monthlyPayment.setPaymentDate(date);
            if(!monthlyPayment.addMonthlyPayment(connection)) {
                result = false;
                break;
            }
            monthlyPaymentList.add(monthlyPayment);
        }
        return result;
    }

    public void addCredit(Connection connection){
        ResultSet rs = null;
        PreparedStatement ps = null;
        String sql = "insert into credit(id, customer_id, home_price, initial_payment, credit_amount,\n" +
                " interest_amount, first_payment_date, last_payment_date, action_date)\n" +
                " values(credit_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            String[] generatedColumns = { "id" };
            ps = connection.prepareStatement(sql, generatedColumns);
            ps.setLong(1, getCustomerId());
            ps.setBigDecimal(2, getHomePrice());
            ps.setBigDecimal(3, getInitialPayment());
            ps.setBigDecimal(4, getCreditAmount());
            ps.setBigDecimal(5, getInterestAmount());
            ps.setDate(6, Date.valueOf(getFirstPaymentDate()));
            ps.setDate(7, Date.valueOf(getLastPaymentDate()));
            ps.setDate(8, Date.valueOf(getActionDate()));

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
            System.out.println("Something went wrong. Couldn't add credit to DB");
            e.printStackTrace();
        }
        finally {
            JdbcUtil.close(rs, ps, null);
        }
    }
}
