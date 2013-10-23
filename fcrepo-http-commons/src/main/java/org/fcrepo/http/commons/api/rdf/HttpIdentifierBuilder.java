package org.fcrepo.http.commons.api.rdf;

import org.fcrepo.kernel.Transaction;
import org.fcrepo.kernel.exception.TransactionMissingException;
import org.fcrepo.kernel.rdf.DefaultIdentifierBuilder;
import org.fcrepo.kernel.rdf.IdentifierBuilder;
import org.fcrepo.kernel.services.TransactionService;
import org.modeshape.jcr.api.ServletCredentials;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import static org.fcrepo.kernel.services.TransactionService.getCurrentTransactionId;
import static org.slf4j.LoggerFactory.getLogger;

public class HttpIdentifierBuilder extends DefaultIdentifierBuilder {


    private static final Logger logger = getLogger(HttpIdentifierBuilder.class);

    private final String basePath;

    @Autowired
    private Repository repo;

    @Autowired
    private TransactionService transactionService;

    public HttpIdentifierBuilder(final Class<?> relativeTo, final UriInfo uris) {

        UriBuilder nodesBuilder = uris.getBaseUriBuilder().path(relativeTo);
        String basePath = nodesBuilder.build("").toString();
        if (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        this.basePath = basePath;

    }

    public HttpIdentifierBuilder(final String basePath) {
        this.basePath = basePath;
    }

    @Override
    public HttpIdentifierBuilder newInstance(final Session session, final String path) {
        final HttpIdentifierBuilder defaultIdentifierBuilder = new HttpIdentifierBuilder(basePath);

        defaultIdentifierBuilder.setSession(session);
        defaultIdentifierBuilder.setPath(path);

        return defaultIdentifierBuilder;

    }

    public HttpIdentifierBuilder fromServletRequest(final HttpServletRequest servletRequest) throws RepositoryException {

        final ServletCredentials creds =
            new ServletCredentials(servletRequest);

        final String workspace = getEmbeddedWorkspace(servletRequest);
        final Transaction transaction =
            getEmbeddedTransaction(servletRequest);

        final Session session;

        if (transaction != null) {

            final HttpSession httpSession =
                servletRequest.getSession(true);

            if (httpSession != null && transaction.getId().equals(httpSession.getAttribute("currentTx"))) {
                session = transaction.getSession().impersonate(creds);
            } else {
                session = transaction.getSession();
            }
        } else if (workspace != null) {
            session = repo.login(creds, workspace);
        } else {
            session = repo.login();
        }

        return newInstance(session, "/");

    }

    @Override
    public String getBaseUri() {

        String path = basePath;
        final Session session = getSession();

            final Workspace workspace = session.getWorkspace();

            final String txId = getCurrentTransactionId(session);

            if (txId != null) {
                path += "/tx:" + txId;
            } else if (workspace != null &&
                           !workspace.getName().equals("default")) {
                path += "/workspace:" + workspace.getName();
            } else {
                path += "";
            }

        return path;
    }

    /**
     * Extract the workspace id embedded at the beginning of a request
     *
     * @param request
     * @return
     */
    private String getEmbeddedWorkspace(final HttpServletRequest request) {
        final String requestPath = request.getPathInfo();

        if (requestPath == null) {
            return null;
        }

        final String[] part = requestPath.split("/");

        if (part.length > 1 && part[1].startsWith("workspace:")) {
            return part[1].substring("workspace:".length());
        } else {
            return null;
        }

    }

    /**
     * Extract the transaction id embedded at the beginning of a request
     *
     * @param servletRequest
     * @return
     * @throws org.fcrepo.kernel.exception.TransactionMissingException
     */
    private Transaction getEmbeddedTransaction(
                                                  final HttpServletRequest servletRequest)
        throws TransactionMissingException {
        final String requestPath = servletRequest.getPathInfo();

        if (requestPath == null) {
            return null;
        }

        final String[] part = requestPath.split("/");

        if (part.length > 1 && part[1].startsWith("tx:")) {
            final String txid = part[1].substring("tx:".length());
            return transactionService.getTransaction(txid);
        } else {
            return null;
        }
    }
}
