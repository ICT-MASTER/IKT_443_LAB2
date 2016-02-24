package no.uia.client;
/******************************************************************************
 *  Compilation:  javac Stopwatch.java
 *  Execution:    java Stopwatch n
 *  Dependencies: none
 *
 *  A utility class to measure the running time (wall clock) of a program.
 *
 *  % java8 Stopwatch 100000000
 *  6.666667e+11  0.5820 seconds
 *  6.666667e+11  8.4530 seconds
 *
 ******************************************************************************/

import java.util.ArrayList;

/**
 *  The <tt>Stopwatch</tt> data type is for measuring
 *  the time that elapses between the start and end of a
 *  programming task (wall-clock time).
 *
 *  See {@link StopwatchCPU} for a version that measures CPU time.
 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */


public class Stopwatch {

    private final long start;

    /**
     * Initializes a new stopwatch.
     */
    private ArrayList<Long> lap = new ArrayList<Long>();

    public Stopwatch() {
        start = System.currentTimeMillis();
        lap.add(start);
    }


    /**
     *
     * @return diff between last
     */
    public long record(){
        long now = System.currentTimeMillis();
        lap.add(now);

        return now - lap.get(lap.size()-2);
    }

    public long diff(long now){
        return now - lap.get(lap.size()-1);
    }
    /**
     * Returns the elapsed CPU time (in seconds) since the stopwatch was created.
     *
     * @return elapsed CPU time (in seconds) since the stopwatch was created
     */
    public double elapsedTime() {
        long now = System.currentTimeMillis();
        return (now - start) / 1000.0;
    }
} 