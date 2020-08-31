package domain;

import org.hibernate.annotations.Polymorphism;
import org.hibernate.annotations.PolymorphismType;

import javax.persistence.Entity;
import java.util.Date;

@Entity
@Polymorphism(type = PolymorphismType.EXPLICIT)
public class StagingEntry extends Entry {
    public StagingEntry(User o, String category, String description, double amount, Date date, EntryType type) {
        super(o, category, description, amount, date, type, null);
    }

    public StagingEntry() {

    }

    @Override
    public String toString() {
        return "StagingEntry{" +
                "user=" + (user == null ? "" : user.getFullName()) +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", payingTo='" + payingTo + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", entryType=" + entryType +
                '}';
    }
}
