/**
 *
 */
package org.fcrepo.kernel.impl;

import java.util.Iterator;

import org.fcrepo.kernel.PropertiesUpdateTactic;
import org.fcrepo.kernel.utils.iterators.RdfStream;

import com.hp.hpl.jena.graph.Triple;


/**
 * {@link PropertiesUpdateTactic} that simply adds triples/properties.
 *
 * @author ajs6f
 * @date Feb 6, 2014
 */
public class AddUpdateTactic implements PropertiesUpdateTactic {

    private final Iterator<Triple> newTriples;

    /**
     * @param triples
     */
    public AddUpdateTactic(final Iterator<Triple> triples) {
        this.newTriples = triples;
    }

    /* (non-Javadoc)
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    @Override
    public RdfStream apply(final RdfStream input) {
        return input.concat(newTriples);
    }

}
