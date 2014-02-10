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

package org.fcrepo.kernel.utils.iterators;

import static com.google.common.collect.ImmutableSet.of;
import static com.google.common.collect.Sets.newHashSet;
import static com.hp.hpl.jena.graph.NodeFactory.createAnon;
import static com.hp.hpl.jena.graph.NodeFactory.createURI;
import static com.hp.hpl.jena.graph.Triple.create;
import static com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel;
import static com.hp.hpl.jena.vocabulary.RDF.type;
import static org.fcrepo.kernel.RdfLexicon.JCR_NAMESPACE;
import static org.fcrepo.kernel.RdfLexicon.PAGE;
import static org.fcrepo.kernel.RdfLexicon.REPOSITORY_NAMESPACE;
import static org.fcrepo.kernel.RdfLexicon.RESTAPI_NAMESPACE;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.fcrepo.kernel.rdf.GraphSubjects;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

public class PersistingRdfStreamConsumerTest {

    @Test
    public void testGoodRdf() throws RepositoryException {
        consume(of(mixinTriple));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testManagedRdf() throws RepositoryException {
        consume(of(managedMixinTriple));
        fail("Should not succeed at persisting a managed triple!");
    }

    public void consume(final Set<Triple> testStream) throws RepositoryException {

        logger.debug("Trying to persist: {}", testStream);

        final Set<Statement> acceptedStatements = newHashSet();

        final Set<Resource> acceptedMixins = newHashSet();

        testPersister = new PersistingRdfStreamConsumer(mockGraphSubjects, mockSession, new RdfStream(testStream)) {

            @Override
            protected void operateOnProperty(final Statement s, final Node subjectNode) throws RepositoryException {
                acceptedStatements.add(s);
            }

            @Override
            protected void
                    operateOnMixin(final Resource mixinResource, final Node subjectNode) throws RepositoryException {
                acceptedMixins.add(mixinResource);
            }
        };

        testPersister.consume();

        for (final Triple t : testStream) {
            assertTrue("Failed to operate on appropriate property!", acceptedStatements.contains(m.asStatement(t)));
        }

    }

    @Test(expected = ExecutionException.class)
    public void testBadStream() throws Exception {
        when(mockTriples.hasNext()).thenThrow(new RuntimeException("Expected."));
        testPersister = new PersistingRdfStreamConsumer(mockGraphSubjects, mockSession, new RdfStream(mockTriples)) {

            @Override
            protected void operateOnProperty(final Statement s, final Node subjectNode) throws RepositoryException {
            }

            @Override
            protected void
                    operateOnMixin(final Resource mixinResource, final Node subjectNode) throws RepositoryException {
            }
        };
        // this should blow out when we try to retrieve the result
        testPersister.consumeAsync().get();
    }

    @Before
    public void setUp() throws RepositoryException {
        initMocks(this);

        for (final Statement fedoraStatement : fedoraStatements) {
            when(mockGraphSubjects.getNodeFromGraphSubject(fedoraStatement.getSubject())).thenReturn(mockNode);
            when(mockGraphSubjects.isFedoraGraphSubject(fedoraStatement.getSubject())).thenReturn(true);
        }
        when(mockGraphSubjects.getNodeFromGraphSubject(foreignStatement.getSubject())).thenReturn(mockNode);
        when(mockGraphSubjects.isFedoraGraphSubject(foreignStatement.getSubject())).thenReturn(false);
    }

    private static final Logger logger = getLogger(PersistingRdfStreamConsumerTest.class);

    private static final Model m = createDefaultModel();

    private static final Triple propertyTriple = create(createAnon(), createAnon(), createAnon());

    private static final Statement propertyStatement = m.asStatement(propertyTriple);

    private static final Triple ldpManagedPropertyTriple = create(createAnon(), PAGE.asNode(), createAnon());

    private static final Statement ldpManagedPropertyStatement = m.asStatement(ldpManagedPropertyTriple);

    private static final Triple fedoraManagedPropertyTriple = create(createAnon(), createURI(REPOSITORY_NAMESPACE
            + "thing"), createAnon());

    private static final Statement fedoraManagedPropertyStatement = m.asStatement(fedoraManagedPropertyTriple);

    private static final Statement jcrManagedPropertyStatement = ResourceFactory
            .createStatement(ResourceFactory.createResource(), ResourceFactory.createProperty(JCR_NAMESPACE, "thing"),
                    ResourceFactory.createResource());

    private static final Triple managedMixinTriple = create(createAnon(), type.asNode(), createURI(RESTAPI_NAMESPACE
            + "mixin"));

    private static final Statement managedMixinStatement = m.asStatement(managedMixinTriple);

    private static final Triple mixinTriple = create(createAnon(), type.asNode(), createURI("myNS:mymixin"));

    private static final Statement mixinStatement = m.asStatement(mixinTriple);

    private static final Triple foreignTriple = create(createAnon(), createAnon(), createAnon());

    private static final Statement foreignStatement = m.asStatement(foreignTriple);

    private static final Statement[] fedoraStatements = new Statement[] {propertyStatement,
            ldpManagedPropertyStatement, mixinStatement, managedMixinStatement, jcrManagedPropertyStatement,
            fedoraManagedPropertyStatement};

    @Mock
    private Session mockSession;

    @Mock
    private Node mockNode;

    @Mock
    private GraphSubjects mockGraphSubjects;

    @Mock
    private Iterator<Triple> mockTriples;

    private PersistingRdfStreamConsumer testPersister;

}
