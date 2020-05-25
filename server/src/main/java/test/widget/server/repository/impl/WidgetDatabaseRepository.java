package test.widget.server.repository.impl;

import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import test.widget.server.domain.Widget;
import test.widget.server.repository.WidgetRepository;

import java.util.List;
import java.util.Optional;

/**
 * {@link WidgetRepository} implementation that stores objects in DB.
 *
 * @author Mikhail Kondratev
 */
public class WidgetDatabaseRepository implements WidgetRepository {

    /**
     * Repository for accessing widget objects.
     */
    private final SimpleJpaRepository<Widget, String> widgetSimpleJpaRepository;

    public WidgetDatabaseRepository(final SimpleJpaRepository<Widget, String> widgetSimpleJpaRepository) {
        this.widgetSimpleJpaRepository = widgetSimpleJpaRepository;
    }

    @Override
    public Optional<Widget> findById(final String id) {
        return widgetSimpleJpaRepository.findById(id);
    }

    @Override
    public void save(final Widget widget) {
        widgetSimpleJpaRepository.saveAndFlush(widget);
    }

    @Override
    public void deleteById(final String id) {
        //avoiding 'entity does not exist' exception from repository.
        widgetSimpleJpaRepository.findById(id)
                .ifPresent(widgetSimpleJpaRepository::delete);
    }

    @Override
    public List<Widget> findAll() {
        return widgetSimpleJpaRepository.findAll();
    }
}
