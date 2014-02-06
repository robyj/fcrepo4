/**
 *
 */
package org.fcrepo.kernel.impl;

import static com.hp.hpl.jena.update.UpdateAction.execute;
import static com.hp.hpl.jena.update.UpdateFactory.create;
import static org.fcrepo.kernel.rdf.GraphProperties.URI_SYMBOL;

import org.fcrepo.kernel.PropertiesUpdateTactic;
import org.fcrepo.kernel.utils.iterators.RdfStream;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.update.UpdateRequest;


/**
 * {@link PropertiesUpdateTactic} that supports SPARQL Update.
 *
 * @author ajs6f
 * @date Feb 6, 2014
 */
public class SparqlUpdateTactic implements PropertiesUpdateTactic {

    private final String updateStatement;

    /**
     * @param updateStatem
     */
    public SparqlUpdateTactic(final String updateStatem) {
        this.updateStatement = updateStatem;
    }

    /* (non-Javadoc)
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    @Override
    public RdfStream apply(final RdfStream input) {
        final Dataset dataset = DatasetFactory.assemble(input.asModel());
        final UpdateRequest request =
            create(updateStatement, dataset.getContext().getAsString(
                    URI_SYMBOL));
        dataset.getDefaultModel().setNsPrefixes(request.getPrefixMapping());
        execute(request, dataset);
        return RdfStream.fromModel(dataset.getDefaultModel());
    }

}
