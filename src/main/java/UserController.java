/**
 * Created by Nicolas on 2017-08-09.
 */

public class UserController {

    // Authenticate the user by hashing the inputted password using the stored salt,
    // then comparing the generated hashed password to the stored hashed password
    public static boolean authenticate(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return false;
        }
        Account account = HibernateUtil.getAccountByEmail(username);
        return account != null && password.equals(account.getPassword());

    }
}
