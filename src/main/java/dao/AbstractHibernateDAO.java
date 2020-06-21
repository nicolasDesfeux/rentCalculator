package dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import domain.DomainObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.util.List;

public abstract class AbstractHibernateDAO<T extends DomainObject> extends AbstractDao<T> implements IOperations<T> {

    protected EntityManagerFactory entityManagerFactory;

    public AbstractHibernateDAO(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

// API

    @Override
    public T findOne(final long id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return entityManager.find(clazz,id);
    }

    @Override
    public List<T> findAll() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TypedQuery<T> query = entityManager.createQuery("SELECT e FROM "+ clazz.getName() + " e", clazz);
        return query.getResultList();
    }

    @Override
    public T create(final T entity) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Preconditions.checkNotNull(entity);
        entityManager.getTransaction().begin();
        entityManager.persist(entity);
        entityManager.getTransaction().commit();
        entityManager.close();
        return entity;
    }

    @Override
    public T update(final T entity) {
        Preconditions.checkNotNull(entity);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        return entityManager.merge(entity);
    }

    @Override
    public void delete(final T entity) {
        Preconditions.checkNotNull(entity);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction( ).begin( );
        entityManager.remove(findOne(entity.getId()));
        entityManager.getTransaction( ).commit( );
        entityManager.close( );
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

    @Override
    public void deleteById(final long entityId) {
        final T entity = findOne(entityId);
        Preconditions.checkState(entity != null);
        delete(entity);
    }

    public String toJson(List<T> objectList){
        StringBuilder result= new StringBuilder("[");
        for (T domainObject : objectList) {
            ObjectMapper Obj = new ObjectMapper();

            try {
                String jsonStr = Obj.writeValueAsString(domainObject);
                // Displaying JSON String
                result.append(",").append(jsonStr);
            }

            catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println((result + "]").replaceFirst(",", ""));
        return (result + "]").replaceFirst(",", "");
    }


}
