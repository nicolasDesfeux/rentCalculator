/**
 * Created by Nicolas on 2017-01-02.
 */

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
public class Entry extends AbstractEntity {

    @Id
    private String id;

    @OneToOne
    private User user;

    private String category;
    private String description;
    private String payingTo;
    private Double amount;
    private Date date;
    private EntryType entryType;

    public Entry() {
    }

    public Entry(User user, String category, String description, double amount, Date date, EntryType entryType, String payingTo) {
        this.id = UUID.randomUUID().toString();
        this.user = user;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.entryType = entryType;
        this.payingTo = payingTo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
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

    public BigDecimal getAmount() {
        return BigDecimal.valueOf(amount).setScale(2,BigDecimal.ROUND_HALF_UP);
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

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public EntryType getEntryType() {
        return entryType;
    }

    public void setEntryType(EntryType entryType) {
        this.entryType = entryType;
    }

    public String getPayingTo() {
        return payingTo;
    }

    public void setPayingTo(String payingTo) {
        this.payingTo = payingTo;
    }

    @Override
    public String getKey() {
        return id;
    }
}
