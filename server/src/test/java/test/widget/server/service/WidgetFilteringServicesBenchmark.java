package test.widget.server.service;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import test.widget.server.domain.Area;
import test.widget.server.domain.Widget;
import test.widget.server.service.impl.BasicWidgetFilteringService;
import test.widget.server.service.impl.RTreeWidgetFilteringService;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@link WidgetFilteringService} implementations benchmark.
 *
 * @author Mikhail Kondratev
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 10)
@Fork(1)
@State(Scope.Benchmark)
public class WidgetFilteringServicesBenchmark {

    /**
     * {@link WidgetFilteringService} implementation name param.
     */
    @Param({"BASIC", "TREE"})
    @SuppressWarnings("unused")
    private ServiceType serviceType;

    /**
     * Widgets count to be generated.
     */
    @Param({"10", "100", "1000", "10000", "100000", "1000000"})
    @SuppressWarnings("unused")
    private Integer widgetsCount;

    /**
     * Service to be measured.
     */
    private WidgetFilteringService service;

    /**
     * Filtering area.
     */
    private Area area;

    /**
     * Widgets to be filtered.
     */
    private List<Widget> widgets;

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(WidgetFilteringServicesBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }


    @Setup
    public void setup() {
        area = WidgetFilteringServiceTestUtils.createArea();
        widgets = WidgetFilteringServiceTestUtils.generateWidgets(widgetsCount);

        switch (serviceType) {
            case BASIC:
                service = new BasicWidgetFilteringService();
                break;

            case TREE:
                service = new RTreeWidgetFilteringService();
                break;
        }

    }

    @Benchmark
    public void benchmark(final Blackhole blackhole) {
        blackhole.consume(service.filterWidgetsInsideArea(widgets, area));
    }

    /**
     * {@link WidgetFilteringService} type as an enum for choosing implementation based on param.
     */
    public enum ServiceType {
        BASIC(BasicWidgetFilteringService.class),
        TREE(RTreeWidgetFilteringService.class);

        ServiceType(@SuppressWarnings("unused") final Class<? extends WidgetFilteringService> serviceClass) {
        }
    }
}
