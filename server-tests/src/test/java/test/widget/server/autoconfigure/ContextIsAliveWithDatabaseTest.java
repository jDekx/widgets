package test.widget.server.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import test.widget.server.repository.WidgetRepository;
import test.widget.server.repository.impl.WidgetDatabaseRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing context startup with embedded database.
 *
 * @author Mikhail Kondratev
 */
@ActiveProfiles("test-h2")
public class ContextIsAliveWithDatabaseTest extends ContextIsAliveTestBase {

    /**
     * Asserts that spring context is alive and bean of type {@link WidgetDatabaseRepository}
     * is created when profile includes h2 database.
     */
    @Test
    public void testContextWidgetRepositoryType() {
        assertThat(getApplicationContext().getBean(WidgetRepository.class))
                .isInstanceOf(WidgetDatabaseRepository.class);
    }
}
