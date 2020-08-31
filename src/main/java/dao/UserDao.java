package dao;

import domain.User;

import javax.persistence.EntityManagerFactory;

public class UserDao extends AbstractHibernateDAO<User> implements IOperations<User>{
        public UserDao(EntityManagerFactory entityManagerFactory) {
            super(entityManagerFactory);
            setClazz(User.class);
        }


}
