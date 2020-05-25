package test.widget.server.controller.params;

import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * Class for holding pagination params.
 *
 * @author Mikhail Kondratev
 */
@Data
public class PaginationParams {

    /**
     * Page size.
     */
    @Nullable
    private Integer pageSize;

    /**
     * Offset elements count.
     * This value holds number of elements that should not be in result set.
     */
    @Nullable
    private Integer offset;
}
