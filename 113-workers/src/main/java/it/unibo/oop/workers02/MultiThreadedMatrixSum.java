package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

/**
 * This is an implementation using streams.
 *
 */
@SuppressWarnings("CPD-START")
public final class MultiThreadedMatrixSum implements SumMatrix {

    private final int nthread;

    /**
     * Builds a multithreaded list sum using streams.
     *
     * @param nthread
     *            no. of thread performing the sum.
     */
    public MultiThreadedMatrixSum(final int nthread) {
        this.nthread = nthread;
    }

    @Override
    public double sum(final double[][] matrix) {
        final List<Double> list = new ArrayList<>(nthread);
        for (final double[] array : matrix) {
            Arrays.stream(array).forEach(list::add);
        }

        final int size = list.size() % nthread + list.size() / nthread;
        /*
         * Build a stream of workers
         */
        return IntStream.iterate(0, start -> start + size)
            .limit(nthread)
            .mapToObj(start -> new Worker(list, start, size))
            // Start them
            .peek(Thread::start)
            // Join them
            .peek(MultiThreadedMatrixSum::joinUninterruptibly)
            // Get their result and sum
            .mapToLong(Worker::getResult)
            .sum();
    }

    @SuppressWarnings("PMD.AvoidPrintStackTrace")
    private static void joinUninterruptibly(final Thread target) {
        var joined = false;
        while (!joined) {
            try {
                target.join();
                joined = true;
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Worker extends Thread {
        private final List<Double> list;
        private final int startpos;
        private final int nelem;
        private long res;

        /**
         * Build a new worker.
         *
         * @param list
         *            the list to sum
         * @param startpos
         *            the initial position for this worker
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        Worker(final List<Double> list, final int startpos, final int nelem) {
            super();
            this.list = list;
            this.startpos = startpos;
            this.nelem = nelem;
        }

        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public synchronized void run() {
            System.out.println("Working from position " + startpos + " to position " + (startpos + nelem - 1));
            for (int i = startpos; i < list.size() && i < startpos + nelem; i++) {
                this.res += this.list.get(i);
            }
        }

        /**
         * Returns the result of summing up the integers within the list.
         *
         * @return the sum of every element in the array
         */
        public synchronized long getResult() {
            return this.res;
        }

    }
}
