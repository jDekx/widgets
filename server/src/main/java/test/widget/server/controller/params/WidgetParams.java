package test.widget.server.controller.params;

import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * Object for receiving widget parameters in controller.
 *
 * @author Mikhail Kondratev
 */
@Data
public class WidgetParams {

    /**
     * X coordinate.
     */
    @Nullable
    private Integer x;

    /**
     * Y coordinate.
     */
    @Nullable
    private Integer y;

    /**
     * Width.
     */
    @Nullable
    private Integer width;

    /**
     * Height.
     */
    @Nullable
    private Integer height;

    /**
     * Z index.
     */
    @Nullable
    private Integer z;
}
