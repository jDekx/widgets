package test.widget.server.controller.params;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Class for passing widget filtering rectangle to controllers.
 *
 * @author Mikhail Kondratev
 */
@Data
public class FilteringParams {

    /**
     * Lower left corner x-coordinate.
     */
    @NotNull
    private Integer x;

    /**
     * Lower left corner y-coordinate.
     */
    @NotNull
    private Integer y;

    /**
     * Area width.
     */
    @NotNull
    private Integer width;

    /**
     * Area height.
     */
    @NotNull
    private Integer height;
}
