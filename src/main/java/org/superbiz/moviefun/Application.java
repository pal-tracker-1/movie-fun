package org.superbiz.moviefun;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean actionServletRegistration(ActionServlet actionServlet) {
        return new ServletRegistrationBean(actionServlet, "/moviefun/*");
    }

    @Bean
    public DatabaseServiceCredentials databaseServiceCredentials(@Value("${vcap.services:}") String vcapServices) {
        return new DatabaseServiceCredentials(vcapServices);
    }

    @Bean
    public DataSource albumsDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(serviceCredentials.jdbcUrl("albums-mysql"));
        return new HikariDataSource(config);
    }


    @Bean
    public DataSource moviesDataSource(DatabaseServiceCredentials serviceCredentials) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(serviceCredentials.jdbcUrl("movies-mysql"));
        return new HikariDataSource(config);
    }

    @Bean
    public HibernateJpaVendorAdapter hibernateJpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setDatabasePlatform("org.hibernate.dialect.MySQL5Dialect");
        adapter.setDatabase(Database.MYSQL);
        adapter.setGenerateDdl(true);

        return adapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean moviesEntityManager(DataSource moviesDataSource, HibernateJpaVendorAdapter adapter) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(moviesDataSource);
        emf.setJpaVendorAdapter(adapter);
        emf.setPackagesToScan("org.superbiz.moviefun.movies");
        emf.setPersistenceUnitName("movies");
        return emf;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean albumsEntityManager(DataSource albumsDataSource, HibernateJpaVendorAdapter adapter) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(albumsDataSource);
        emf.setJpaVendorAdapter(adapter);
        emf.setPackagesToScan("org.superbiz.moviefun.albums");
        emf.setPersistenceUnitName("albums");
        return emf;
    }

    @Bean
    public PlatformTransactionManager albumsPlatformTransactionManager(EntityManagerFactory albumsEntityManager) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(albumsEntityManager);
        return transactionManager;
    }

    @Bean
    public PlatformTransactionManager moviesPlatformTransactionManager(EntityManagerFactory moviesEntityManager) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(moviesEntityManager);
        return transactionManager;
    }


}
