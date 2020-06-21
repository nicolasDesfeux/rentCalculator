package dao;

import domain.User;

import java.util.List;

public interface IUserDao {
    User findOne(long id);

    List<User> findAll();

    User create(User entity);

    User update(User entity);

    void delete(User entity);

    void deleteById(long entityId);
}
