package dao;

import domain.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;
import java.util.*;

public class EntryDao extends AbstractHibernateDAO<Entry> implements IOperations<Entry>{
        public EntryDao(EntityManagerFactory entityManagerFactory) {
            super(entityManagerFactory);
            setClazz(Entry.class);
        }

    public List<Entry> getAllEntries(Date currentMonth) {
        return getAllEntries(currentMonth,1);
    }

    public List<Entry> getAllEntries(Date currentMonth, int nbMonth) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CriteriaBuilder criteriaBuilderObj = entityManager.getCriteriaBuilder();

        // Making The Query Object From The 'CriteriaBuilder' Instance
        CriteriaQuery<Entry> queryObj = criteriaBuilderObj.createQuery(Entry.class);
        Root<Entry> from = queryObj.from(Entry.class);
        ParameterExpression<Date> start = criteriaBuilderObj.parameter(Date.class);
        ParameterExpression<Date> end = criteriaBuilderObj.parameter(Date.class);
        queryObj.select(from).where(criteriaBuilderObj.between(from.get("date"), start, end));
        TypedQuery<Entry> typedQuery = entityManager.createQuery(queryObj);
        Calendar c = new GregorianCalendar();
        c.setTime(currentMonth);
        c.add(Calendar.MONTH,nbMonth);
        c.add(Calendar.DAY_OF_MONTH,-1);
        if(c.getTime().before(currentMonth)){
            typedQuery.setParameter(end,currentMonth);
            typedQuery.setParameter(start, c.getTime());
        }else{
            typedQuery.setParameter(start,currentMonth);
            typedQuery.setParameter(end, c.getTime());
        }

        System.out.println(c.getTime());
        List<Entry> resultList = typedQuery.getResultList();
        resultList.sort(Comparator.comparing(Entry::getDate));
        Collections.reverse(resultList);
        return resultList;
    }
}
