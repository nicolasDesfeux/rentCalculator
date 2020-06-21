package dao;

import domain.Entry;

import java.util.List;

public interface IEntryDao {
    Entry findOne(long id);

    List<Entry> findAll();

    Entry create(Entry entity);

    Entry update(Entry entity);

    void delete(Entry entity);

    void deleteById(long entityId);
}
