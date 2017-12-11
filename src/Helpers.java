import java.util.*;

public class Helpers {
    public static String RetryScanner(String message, String pattern)
    {
        String returnString = "";
        boolean success = false;
        while (!success)
            try {
                System.out.print(message);
                Scanner scan = new Scanner(System.in);
                returnString = scan.next(pattern);
                success = true;
                scan.close();
            }
            catch (InputMismatchException ex)
            {
                System.out.println("Invalid input, try again!");
            }
        return returnString;
    }
}
