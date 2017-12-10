import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Nicolas on 2017-02-19.
 */
@Entity
public class User extends AbstractEntity {
    private String lastName;
    private String firstName;
    @Id
    private String userId;

    public User() {
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public User(String lastName, String firstName, String userId) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (lastName != null ? !lastName.equals(user.lastName) : user.lastName != null) return false;
        if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null) return false;
        return userId != null ? userId.equals(user.userId) : user.userId == null;
    }

    @Override
    public int hashCode() {
        int result = lastName != null ? lastName.hashCode() : 0;
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        return result;
    }

    @Override
    public String getKey() {
        return userId;
    }

    public String getFulName() {
        return (getFirstName() != null ? getFirstName() : "") + (getLastName() != null ? " " + getLastName() : "");
    }
}
