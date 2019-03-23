package conexp.fx.core.dl;

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

import org.semanticweb.owlapi.model.IRI;

import com.google.common.collect.Collections2;

import conexp.fx.core.collections.setlist.HashSetArrayList;
import conexp.fx.core.collections.setlist.SetList;

public final class Signature {

  private final IRI      baseIRI;
  private final SetList<IRI> conceptNames    = new HashSetArrayList<IRI>();
  private final SetList<IRI> roleNames       = new HashSetArrayList<IRI>();
  private final SetList<IRI> individualNames = new HashSetArrayList<IRI>();

  public Signature(final IRI baseIRI) {
    super();
    this.baseIRI = baseIRI;
  }

  public final IRI getBaseIRI() {
    return baseIRI;
  }

  public final SetList<IRI> getConceptNames() {
    return conceptNames;
  }

  public final SetList<IRI> getRoleNames() {
    return roleNames;
  }

  public final SetList<IRI> getIndividualNames() {
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
