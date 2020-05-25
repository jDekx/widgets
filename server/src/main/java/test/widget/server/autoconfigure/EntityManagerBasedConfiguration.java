package test.widget.server.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import test.widget.server.domain.Widget;
import test.widget.server.repository.WidgetRepository;
import test.widget.server.repository.impl.WidgetDatabaseRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

/**
 * Auto-configuration for creating {@link WidgetRepository} if any type of {@link DataSource} exists.
 *
 * @author Mikhail Kondratev
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(HeapWidgetRepositoryConfiguration.class)
@ConditionalOnMissingBean(WidgetRepository.class)
@Import({DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@EnableTransactionManagement
class EntityManagerBasedConfiguration {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public WidgetRepository widgetJpaRepository() {
        return new WidgetDatabaseRepository(new SimpleJpaRepository<>(Widget.class, entityManager));
    }

}
