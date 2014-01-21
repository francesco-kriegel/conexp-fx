package conexp.fx.core.test;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.annotation.AxisRange;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkHistoryChart;
import com.carrotsearch.junitbenchmarks.annotation.BenchmarkMethodChart;
import com.carrotsearch.junitbenchmarks.annotation.LabelType;

@AxisRange(min = 0, max = 1)
@BenchmarkMethodChart(filePrefix = "benchmarks/CLASSNAME-latest")
@BenchmarkHistoryChart(filePrefix = "benchmarks/CLASSNAME-history", labelWith = LabelType.CUSTOM_KEY, maxRuns = 20)
@BenchmarkOptions(callgc = true, benchmarkRounds = 1, warmupRounds = 0)
public abstract class TestBenchmark extends AbstractBenchmark {}
