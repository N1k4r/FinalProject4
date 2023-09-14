package org.javarash.dao;

import jakarta.transaction.Transactional;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.javarash.domain.Country;

import java.util.List;
@Transactional
public class CountryDAO {
    private final SessionFactory sessionFactory;

    public CountryDAO(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }

    public List<Country> getAll(){
        Query<Country> countryQuery = sessionFactory.getCurrentSession().createQuery("select c from Country c join fetch c.countryLanguages", Country.class);
        return countryQuery.list();
    }
}
