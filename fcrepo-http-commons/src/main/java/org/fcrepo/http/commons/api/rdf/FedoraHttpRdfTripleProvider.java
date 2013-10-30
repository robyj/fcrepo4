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
package org.fcrepo.http.commons.api.rdf;

import org.fcrepo.kernel.FedoraResource;
import org.fcrepo.kernel.rdf.GraphSubjects;
import org.fcrepo.kernel.utils.iterators.RdfStream;

import javax.jcr.RepositoryException;
import javax.ws.rs.core.UriInfo;

public interface FedoraHttpRdfTripleProvider {
    /**
     * When triples are being serialized for the client, {@link HttpTripleUtil}
     * will scan @Components with this interface, and call this method to
     * give them an opportunity to inject additional triples (not represented
     * in the data, e.g. to JAX-RS resources)
     *
     * @param graphSubjects
     * @param resource
     * @return
     * @throws RepositoryException
     */
    RdfStream getRdfStream(final GraphSubjects graphSubjects, final FedoraResource resource, final UriInfo uriInfo) throws RepositoryException;
}
