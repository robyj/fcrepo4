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

import static com.google.common.collect.ImmutableBiMap.of;
import static com.hp.hpl.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Map;

import javax.jcr.RepositoryException;
import javax.ws.rs.core.UriInfo;

import org.fcrepo.kernel.FedoraResource;
import org.fcrepo.kernel.rdf.GraphSubjects;
import org.fcrepo.kernel.utils.iterators.RdfStream;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

public class HttpTripleUtilTest {

    private HttpTripleUtil testObj;

    private Dataset dataset;

    @Mock
    private UriInfo mockUriInfo;

    @Mock
    private GraphSubjects mockSubjects;

    @Mock
    private FedoraHttpRdfTripleProvider mockBean1;

    @Mock
    private FedoraHttpRdfTripleProvider mockBean2;

    @Mock
    private ApplicationContext mockContext;

    @Mock
    private FedoraResource mockResource;

    @Before
    public void setUp() {
        initMocks(this);
        testObj = new HttpTripleUtil();
        testObj.setApplicationContext(mockContext);
        dataset = DatasetFactory.create(createDefaultModel());
    }

    @Test
    @Ignore
    public void shouldAddTriplesFromRegisteredBeans()
            throws RepositoryException {
        final Map<String, FedoraHttpRdfTripleProvider> mockBeans =
                of("doesnt", mockBean1, "matter", mockBean2);
        when(mockContext.getBeansOfType(FedoraHttpRdfTripleProvider.class))
                .thenReturn(mockBeans);
        when(mockBean1.getRdfStream(mockSubjects, mockResource, mockUriInfo))
                .thenReturn(new RdfStream());
        when(mockBean2.getRdfStream(mockSubjects, mockResource, mockUriInfo))
                .thenReturn(new RdfStream());

        testObj.addHttpComponentModelsForResource(dataset, mockResource,
                mockUriInfo, mockSubjects);
        verify(mockBean1).getRdfStream(mockSubjects, mockResource, mockUriInfo);
        verify(mockBean2).getRdfStream(mockSubjects, mockResource, mockUriInfo);
    }
}
