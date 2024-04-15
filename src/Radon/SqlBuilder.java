package Radon;

import arc.util.Log;
import arc.util.Nullable;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class SqlBuilder {
    public final String queryString;
    public final HashMap<String, Object> variables = new HashMap<>();
    public final HashMap<String, Object[]> variableLists = new HashMap<>();

    public SqlBuilder(String queryString) {
        this.queryString = queryString;
    }

    /**
     * Sets a variable in the HQL Query
     *
     * @param varName The name of the variable in the HQL Query.
     * @param varObject The value of the variable
     * @return itself
     */
    public SqlBuilder set(String varName, Object varObject) {
        variables.put(varName, varObject);
        return this;
    }

    /**
     * Sets a variable in the HQL Query.
     *
     * @param varName The name of the list variable in the HQL Query.
     * @param list The list value of the variable.
     * @return itself
     */
    public SqlBuilder setList(String varName, Collection<?> list) {
        variableLists.put(varName, list.toArray());
        return this;
    }

    /**
     * Runs the HQL Query with an insert or update.
     * @return The number of rows inserted updated.
     */
    public Integer update() {
        Transaction transaction = null;
        try (Session session = Radon.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            var query = session.createQuery(queryString).setMaxResults(1);
            variables.forEach(query::setParameter);
            variableLists.forEach(query::setParameterList);
            return query.executeUpdate();
        } catch (Exception e) {
            Log.err(e);
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            return null;
        }
    }

    /**
     * Runs the HQL Query and returns a single row.
     * @return The first row as an Object
     */
    @Nullable
    public Object getSingle() {
        Transaction transaction = null;
        try (Session session = Radon.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            var query = session.createQuery(queryString).setMaxResults(1);
            variables.forEach(query::setParameter);
            variableLists.forEach(query::setParameterList);
            return query.uniqueResult();
        } catch (Exception e) {
            Log.err(e);
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            return null;
        }
    }

    /**
     * Runs the HQL Query and returns a single row.
     * @param tClass Entity class
     * @return The first row as an Entity class
     */
    @Nullable
    public <T> T getSingle(Class<T> tClass) {
        Transaction transaction = null;
        try (Session session = Radon.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            var query = session.createQuery(queryString, tClass).setMaxResults(1);
            variables.forEach(query::setParameter);
            variableLists.forEach(query::setParameterList);
            return query.uniqueResult();
        } catch (Exception e) {
            Log.err(e);
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            return null;
        }
    }

    /**
     * Runs the HQL Query and returns every row.
     *
     * @return Returns a List of rows
     */
    @Nullable
    public List<Object> getMultiple() {
        return getMultiple(0);
    }

    /**
     * Runs the HQL Query and returns every row.
     *
     * @param limit Limits the amount of rows returned if value > 0
     * @return Returns a List of rows
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public List<Object> getMultiple(int limit) {
        Transaction transaction = null;
        try (Session session = Radon.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            var query = session.createQuery(queryString);
            if (limit > 0) query.setMaxResults(limit);
            variables.forEach(query::setParameter);
            variableLists.forEach(query::setParameterList);
            return query.list();
        } catch (Exception e) {
            Log.err(e);
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            return null;
        }
    }

    /**
     * Runs the HQL Query and returns every row.
     *
     * @param tClass Returns a List of entity class
     * @return Returns a List of tClass
     */

    @Nullable
    public <T> List<T> getMultiple(Class<T> tClass) {
        Transaction transaction = null;
        try (Session session = Radon.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            var query = session.createQuery(queryString, tClass);
            variables.forEach(query::setParameter);
            variableLists.forEach(query::setParameterList);
            return query.list();
        } catch (Exception e) {
            Log.err(e);
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            return null;
        }
    }

    /**
     * Runs the HQL Query and returns every row.
     *
     * @param limit Limits the amount of rows returned
     * @param tClass Entity class
     * @return Returns a List of entity class
     */
    @Nullable
    public <T> List<T> getMultiple(int limit, Class<T> tClass) {
        Transaction transaction = null;
        try (Session session = Radon.sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            var query = session.createQuery(queryString, tClass);
            query.setMaxResults(limit);
            variables.forEach(query::setParameter);
            variableLists.forEach(query::setParameterList);
            return query.list();
        } catch (Exception e) {
            Log.err(e);
            if (transaction != null && transaction.isActive())
                transaction.rollback();
            return null;
        }
    }
}
