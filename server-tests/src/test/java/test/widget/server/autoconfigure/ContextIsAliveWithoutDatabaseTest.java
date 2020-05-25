package test.widget.server.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import test.widget.server.repository.WidgetRepository;
import test.widget.server.repository.impl.WidgetHeapRepository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing context startup without database.
 *
 * @author Mikhail Kondratev
 */
@ActiveProfiles("test")
public class ContextIsAliveWithoutDatabaseTest extends ContextIsAliveTestBase {

    /**
     * Asserts that spring context is alive and bean of type {@link WidgetHeapRepository}
     * is created when profile includes h2 database.
     */
    @Test
    public void testContextWidgetRepositoryType() {
        assertThat(getApplicationContext().getBean(WidgetRepository.class))
                .isInstanceOf(WidgetHeapRepository.class);
    }
}
