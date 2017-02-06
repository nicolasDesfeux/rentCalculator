import com.mongodb.BasicDBObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Nicolas on 2017-01-04.
 */
public class Setting extends BasicDBObject {
    private static final long serialVersionUID = 1L;

    public Setting() {

    }

    public Setting(Date month, Double amount, String description, String user, String payingTo, String type) {
        super.put("amount", amount);
        super.put("description", description);
        super.put("user", user);
        super.put("payingTo", payingTo);
        super.put("type", type);

        Calendar cal = Calendar.getInstance();
        cal.setTime(month);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        super.put("month", cal.getTime());
    }

    public Date getMonth() {
        return (Date) super.get("month");
    }

    public void setMonth(Date month) {
        super.put("month", month);
    }

    public Double getAmount() {
        return (Double) super.get("amount");
    }

    public void setAmount(Double amount) {
        super.put("amount", amount);
    }

    public String getDescription() {
        return (String) super.get("description");
    }

    public void setDescription(String description) {
        super.put("description", description);
    }

    public String getUser() {
        return (String) super.get("user");
    }

    public void setUser(String user) {
        super.put("user", user);
    }

    public String getPayingTo() {
        return (String) super.get("payingTo");
    }

    public void setPayingTo(String payingTo) {
        super.put("payingTo", payingTo);
    }
}
