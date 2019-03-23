package conexp.fx.core.algorithm.exploration;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2019 Francesco Kriegel
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.Arrays;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import conexp.fx.core.context.Implication;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public class ExpertPool<G, M> implements Expert<G, M> {

  enum Strategy {
    ANSWER_FROM_FIRST_IDLE("Chooses a random idle expert and returns its/his/her answer."),
    FIRST_ANSWER_FROM_IDLE("Use answer from fastest idle expert"),
    FIRST_ANSWER("Use answer from fastest expert"),
    SOME_CONFIRMS(
        "Confirm all implications that are confirmed by at least one of the experts, i.e., reject all implications that are rejected by all experts."),
    ALL_CONFIRM(
        "Confirm all implications that confirmed by all experts, i.e., reject all implications that are rejected by at least one of the experts.");

    private final String description;

    private Strategy(final String description) {
      this.description = description;
    }

    public String getDescription() {
      return this.description;
    }

  }

  private final ObservableMap<Expert<G, M>, Boolean> experts = FXCollections.observableMap(new ConcurrentHashMap<>());
  private final Strategy                             strategy;
  private final ExecutorService                      exe;

  public ExpertPool(final ExpertPool.Strategy strategy, final ExecutorService executor) {
    this.strategy = strategy;
    this.exe = executor;
    this.experts.addListener((MapChangeListener<Expert<G, M>, Boolean>) change -> {
      if (change.wasAdded() && change.wasRemoved()) {
        System.out.println(change.getKey() + " IS NOW " + (change.getValueAdded() ? "IDLE" : "BUSY"));
      } else if (change.wasAdded()) {
        System.out.println(change.getKey() + " WAS ADDED AND IS " + (change.getValueAdded() ? "IDLE" : "BUSY"));
      } else if (change.wasRemoved()) {
        System.out.println(change.getKey() + " WAS REMOVED AND WAS " + (change.getValueRemoved() ? "IDLE" : "BUSY"));
      }
    });
  }

  @Override
  public Set<CounterExample<G, M>> getCounterExamples(Implication<G, M> implication) throws InterruptedException {
    switch (strategy) {
    case ANSWER_FROM_FIRST_IDLE:
      return getIdleExpert().getCounterExamples(implication);
    case FIRST_ANSWER_FROM_IDLE:
      try {
        return exe.invokeAny(
            this.experts
                .entrySet()
                .parallelStream()
                .filter(entry -> entry.getValue().equals(true))
                .map(entry -> entry.getKey())
                .map(expert -> (Callable<Set<CounterExample<G, M>>>) () -> {
                  try {
                    return expert.getCounterExamples(implication);
                  } catch (Exception e) {
                    return null;
                  }
                })
                .collect(Collectors.toSet()));
      } catch (ExecutionException e) {
        return null;
      }
    case FIRST_ANSWER:
      try {
        return exe.invokeAny(
            this.experts.keySet().parallelStream().map(expert -> (Callable<Set<CounterExample<G, M>>>) () -> {
              try {
                return expert.getCounterExamples(implication);
              } catch (Exception e) {
                return null;
              }
            }).collect(Collectors.toSet()));
      } catch (ExecutionException e) {
        return null;
      }
    case SOME_CONFIRMS:
      if (experts.keySet().parallelStream().map(expert -> {
        try {
          return expert.getCounterExamples(implication);
        } catch (Exception __) {
          return null;
        }

      }).anyMatch(cex -> cex.isEmpty()))
        return Collections.emptySet();
    case ALL_CONFIRM:
      return experts.keySet().parallelStream().map(expert -> {
        try {
          return expert.getCounterExamples(implication);
        } catch (Exception __) {
          return null;
        }
      }).flatMap(Set::parallelStream).collect(Collectors.toSet());
    default:
      return Collections.emptySet();
    }

  }

  @SafeVarargs
  public final void add(final Expert<G, M>... experts) {
    add(Arrays.asList(experts));
  }

  public final void add(final Iterable<Expert<G, M>> experts) {
    experts.forEach(expert -> this.experts.put(expert, true));
  }

  @SafeVarargs
  public final void remove(final Expert<G, M>... experts) {
    remove(Arrays.asList(experts));
  }

  public final void remove(final Iterable<Expert<G, M>> experts) {
    experts.forEach(expert -> this.experts.remove(expert));
  }

  public final boolean isIdle(final Expert<G, M> expert) {
    return this.experts.get(expert);
  }

  public final boolean allIdle() {
    return !this.experts.values().contains(false);
  }

  public final boolean someIdle() {
    return this.experts.values().contains(true);
  }

  public final void setBusy(final Expert<G, M> expert) {
    if (!this.experts.containsKey(expert))
      throw new IllegalArgumentException();
    this.experts.put(expert, false);
  }

  public final void setIdle(final Expert<G, M> expert) {
    if (!this.experts.containsKey(expert))
      throw new IllegalArgumentException();
    this.experts.put(expert, true);
  }

  public final Expert<G, M> getIdleExpert() throws NoSuchElementException {
    return this.experts
        .entrySet()
        .parallelStream()
        .filter(entry -> entry.getValue().equals(true))
        .map(entry -> entry.getKey())
        .findAny()
        .get();
  }

  public final Strategy getStrategy() {
    return this.strategy;
  }

}
