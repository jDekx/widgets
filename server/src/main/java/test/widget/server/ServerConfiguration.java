package test.widget.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import test.widget.server.controller.WidgetController;
import test.widget.server.service.WidgetFilteringService;
import test.widget.server.service.WidgetService;
import test.widget.server.service.impl.RTreeWidgetFilteringService;

/**
 * Widget server spring context configuration.
 *
 * @author Mikhail Kondratev
 */
@Configuration
@ComponentScan(basePackageClasses = {WidgetController.class, WidgetService.class})
public class ServerConfiguration {

    @Bean
    public WidgetFilteringService widgetFilteringService() {
        return new RTreeWidgetFilteringService();
    }

}
