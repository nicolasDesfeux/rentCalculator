/**
 * Created by Nicolas on 2017-01-02.
 */

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class Account extends AbstractEntity {

    @Id
    private String id;

    private String accountName;
    private String email;
    private String password;

    public Account() {
    }

    public Account(String accountName, String email, String password) {
        this.id = UUID.randomUUID().toString();
        this.accountName = accountName;
        this.email = email;
        this.password = password;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getKey() {
        return id;
    }
}
