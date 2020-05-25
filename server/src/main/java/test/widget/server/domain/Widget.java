package test.widget.server.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Data class for storing widget attribute information.
 *
 * @author Mikhail Kondratev
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Widget implements Cloneable {

    /**
     * Identifier.
     */
    @Id
    @Nullable
    private String id;

    /**
     * X-coordinate.
     */
    private int x;

    /**
     * Y-coordinate.
     */
    private int y;

    /**
     * Z index.
     * The higher the value, the higher the widget lies on the plane.
     */
    private int z;

    /**
     * Width of a widget.
     */
    private int width;

    /**
     * Height of a widget.
     */
    private int height;

    /**
     * Date and time of last modification of this object.
     */
    @Nullable
    private LocalDateTime lastModified;

    /**
     * If object is newly created.
     */
    @Transient
    private boolean isNew;

    @Override
    public Widget clone() throws CloneNotSupportedException {
        return (Widget) super.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Widget)) return false;
        Widget widget = (Widget) o;
        return Objects.requireNonNull(getId()).equals(widget.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
