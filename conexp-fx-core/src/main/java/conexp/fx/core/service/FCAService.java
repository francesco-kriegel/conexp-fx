package conexp.fx.core.service;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import conexp.fx.core.builder.Request;

/**
 * A service for formal concept analysis computations. It can hold several instances. Each instance represents a formal
 * context.
 * 
 * @author Francesco Kriegel, TU Dresden
 * 
 */
public final class FCAService {

  private boolean                isInitialized = false;
  private Set<FCAInstance<?, ?>> instances;
  private ThreadPoolExecutor     tpe;

  /**
   * Initializes the FCAService with one thread per processor.
   */
  public FCAService() {
    this(Runtime.getRuntime().availableProcessors());
  }

  /**
   * Initializes the FCAService with desired number of threads.
   * 
   * @param poolSize
   */
  public FCAService(final int poolSize) {
    if (isInitialized)
      return;
    isInitialized = true;
    instances = new HashSet<FCAInstance<?, ?>>();
    tpe = new ThreadPoolExecutor(poolSize, poolSize, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
  }

  /**
   * Submits a new request that describes a formal context.
   * 
   * @param fcaRequest
   */
  public final synchronized <G, M> FCAInstance<G, M> add(final Request<G, M> fcaRequest) {
    final FCAInstance<G, M> fcaInstance = new FCAInstance<G, M>(this, fcaRequest);
    instances.add(fcaInstance);
    return fcaInstance;
  }

  /**
   * Gets the FCAInstance with supplied id. Throws a NoSuchElementException if there is no FCAInstance with given id.
   * 
   * @param id
   * @return
   * @throws NoSuchElementException
   */
  public final synchronized FCAInstance<?, ?> get(final String id) throws NoSuchElementException {
    for (FCAInstance<?, ?> fcaInstance : instances)
      if (fcaInstance.id.get().equals(id))
        return fcaInstance;
    throw new NoSuchElementException("There is no FCAInstance with id: " + id);
  }

  /**
   * Return the ThreadPoolExecutor instance that is (or at least shall be) used for computations.
   * 
   * @return
   */
  protected final ThreadPoolExecutor tpe() {
    return tpe;
  }
}
