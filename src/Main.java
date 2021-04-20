import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        String name = null;
        String surname = null;
        BigDecimal homePrice = BigDecimal.ZERO;
        LocalDate birthDay = null;
        int years;

        Connection conn = null;


        System.out.print("Name:");
        if(scanner.hasNextLine()){
            name = scanner.nextLine();
        }
        System.out.print("Surname:");
        if(scanner.hasNextLine()){
            surname = scanner.nextLine();
        }
        System.out.print("Birth date (DD.MM.YYYY):");
        if(scanner.hasNextLine()){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            try {
                birthDay = LocalDate.parse(scanner.nextLine(), formatter);
            } catch (Exception e){
                System.out.println("Wrong date format. Try again");
                return;
            }
        }

        System.out.print("Home Price:");
        if(scanner.hasNextBigDecimal()){
            homePrice = scanner.nextBigDecimal();
        }

        System.out.print("Year (max=25):");
        if(scanner.hasNextInt()){
            years = scanner.nextInt();
            if(years>25){
                System.out.println("Max year limit:25. Try again");
                return;
            }
        }else{
            System.out.println("Wrong year number");
            return;
        }

        scanner.close();

        try {
            conn = JdbcUtil.getConnection();
            conn.setAutoCommit(false);

            Customer customer = new Customer();
            customer.setId(1);
            customer.setName(name);
            customer.setSurname(surname);
            customer.setBirth_date(birthDay);

            customer.addCustomer(conn);

            Credit credit  = new Credit();
            boolean result = credit.apply(conn, customer, years, homePrice);
            if(result) {
                System.out.println(
                        " Annual:  Initial Payment:" + credit.getInitialPayment()
                                + "   Credit Amount:" + credit.getCreditAmount()
                                + "   Interest Amount:" + credit.getInterestAmount()
                );

                credit.getMonthlyPaymentList().forEach(monthlyPayment -> System.out.println(
                        monthlyPayment.getId()
                                + "  Month:" + monthlyPayment.getPaymentDate()
                                + "  Base Amount:" + monthlyPayment.getBaseAmount()
                                + "  Interest Amount:" + monthlyPayment.getInterestAmount()
                                + "  Total Amount:" + monthlyPayment.getTotalAmount()
                        )
                );
            }

        } catch (RetirementAgeException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println("There is something wrong in configuration file");
        } catch (SQLException e) {
            System.out.println("There is something wrong in connection to DB");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }finally {
            JdbcUtil.close(null, null, conn);
        }
    }
}
