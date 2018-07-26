package algorithm;

/**
 * This interface provides a way to run an algorithm
 * on a thread as a {@link Runnable} object.
 *
 * @author Ritwik Banerjee
 */
public interface Algorithm extends Runnable {

    int getMaxIterations();

    int getUpdateInterval();

    boolean tocontinue();

}