package dao;

import domain.StagingEntry;

import javax.persistence.EntityManagerFactory;

public class StagingEntryDao extends AbstractHibernateDAO<StagingEntry> implements IOperations<StagingEntry>{
        public StagingEntryDao(EntityManagerFactory entityManagerFactory) {
            super(entityManagerFactory);
            setClazz(StagingEntry.class);
        }
}
