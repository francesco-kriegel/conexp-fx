package conexp.fx.core.collections.relation;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2015 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

@FunctionalInterface
public interface RelationEventHandler<R, C> {

  public void handle(RelationEvent<R, C> event);

}
