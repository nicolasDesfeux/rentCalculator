package domain;
/**
 * Created by Nicolas on 2017-01-02.
 */
import javax.persistence.*;
import java.util.Date;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "ENTRY")
public class Entry extends DomainObject{

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    protected User user;

    protected String category;
    protected String description;
    protected String payingTo;
    protected Double amount;
    @Temporal(TemporalType.DATE)
    protected Date date;
    protected EntryType entryType;

    public Entry(){
    }

    public Entry(User user, String category, String description, double amount, Date date, EntryType entryType, String payingTo) {
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

    public double getAmount() {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
    public String toString() {
        return "Entry{" +
                "user=" + user.getFullName() +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", payingTo='" + payingTo + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", entryType=" + entryType +
                '}';
    }
}
