package test.widget.server.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Area for filtering widgets.
 *
 * @author Mikhail Kondratev
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Area {

    /**
     * Empty area object.
     */
    public static final Area EMPTY_AREA = new Area(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

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
