package test.widget.server.repository;

import test.widget.server.domain.Widget;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for storing and accessing widgets.
 *
 * @author Mikhail Kondratev
 */
public interface WidgetRepository {

    /**
     * Finds widget by it's id.
     *
     * @param id widget identifier.
     * @return found widget or {@link Optional#empty()} if widget with specified id was not found.
     */
    Optional<Widget> findById(final String id);

    /**
     * Saves specified widget in repository.
     *
     * @param widget widget to be saved in repository.
     */
    void save(final Widget widget);

    /**
     * Deletes widget with specified identifier.
     * If widget with specified id does not exist - do nothing.
     *
     * @param id identifier of a widget to be removed.
     */
    void deleteById(final String id);

    /**
     * Gets all widget from repository.
     *
     * @return list of all widgets in repository.
     */
    List<Widget> findAll();
}
