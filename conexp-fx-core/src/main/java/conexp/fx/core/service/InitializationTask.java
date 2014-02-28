package conexp.fx.core.service;

import java.util.Iterator;

import conexp.fx.core.algorithm.nextclosure.NextConcept;
import conexp.fx.core.concurrent.BlockingTask;
import conexp.fx.core.context.Concept;
import conexp.fx.core.util.Constants;

final class InitializationTask<G, M> extends BlockingTask {

  /**
   * 
   */
  private final FCAInstance<G, M> fcaInstance;
  private final FCAInstance.InitLevel lvl;

  public InitializationTask(FCAInstance<G, M> fcaInstance, final FCAInstance.InitLevel lvl) {
    super("Displayability Test");
    this.fcaInstance = fcaInstance;
    this.lvl = lvl;
  }

  protected final void _call() {
    updateMessage("checking whether there are more than " + Constants.MAX_CONCEPTS + " concepts...");
    if (isDisplayable()) fcaInstance.init();
//    updateProgress(1,1);
  }

  private final boolean isDisplayable() {
//    if (this.fcaInstance.context.rowHeads().size() > 1000)
//      return false;
//    if (this.fcaInstance.context.colHeads().size() > 1000)
//      return false;

  	Iterator<Concept<G,M>> it = new NextConcept<G, M>(this.fcaInstance.context).iterator();
  	int i=0;
  	while (it.hasNext()){
  		it.next();
  		i++;
  	}
  	System.out.println(fcaInstance.id.get()+" -- number of concepts: "+i);
  	return i < Constants.MAX_CONCEPTS+1;
//    try {
//      Iterators.get(new NextConcept<G, M>(this.fcaInstance.context).iterator(), Constants.MAX_CONCEPTS + 1);
//    } catch (IndexOutOfBoundsException e) {
//      return true;
//    }
//    return false;
  }
}