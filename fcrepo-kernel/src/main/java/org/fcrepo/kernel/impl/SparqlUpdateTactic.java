/**
 * Copyright 2013 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package org.fcrepo.kernel.impl;

import static com.hp.hpl.jena.query.DatasetFactory.create;
import static com.hp.hpl.jena.update.UpdateAction.execute;
import static com.hp.hpl.jena.update.UpdateFactory.create;
import static org.slf4j.LoggerFactory.getLogger;

import org.fcrepo.kernel.PropertiesUpdateTactic;
import org.fcrepo.kernel.utils.iterators.RdfStream;
import org.slf4j.Logger;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateRequest;


/**
 * {@link PropertiesUpdateTactic} that supports SPARQL Update.
 *
 * @author ajs6f
 * @date Feb 6, 2014
 */
public class SparqlUpdateTactic implements PropertiesUpdateTactic {

    private final String updateStatement;

    private static final Logger LOGGER = getLogger(SparqlUpdateTactic.class);

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

        final Model model = input.asModel();
        LOGGER.debug("Updating RDF:\n{}", model);
        final Dataset dataset = create(model);
        LOGGER.debug("Using SPARQL Update statement:\n{}", updateStatement);
        final UpdateRequest request =
            create(updateStatement, input.topic().toString());

        dataset.getDefaultModel().setNsPrefixes(request.getPrefixMapping());
        execute(request, dataset);
        final Model result = dataset.getDefaultModel();
        LOGGER.debug("Returning RDF:\n{}", result);
        return RdfStream.fromModel(result);
    }

}
