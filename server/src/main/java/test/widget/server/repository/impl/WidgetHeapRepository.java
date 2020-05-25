package test.widget.server.repository.impl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import test.widget.server.domain.Widget;
import test.widget.server.repository.WidgetRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link WidgetRepository} that holds object in memory using {@link ConcurrentHashMap}.
 *
 * @author Mikhail Kondratev
 */
@Slf4j
public class WidgetHeapRepository implements WidgetRepository {

    /**
     * Widgets in this repository. Key - widget identifier, value - widget itself.
     */
    private final Map<String, Widget> widgets = new ConcurrentHashMap<>();

    @Override
    public Optional<Widget> findById(final String id) {
        return Optional.ofNullable(widgets.get(id));
    }

    @Override
    @SneakyThrows(CloneNotSupportedException.class)
    public void save(final Widget widget) {
        final Widget cloned = widget.clone();
        cloned.setNew(false);
        widgets.put(widget.getId(), cloned);
    }

    @Override
    public void deleteById(String id) {
        widgets.remove(id);
    }

    @Override
    public List<Widget> findAll() {
        return new ArrayList<>(widgets.values());
    }
}
