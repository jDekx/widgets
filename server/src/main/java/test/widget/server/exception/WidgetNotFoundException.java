package test.widget.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.text.MessageFormat;

/**
 * This type of exception is thrown when application couldn't find requested widget with specified id.
 *
 * @author Mikhail Kondratev
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Cannot find widget with specified id")
public class WidgetNotFoundException extends RuntimeException {

    /**
     * Constructor.
     *
     * @param widgetId widget identifier.
     */
    public WidgetNotFoundException(final String widgetId) {
        super(MessageFormat.format("Cannot find widget with specified id: {0}", widgetId));
    }
}
