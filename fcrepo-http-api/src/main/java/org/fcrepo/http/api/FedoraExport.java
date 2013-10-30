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

package org.fcrepo.http.api;

import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.Response.ok;
import static org.fcrepo.kernel.RdfLexicon.HAS_SERIALIZATION;
import static org.fcrepo.kernel.RdfLexicon.RDFS_LABEL;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import com.google.common.collect.ImmutableSet;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;
import org.fcrepo.http.commons.AbstractResource;
import org.fcrepo.http.commons.api.rdf.FedoraHttpRdfTripleProvider;
import org.fcrepo.http.commons.session.InjectedSession;
import org.fcrepo.kernel.FedoraResource;
import org.fcrepo.kernel.rdf.GraphSubjects;
import org.fcrepo.kernel.utils.iterators.RdfStream;
import org.fcrepo.serialization.FedoraObjectSerializer;
import org.fcrepo.serialization.SerializerUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Serialization for nodes
 */
@Component
@Scope("prototype")
@Path("/{path: .*}/fcr:export")
public class FedoraExport extends AbstractResource implements FedoraHttpRdfTripleProvider {

    @Autowired
    protected SerializerUtil serializers;

    @InjectedSession
    protected Session session;

    private final Logger logger = getLogger(this.getClass());

    /**
     * Export an object with the given format, e.g.: GET
     * /path/to/object/fcr:export?format=jcr/xml -> the node as JCR/XML
     *
     * @param pathList
     * @param format
     * @return
     */
    @GET
    public Response exportObject(
        @PathParam("path") final List<PathSegment> pathList,
        @QueryParam("format") @DefaultValue("jcr/xml") final String format) {

        final String path = toPath(pathList);

        logger.debug("Requested object serialization for: " + path
                + " using serialization format " + format);

        final FedoraObjectSerializer serializer =
            serializers.getSerializer(format);

        return ok().type(serializer.getMediaType()).entity(
                new StreamingOutput() {

                    @Override
                    public void write(final OutputStream out)
                        throws IOException {

                        try {
                            logger.debug("Selecting from serializer map: "
                                    + serializers);
                            logger.debug("Retrieved serializer for format: "
                                    + format);
                            serializer.serialize(objectService.getObject(
                                    session, path), out);
                            logger.debug("Successfully serialized object: "
                                    + path);
                        } catch (final RepositoryException e) {
                            throw new WebApplicationException(e);
                        } finally {
                            session.logout();
                        }
                    }
                }).build();

    }

    @Override
    public RdfStream getRdfStream(final GraphSubjects graphSubjects, final FedoraResource resource, final UriInfo uriInfo) throws RepositoryException {

        final ImmutableSet.Builder<Triple> tripleBuilder = new ImmutableSet.Builder<Triple>();

        final Node node = graphSubjects.getGraphSubject(resource.getNode()).asNode();

        // fcr:export?format=xyz
        for (final String key : serializers.keySet()) {
            final Map<String, String> pathMap =
                singletonMap("path", resource.getPath().substring(1));
            final Resource format =
                createResource(uriInfo.getBaseUriBuilder().path(FedoraExport.class).queryParam("format", key).buildFromMap(pathMap).toASCIIString());
            tripleBuilder.add(Triple.create(node, HAS_SERIALIZATION.asNode(), format.asNode()));
            tripleBuilder.add(Triple.create(format.asNode(), RDFS_LABEL.asNode(), NodeFactory.createLiteral(key)));
        }

        return new RdfStream(tripleBuilder.build());
    }
}
