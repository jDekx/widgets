package test.widget.server;

import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Global server configuration properties.
 *
 * @author Mikhail Kondratev
 */
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "server.widget")
public class ServerConfigurationProperties {

    /**
     * Timeout for trying to acquire lock, in seconds.
     */
    @Range(min = 1, max = Integer.MAX_VALUE)
    private int lockTimeout;

    /**
     * Default value for widget's z-index, if no widget exist.
     */
    @Range(min = Integer.MIN_VALUE, max = Integer.MAX_VALUE)
    private int initialZIndex;

    /**
     * Default page size when getting widgets.
     */
    @Range(min = 1, max = Integer.MAX_VALUE)
    private int pageDefaultSize;

    /**
     * Max page size when getting widgets.
     */
    @Range(min = 1, max = Integer.MAX_VALUE)
    private int pageMaxSize;

    /**
     * <code>true</code> - widgets should be stored in DB,
     * <code>false</code> - widgets should be stored in heap.
     */
    private boolean databaseEnabled;
}
