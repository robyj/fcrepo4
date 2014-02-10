/**
 *
 */
package org.fcrepo.kernel.utils.iterators;

import static org.fcrepo.kernel.rdf.ManagedRdf.isManagedTriple;

import java.util.Iterator;

import org.fcrepo.kernel.exception.MalformedRdfException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.hp.hpl.jena.graph.Triple;


/**
 * An {@link RdfStream} that can produce only unmanaged triples.
 * Any managed triple that comes through this stream will throw an exception.
 *
 * @author ajs6f
 * @date Feb 10, 2014
 */
public class UnmanagedRdfStream extends RdfStream {

    /**
     * @param stream
     */
    public UnmanagedRdfStream(final RdfStream stream) {
        super(stream.iterator());
        namespaces(stream.namespaces());
        topic(stream.topic());
    }

    @Override
    public Iterator<Triple> delegate() {
        return Iterators.filter(super.delegate(), mustBeUnmanagedTriple);
    }

    private static Predicate<Triple> mustBeUnmanagedTriple = new Predicate<Triple>() {

        @Override
        public boolean apply(final Triple t) {
            if (isManagedTriple.apply(t)) {
                throw new IllegalArgumentException(new MalformedRdfException(
                        "Discovered triple with managed predicate or type! " + t));
            }
            return true;
        }

    };
}
