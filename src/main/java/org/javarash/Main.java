package org.javarash;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisStringCommands;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.javarash.dao.CityDAO;
import org.javarash.dao.CountryDAO;
import org.javarash.domain.City;
import org.javarash.domain.Country;
import org.javarash.domain.CountryLanguage;
import org.javarash.redis.CityCountry;
import org.javarash.redis.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class Main {

    private static final SessionFactory sessionFactory;
    private static final RedisClient redisClient;
    private static final ObjectMapper objectMapper;
    private static final CityDAO cityDAO;
    private static final CountryDAO countryDAO;

    static {
        sessionFactory = new Configuration()
                .configure()
                .addAnnotatedClass(City.class)
                .addAnnotatedClass(Country.class)
                .addAnnotatedClass(CountryLanguage.class)
                .addAnnotatedClass(CityDAO.class)
                .addAnnotatedClass(CountryDAO.class)
                .addAnnotatedClass(CityDAO.class)
                .buildSessionFactory();

        redisClient = prepareRedisClient();
        cityDAO = new CityDAO(sessionFactory);
        countryDAO = new CountryDAO(sessionFactory);
        objectMapper = new ObjectMapper();
    }
    public static void main(String[] args) {
        pushToRedis(transformData(getAllCities()));
        sessionFactory.getCurrentSession().close();

        int countTest = 5;
        int redisMillis = 0;
        int mySqlMillis = 0;

        for (int i = 0; i < countTest; i++) {
            List<Integer> ids = getRandomListId();

            long startRedis = System.currentTimeMillis();
            testRedisData(ids);
            long stopRedis = System.currentTimeMillis();

            long startMysql = System.currentTimeMillis();
            testMysqlData(ids);
            long stopMysql = System.currentTimeMillis();

            long durationRedis = stopRedis - startRedis;
            long durationMysql = stopMysql - startMysql;

            redisMillis += (int) durationRedis;
            mySqlMillis += (int) durationMysql;

            System.out.printf("%d Test \t%s:\t%d ms | ", i+1, "Redis", durationRedis);
            System.out.printf("%s:\t%d ms\n", "MySQL", durationMysql);
        }
        shutdown();

        double fasterPercentage = Math.round(100 - ((double) redisMillis / mySqlMillis * 100));
        System.out.printf("Redis is faster than mySql on %d%s\nbased on the results of %d tests",
                                                            (int) fasterPercentage, "%", countTest);


    }

    public static List<Integer> getRandomListId(){
        int countId = new Random().nextInt(5, 10);
        List<Integer> listId = new ArrayList<>();
        for (int i = 0; i < countId; i++) {
            listId.add(new Random().nextInt(0, 4079));
        }
        return listId;
    }

    public static List<City> getAllCities(){
        try(Session session = sessionFactory.getCurrentSession()){
            session.beginTransaction();
            List<City> allCities = new ArrayList<>();
            int totalCount = cityDAO.getTotalCount();
            int step = 500;
            for (int i = 0; i < totalCount; i += step) {
                allCities.addAll(cityDAO.getItems(i, step));
            }
            session.getTransaction().commit();
            return allCities;
        }
    }

    private static List<CityCountry> transformData(List<City> cities) {
        return cities.stream().map(city -> {
            CityCountry res = new CityCountry();
            res.setId(city.getId());
            res.setName(city.getName());
            res.setPopulation(city.getPopulation());
            res.setDistrict(city.getDistrict());

            Country country = city.getCountry();
            res.setAlternativeCountryCode(country.getSecondCode());
            res.setContinent(country.getContinent());
            res.setCountryCode(country.getCode());
            res.setCountryName(country.getName());
            res.setCountryPopulation(country.getPopulation());
            res.setCountryRegion(country.getRegion());
            res.setCountrySurfaceArea(country.getSurfaceArea());
            Set<CountryLanguage> countryLanguages = country.getCountryLanguages();
            Set<Language> languages = countryLanguages.stream().map(cl -> {
                Language language = new Language();
                language.setLanguage(cl.getLanguage());
                language.setIsOfficial(cl.getIsOfficial());
                language.setPercentage(cl.getPercentage());
                return language;
            }).collect(Collectors.toSet());
            res.setLanguages(languages);

            return res;
        }).collect(Collectors.toList());
    }

    private static RedisClient prepareRedisClient() {
        RedisClient redisClient = RedisClient.create(RedisURI.create("localhost", 6379));
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            System.out.println("\nConnected to Redis\n");
        }
        return redisClient;
    }

    private static void pushToRedis(List<CityCountry> data) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (CityCountry cityCountry : data) {
                try {
                    sync.set(String.valueOf(cityCountry.getId()), objectMapper.writeValueAsString(cityCountry));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static void testRedisData(List<Integer> ids) {
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            RedisStringCommands<String, String> sync = connection.sync();
            for (Integer id : ids) {
                String value = sync.get(String.valueOf(id));
                try {
                    objectMapper.readValue(value, CityCountry.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void testMysqlData(List<Integer> ids) {
        try (Session session = sessionFactory.getCurrentSession()) {
            session.beginTransaction();
            for (Integer id : ids) {
                City city = cityDAO.getById(id);
                Set<CountryLanguage> languages = city.getCountry().getCountryLanguages();
            }
            session.getTransaction().commit();
            sessionFactory.getCurrentSession().close();
        }
    }

    private static void shutdown() {
        if (nonNull(sessionFactory)) {
            sessionFactory.close();
        }
        if (nonNull(redisClient)) {
            redisClient.shutdown();
        }
    }

}