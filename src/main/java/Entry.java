import java.util.Date;

/**
 * Created by Nicolas on 2017-01-02.
 */

public class Entry {

    public Entry() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    private String id;

    private String user;
    private String category;
    private String description;
    private Double amount;
    private Date date;

    public Entry(String user, String category, String description, double amount, Date date) {
        this.user = user;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
