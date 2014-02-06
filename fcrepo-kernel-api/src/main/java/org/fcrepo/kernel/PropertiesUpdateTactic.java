package org.fcrepo.kernel;

import org.fcrepo.kernel.utils.iterators.RdfStream;

import com.google.common.base.Function;


/**
 * Represents the calculation (but not the persistence) of an update to a resource's properties.
 *
 * @author ajs6f
 * @date Feb 6, 2014
 */
public interface PropertiesUpdateTactic extends Function<RdfStream, RdfStream> {

}
