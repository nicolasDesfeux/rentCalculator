package dao;

import domain.User;

import javax.persistence.EntityManagerFactory;

public class UserDao extends AbstractHibernateDAO<User> implements IUserDao{
        public UserDao(EntityManagerFactory entityManagerFactory) {
            super(entityManagerFactory);
            setClazz(User.class);
        }


}
