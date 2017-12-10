import org.apache.commons.lang.time.DateUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class HibernateUtil {

    private static EntityManagerFactory entityManagerFactory;

    private static void buildEntityManagerFactory() {
        entityManagerFactory = Persistence.createEntityManagerFactory("mongoDbConnexion");
    }

    private static EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            buildEntityManagerFactory();
        }
        return entityManagerFactory.createEntityManager();
    }

    public static void persist(AbstractEntity o) {
        //Test
        EntityManager entityManager = getEntityManager();

        if (entityManager.find(o.getClass(), o.getKey()) == null) {
            entityManager.getTransaction().begin();
            entityManager.persist(o);
            entityManager.getTransaction().commit();
        } else {
            entityManager.getTransaction().begin();
            entityManager.merge(o);
            entityManager.getTransaction().commit();
        }
        // get a new EM to make sure data is actually retrieved from the store and not Hibernate's internal cache
        entityManager.close();

        //End
    }

    public static void createOneAccount() {
        //Test
        EntityManager entityManager = getEntityManager();

        Account o = new Account("test", "nicolas.desfeux@gmail.com", "test");
        entityManager.getTransaction().begin();
        entityManager.persist(o);
        entityManager.getTransaction().commit();
        // get a new EM to make sure data is actually retrieved from the store and not Hibernate's internal cache
        entityManager.close();

        //End
    }

    public static void removeById(String id) {
        //Test
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createNativeQuery("db.Entry.find({\"_id\" : \"" + id + "\"})", Entry.class);
        Entry e = (Entry) query.getResultList().get(0);
        entityManager.getTransaction().begin();
        entityManager.remove(e);
        entityManager.getTransaction().commit();
        // get a new EM to make sure data is actually retrieved from the store and not Hibernate's internal cache
        entityManager.close();

        //End
    }

    public static User findUserById(String o) {
        //Test

        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();
        // create an Entry
        // persist organizer (will be cascaded to hikes)
        User user = entityManager.find(User.class, o);
        entityManager.getTransaction().commit();

        // get a new EM to make sure data is actually retrieved from the store and not Hibernate's internal cache
        entityManager.close();

        //End
        return user;
    }

    public static Collection<User> getAllUsers() {
        //Test
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createNativeQuery("db.User.find({})", User.class);
        return query.getResultList();
    }

    public static Entry getEntryById(String id) {
        //Test
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createNativeQuery("db.Entry.find({\"_id\" : \"" + id + "\"})", Entry.class);
        return (Entry) query.getResultList().get(0);
    }

    public static Collection<Entry> getEntriesForMonth(Date date, EntryType entryType) {
        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS\'Z\'");
        Query query = entityManager.createNativeQuery("db.Entry.find({\"entryType\" : {\"$in\" : [new NumberLong(\"" + entryType.ordinal() + "\")]},\"date\" : { \"$gte\" : new ISODate(\"" + simpleDateFormat.format(date) + "\"), \"$lt\" : new ISODate(\"" + simpleDateFormat.format(DateUtils.addMonths(date, 1)) + "\") }})", Entry.class);
        List<Entry> resultList = query.getResultList();
        resultList.sort(Comparator.comparing(Entry::getDate));
        entityManager.getTransaction().commit();
        entityManager.close();
        return resultList;
    }

    public static Entry getOldestEntry(EntryType entryType) {
        EntityManager entityManager = getEntityManager();
        entityManager.getTransaction().begin();
        Query query = entityManager.createNativeQuery("db.Entry.find({\"entryType\" : {\"$in\" : [new NumberLong(\"" + entryType.ordinal() + "\")]}})", Entry.class);
        List<Entry> resultList = query.getResultList();
        resultList.sort(Comparator.comparing(Entry::getDate));
        entityManager.getTransaction().commit();
        entityManager.close();
        return resultList.get(0);
    }

    public static void persist(List<Entry> entries) {
        for (Entry entry : entries) {
            persist(entry);
        }
    }

    public static Account getAccountByEmail(String username) {
        //Test
        EntityManager entityManager = getEntityManager();
        Query query = entityManager.createNativeQuery("db.Account.find({\"email\" : \"" + username + "\"})", Account.class);
        List resultList = query.getResultList();
        entityManager.close();
        return resultList.isEmpty() ? null : (Account) resultList.get(0);
    }
}