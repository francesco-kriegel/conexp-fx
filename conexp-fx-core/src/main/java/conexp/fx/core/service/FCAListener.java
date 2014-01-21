package conexp.fx.core.service;

public abstract class FCAListener<G, M> {

  protected FCAInstance<G, M> fcaInstance;

  protected FCAListener() {
    super();
  }

  protected final void initializeInstance(final FCAInstance<G, M> fcaInstance) {
    this.fcaInstance = fcaInstance;
    this.initializeListeners();
    this.fcaInstance.addListener(this);
  }

  protected abstract void initializeListeners();
}
