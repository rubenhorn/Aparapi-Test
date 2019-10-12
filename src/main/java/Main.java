import com.aparapi.Kernel;
import com.aparapi.Range;
import com.aparapi.device.Device;
import com.aparapi.device.OpenCLDevice;

public class Main {
    public static void main(String... args) {
        int max = (int) 1e5;
        long start;

        System.out.format("Checking numbers for primes from 1 to %d by division\n", max);

// ---------- Plain Java
        int[] resSequential = new int[max];

        System.out.print("Sequential... ");
        start = System.currentTimeMillis();
        for(int n = 1; n <= max; n++) {
            int isPrime = 1;
            for (int i = n / 2; i > 1; i--) {
                if(n % i == 0) {
                   isPrime = 0;
                }
            }
            resSequential[n - 1] = isPrime;
        }
        System.out.format("finished in %d ms\n", System.currentTimeMillis() - start);

// ---------- Aparapi
        final int[] resParallel = new int[max];

        System.out.format("Parallel using %s... ", Device.best().getShortDescription());
        start = System.currentTimeMillis();
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int n = getGlobalId() + 1;
                int isPrime = 1;
                for (int i = n / 2; i > 1; i--) {
                    if(n % i == 0) {
                        isPrime = 0;
                    }
                }
                resParallel[n - 1] = isPrime;
            }
        };
        Range range = Range.create(resParallel.length);
        kernel.execute(range);
        System.out.format("finished in %d ms\n", System.currentTimeMillis() - start);

        System.out.print("Validating results... ");

// ---------- Validation
        for (int i = 0; i < max; i++) {
            if(resParallel[i] != resSequential[i]) {
                throw new RuntimeException(String.format(
                    "Different results for %d\n(Got %d from parallel code and %d from sequential code)",
                    i, resParallel[i], resSequential[i]));
            }
        }
        System.out.println("done");
    }
}
