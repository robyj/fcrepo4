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

package org.fcrepo.http.api.url;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;
import static java.util.Collections.singletonMap;
import static org.fcrepo.jcr.FedoraJcrTypes.ROOT;
import static org.fcrepo.kernel.RdfLexicon.HAS_FIXITY_SERVICE;
import static org.fcrepo.kernel.RdfLexicon.HAS_NAMESPACE_SERVICE;
import static org.fcrepo.kernel.RdfLexicon.HAS_SEARCH_SERVICE;
import static org.fcrepo.kernel.RdfLexicon.HAS_SERIALIZATION;
import static org.fcrepo.kernel.RdfLexicon.HAS_SITEMAP;
import static org.fcrepo.kernel.RdfLexicon.HAS_TRANSACTION_SERVICE;
import static org.fcrepo.kernel.RdfLexicon.HAS_VERSION_HISTORY;
import static org.fcrepo.kernel.RdfLexicon.HAS_WORKSPACE_SERVICE;
import static org.fcrepo.kernel.RdfLexicon.RDFS_LABEL;

import java.util.Map;

import javax.jcr.RepositoryException;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.ImmutableSet;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import org.fcrepo.http.api.FedoraExport;
import org.fcrepo.http.api.FedoraFieldSearch;
import org.fcrepo.http.api.FedoraFixity;
import org.fcrepo.http.api.FedoraSitemap;
import org.fcrepo.http.api.FedoraVersions;
import org.fcrepo.http.api.repository.FedoraRepositoryNamespaces;
import org.fcrepo.http.api.repository.FedoraRepositoryTransactions;
import org.fcrepo.http.api.repository.FedoraRepositoryWorkspaces;
import org.fcrepo.http.commons.api.rdf.UriAwareResourceModelFactory;
import org.fcrepo.kernel.FedoraResource;
import org.fcrepo.kernel.rdf.GraphSubjects;
import org.fcrepo.kernel.utils.iterators.RdfStream;
import org.fcrepo.serialization.SerializerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Inject our HTTP API methods into the object graphs
 */
@Component
public class HttpApiResources implements UriAwareResourceModelFactory {

    @Autowired
    protected SerializerUtil serializers;

    @Override
    public RdfStream createModelForResource(final FedoraResource resource,
        final UriInfo uriInfo, final GraphSubjects graphSubjects)
        throws RepositoryException {

        final HttpNodeRdfContext triples = new HttpNodeRdfContext(graphSubjects, resource, uriInfo);

        triples.concatSerializers(serializers);

        return triples;
    }

    class HttpNodeRdfContext extends RdfStream {
        private final Resource subject;
        private FedoraResource resource;
        private UriInfo uriInfo;

        public HttpNodeRdfContext(final GraphSubjects graphSubjects, final FedoraResource resource, final UriInfo uriInfo) throws RepositoryException {
            super();

            this.resource = resource;
            this.uriInfo = uriInfo;
            this.subject = graphSubjects.getGraphSubject(resource.getNode());

            addContentStatements();

            if (resource.getNode().getPrimaryNodeType().isNodeType(ROOT)) {
                addRepositoryStatements();
            } else {
                addNodeStatements();
            }
        }

        private void addContentStatements() throws RepositoryException {
            // fcr:fixity
            final Map<String, String> pathMap =
                singletonMap("path", resource.getPath().substring(1));
            concat(Triple.create(subject.asNode(), HAS_FIXITY_SERVICE.asNode(), NodeFactory.createURI(uriInfo.getBaseUriBuilder().path(FedoraFixity.class).buildFromMap(pathMap).toASCIIString())));
        }

        private void addNodeStatements() throws RepositoryException {

            // fcr:versions
            final Map<String, String> pathMap =
                singletonMap("path", resource.getPath().substring(1));
            concat(Triple.create(subject.asNode(), HAS_VERSION_HISTORY.asNode(), NodeFactory.createURI(uriInfo.getBaseUriBuilder().path(FedoraVersions.class).buildFromMap(pathMap).toASCIIString())));

        }

        private void addRepositoryStatements() {
            final ImmutableSet.Builder<Triple> tripleBuilder = new ImmutableSet.Builder<Triple>();

            tripleBuilder.add(Triple.create(subject.asNode(), HAS_SEARCH_SERVICE.asNode(),NodeFactory.createURI(uriInfo.getBaseUriBuilder().path(FedoraFieldSearch.class).build().toASCIIString())));
            tripleBuilder.add(Triple.create(subject.asNode(), HAS_SITEMAP.asNode(),NodeFactory.createURI(uriInfo.getBaseUriBuilder().path(FedoraSitemap.class).build().toASCIIString())));
            tripleBuilder.add(Triple.create(subject.asNode(), HAS_TRANSACTION_SERVICE.asNode(),NodeFactory.createURI(uriInfo.getBaseUriBuilder().path(FedoraRepositoryTransactions.class).build().toASCIIString())));
            tripleBuilder.add(Triple.create(subject.asNode(), HAS_NAMESPACE_SERVICE.asNode(),NodeFactory.createURI(uriInfo.getBaseUriBuilder().path(FedoraRepositoryNamespaces.class).build().toASCIIString())));
            tripleBuilder.add(Triple.create(subject.asNode(), HAS_WORKSPACE_SERVICE.asNode(),NodeFactory.createURI(uriInfo.getBaseUriBuilder().path(FedoraRepositoryWorkspaces.class).build().toASCIIString())));

            concat(tripleBuilder.build());
        }

        public void concatSerializers(SerializerUtil serializers) throws RepositoryException {

            final ImmutableSet.Builder<Triple> tripleBuilder = new ImmutableSet.Builder<Triple>();

            // fcr:export?format=xyz
            for (final String key : serializers.keySet()) {
                final Map<String, String> pathMap =
                    singletonMap("path", resource.getPath().substring(1));
                final Resource format =
                    createResource(uriInfo.getBaseUriBuilder().path(FedoraExport.class).queryParam("format", key).buildFromMap(pathMap).toASCIIString());
                tripleBuilder.add(Triple.create(subject.asNode(), HAS_SERIALIZATION.asNode(), format.asNode()));
                tripleBuilder.add(Triple.create(format.asNode(), RDFS_LABEL.asNode(), NodeFactory.createLiteral(key)));
            }

            concat(tripleBuilder.build());
        }
    }


}
