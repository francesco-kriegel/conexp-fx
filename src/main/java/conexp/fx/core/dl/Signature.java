package conexp.fx.core.dl;

/*
 * #%L
 * Concept Explorer FX
 * %%
 * Copyright (C) 2010 - 2017 Francesco Kriegel
 * %%
 * You may use this software for private or educational purposes at no charge. Please contact me for commercial use.
 * #L%
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Collections2;

public final class Signature {

  private final IRI      baseIRI;
  private final Set<IRI> conceptNames    = new HashSet<IRI>();
  private final Set<IRI> roleNames       = new HashSet<IRI>();
  private final Set<IRI> individualNames = new HashSet<IRI>();

  public Signature(final IRI baseIRI) {
    super();
    this.baseIRI = baseIRI;
  }

  public final IRI getBaseIRI() {
    return baseIRI;
  }

  public final Set<IRI> getConceptNames() {
    return conceptNames;
  }

  public final Set<IRI> getRoleNames() {
    return roleNames;
  }

  public final Set<IRI> getIndividualNames() {
    return individualNames;
  }

  public final boolean addConceptNames(final IRI... conceptNames) {
    return this.conceptNames.addAll(Arrays.asList(conceptNames));
  }

  public final boolean addConceptNames(final String... conceptNames) {
    return this.conceptNames.addAll(Collections2.transform(Arrays.asList(conceptNames), IRI::create));
  }

  public final boolean addRoleNames(final IRI... roleNames) {
    return this.roleNames.addAll(Arrays.asList(roleNames));
  }

  public final boolean addRoleNames(final String... roleNames) {
    return this.roleNames.addAll(Collections2.transform(Arrays.asList(roleNames), IRI::create));
  }

  public final boolean addIndividualNames(final IRI... individualNames) {
    return this.individualNames.addAll(Arrays.asList(individualNames));
  }

  public final boolean addIndividualNames(final String... individualNames) {
    return this.individualNames.addAll(Collections2.transform(Arrays.asList(individualNames), IRI::create));
  }

  @Override
  public final boolean equals(final Object obj) {
    if (!(obj instanceof Signature))
      return false;
    final Signature other = (Signature) obj;
    return this.getConceptNames().equals(other.getConceptNames()) && this.getRoleNames().equals(other.getRoleNames())
        && this.getIndividualNames().equals(other.getIndividualNames());
  }

  @Override
  public final int hashCode() {
    return 17 + 23 * conceptNames.hashCode() + 29 * roleNames.hashCode() + 31 * individualNames.hashCode();
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("   concept names: " + conceptNames.toString() + "\r\n");
    sb.append("      role names: " + roleNames.toString() + "\r\n");
    sb.append("individual names: " + individualNames.toString() + "\r\n");
    return sb.toString();
  }

}
