package ca.jrvs.apps.trading.dao;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import ca.jrvs.apps.trading.model.domain.EntityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

public abstract class JdbcCrudDao<T extends EntityModel<Integer>> implements CrudRepository<T, Integer> {

    private static final Logger logger = LoggerFactory.getLogger(JdbcCrudDao.class);

    abstract public JdbcTemplate getJdbcTemplate();

    abstract public SimpleJdbcInsert getSimpleJdbcInsert();

    abstract public String getTableName();

    abstract public String getIdColumnName();

    abstract Class<T> getEntityClass();

    /**
     * Save an entity and update its auto-generated integer ID
     *
     * @param entity - Entity to be saved
     * @return the saved entity
     */
    @Override
    public <S extends T> S save(S entity) {
        if (existsById(entity.getId())) {
            if (updateOne(entity) != 1) {
                throw new DataRetrievalFailureException("ERROR: Unable to update");
            }
        } else {
            addOne(entity);
        }
        return entity;
    }

    /**
     * Helper function which saves one quote
     */
    private <S extends T> void addOne(S entity) {
        SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(entity);
        Number newId = getSimpleJdbcInsert().executeAndReturnKey(parameterSource);
        entity.setId(newId.intValue());
    }

    /**
     * Helper function that updates one quote
     */
    abstract public int updateOne(T entity);

    @Override
    public Optional<T> findById(Integer id) {
        Optional<T> entity = Optional.empty();
        String selectSql = "SELECT * FROM " + getTableName() + " WHERE " + getIdColumnName() + "=?";
        try {
            entity = Optional.ofNullable(getJdbcTemplate().queryForObject(selectSql,
                    BeanPropertyRowMapper.newInstance(getEntityClass()), id));
        } catch (IncorrectResultSizeDataAccessException ex) {
            logger.error("Cannot find ID: " + id, ex);
        }
        return entity;
    }

    @Override
    public boolean existsById(Integer id) {
        try {
            findById(id).get();
            return true;
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    @Override
    public List<T> findAll() {
        String selectSql = "SELECT * FROM " + getTableName();
        List<T> quotes = getJdbcTemplate()
                .query(selectSql, BeanPropertyRowMapper.newInstance(getEntityClass()));
        return quotes;
    }

    @Override
    public List<T> findAllById(Iterable<Integer> ids) {
        final String[] idsString = {"("};
        ids.forEach(id -> {
            if (idsString[0].equals("("))
                idsString[0] = idsString[0] + id;
            else
                idsString[0] = idsString[0] + ", " + id;
        });
        idsString[0] = idsString[0] + ")";
        String selectSql = "SELECT * FROM " + getTableName() + " WHERE " + getIdColumnName() + " IN " + idsString[0];
        List<T> entities = getJdbcTemplate()
                .query(selectSql, BeanPropertyRowMapper.newInstance(getEntityClass()));
        return entities;
    }

    @Override
    public void deleteById(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("ID can't be null");
        }
        String deleteSql = "DELETE FROM " + getTableName() + " WHERE " + getIdColumnName() + " =?";
        getJdbcTemplate().update(deleteSql, id);
    }

    @Override
    public long count() {
        String countSql = "SELECT COUNT(*) FROM " + getTableName();
        return getJdbcTemplate().queryForObject(countSql, Integer.class);
    }

    @Override
    public void deleteAll() {
        String deleteSql = "DELETE FROM " + getTableName();
        getJdbcTemplate().update(deleteSql);
    }
}
