package test.widget.server.domain;

import lombok.Data;

/**
 * Area for filtering widgets.
 *
 * @author Mikhail Kondratev
 */
@Data
public class Area {

    /**
     * X-coordinate.
     */
    private int x;

    /**
     * Y-coordinate.
     */
    private int y;

    /**
     * Area width.
     */
    private int width;

    /**
     * Area height.
     */
    private int height;
}
