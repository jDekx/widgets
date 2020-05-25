package test.widget.server.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import test.widget.server.repository.WidgetRepository;
import test.widget.server.repository.impl.WidgetHeapRepository;

/**
 * Auto-configuration for creating heap-based widget repository.
 *
 * @author Mikhail Kondratev
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "server.widget", name = "database-enabled", havingValue = "false", matchIfMissing = true)
class HeapWidgetRepositoryConfiguration {

    @Bean
    public WidgetRepository widgetHeapRepository() {
        return new WidgetHeapRepository();
    }
}
