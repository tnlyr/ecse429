package unitTest;

import com.sun.management.OperatingSystemMXBean;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * Utility class that provides performance measurements and CSV logging.
 */
public class PerformanceUtils {

    // ---------------- CPU & MEMORY METRICS ----------------

    public static double sampleCpuLoad(long startMillis, long endMillis) {
        OperatingSystemMXBean os =
                ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

        double sum = 0.0;
        int samples = 0;

        while (System.currentTimeMillis() < endMillis) {
            sum += os.getCpuLoad();
            samples++;

            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        return samples > 0 ? (sum / samples) * 100.0 : 0.0;
    }

    public static long getCurrentMemoryUsage() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    // ---------------- CSV LOGGING ----------------

    /**
     * Saves a benchmark result as a new CSV record.
     *
     * @param filePath   output CSV file
     * @param label      operation or benchmark description
     * @param count      number of processed items
     * @param durationMs execution time in ms
     * @param cpu        CPU usage percentage
     * @param memory     memory usage in bytes
     */
    public static void logToCsv(
            String filePath,
            String label,
            int count,
            long durationMs,
            double cpu,
            long memory
    ) {
        try (FileWriter fw = new FileWriter(filePath, true)) {
            fw.write(label + "," +
                    count + "," +
                    durationMs + "," +
                    cpu + "," +
                    memory + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
