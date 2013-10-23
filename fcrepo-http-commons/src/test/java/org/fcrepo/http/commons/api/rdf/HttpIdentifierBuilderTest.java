package org.fcrepo.http.commons.api.rdf;

import com.hp.hpl.jena.rdf.model.Resource;
import com.sun.jersey.api.uri.UriBuilderImpl;
import org.fcrepo.kernel.Transaction;
import org.fcrepo.kernel.rdf.IdentifierBuilder;
import org.fcrepo.kernel.services.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import java.net.URI;

import static org.fcrepo.http.commons.test.util.TestHelpers.setField;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class HttpIdentifierBuilderTest {
    private HttpIdentifierBuilder testObj;

    private String testPath = "/foo/bar";

    @Mock
    private Session mockSession;

    @Mock
    private Workspace mockWorkspace;

    @Mock private Resource mockSubject;

    @Mock
    private Node mockNode;

    @Mock
    private ValueFactory mockValueFactory;

    @Mock
    private HttpServletRequest mockServletRequest;

    @Mock
    private TransactionService mockTxService;

    @Mock
    private Transaction mockTx;

    @Mock
    private Repository mockRepo;

    private UriInfo uriInfo;

    @Before
    public void setUp() throws RepositoryException, NoSuchFieldException {
        initMocks(this);
        uriInfo = getUriInfoImpl(testPath);
        when(mockSession.getValueFactory()).thenReturn(mockValueFactory);
        testObj =
            new HttpIdentifierBuilder(MockNodeController.class,
                                     uriInfo);

        setField(testObj, "repo", mockRepo);
        setField(testObj, "transactionService", mockTxService);
        when(mockRepo.login()).thenReturn(mockSession);
    }

    @Test
    public void testFromServletRequest() throws RepositoryException {
        final HttpIdentifierBuilder identifierBuilder = testObj.fromServletRequest(mockServletRequest);

        assertEquals("http://localhost:8080/fcrepo/rest/", identifierBuilder.getResource().getURI());
    }

    @Test
    public void testFromServletRequestAndPath() throws RepositoryException {
        final HttpIdentifierBuilder baseIdentifierBuilder = testObj.fromServletRequest(mockServletRequest);

        final IdentifierBuilder identifierBuilder = baseIdentifierBuilder.replacePath("/some/path/to/node");

        assertEquals("http://localhost:8080/fcrepo/rest/some/path/to/node", identifierBuilder.getResource().getURI());
    }

    @Test
    public void testFromServletRequestInTransaction() throws RepositoryException {

        when(mockServletRequest.getPathInfo()).thenReturn("/tx:123/some/path");

        final Session mockTxSession = mock(Session.class);
        when(mockTxSession.getNamespaceURI("fcrepo4.tx.id")).thenReturn("123");
        when(mockTx.getSession()).thenReturn(mockTxSession);
        when(mockTxService.getTransaction("123")).thenReturn(mockTx);

        final HttpIdentifierBuilder baseIdentifierBuilder = testObj.fromServletRequest(mockServletRequest);

        final IdentifierBuilder identifierBuilder = baseIdentifierBuilder.replacePath("/some/path/to/node");

        assertEquals("http://localhost:8080/fcrepo/rest/tx:123/some/path/to/node", identifierBuilder.getResource().getURI());

    }

    private static UriInfo getUriInfoImpl(final String path) {
        // UriInfo ui = mock(UriInfo.class,withSettings().verboseLogging());
        final UriInfo ui = mock(UriInfo.class);
        final UriBuilder ub = new UriBuilderImpl();
        ub.scheme("http");
        ub.host("localhost");
        ub.port(8080);
        ub.path("/fcrepo");

        final UriBuilder rb = new UriBuilderImpl();
        rb.scheme("http");
        rb.host("localhost");
        rb.port(8080);
        rb.path("/fcrepo/rest" + path);

        when(ui.getRequestUri()).thenReturn(
                                               URI.create("http://localhost:8080/fcrepo/rest" + path));
        when(ui.getBaseUri()).thenReturn(
                                            URI.create("http://localhost:8080/fcrepo"));
        when(ui.getBaseUriBuilder()).thenReturn(ub);
        when(ui.getAbsolutePathBuilder()).thenReturn(rb);

        return ui;
    }

    @Path("/rest/{path}")
    private class MockNodeController {

    }
}
