package org.fcrepo.kernel.rdf;

import com.hp.hpl.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;
import static org.fcrepo.jcr.FedoraJcrTypes.FCR_CONTENT;
import static org.fcrepo.kernel.RdfLexicon.RESTAPI_NAMESPACE;
import static org.modeshape.jcr.api.JcrConstants.JCR_CONTENT;

public class DefaultIdentifierBuilder implements IdentifierBuilder {

    @Autowired
    private Repository repo;

    private Session session;
    private String path;

    private String baseUri;

    public DefaultIdentifierBuilder() {
        this.baseUri = RESTAPI_NAMESPACE;
    }

    public DefaultIdentifierBuilder(final Repository repo) {
        this(repo, RESTAPI_NAMESPACE);
    }

    public DefaultIdentifierBuilder(final Repository repo, final String baseResourceUri ) {
        this.repo = repo;
        this.baseUri = baseResourceUri;
    }

    @Override
    public DefaultIdentifierBuilder newInstance(final Session session, final String path) {
        final DefaultIdentifierBuilder defaultIdentifierBuilder = new DefaultIdentifierBuilder(repo);

        defaultIdentifierBuilder.setSession(session);
        defaultIdentifierBuilder.setPath(path);

        return defaultIdentifierBuilder;
    }

    @Override
    public IdentifierBuilder fromPath(final String absPath) throws RepositoryException {
        return fromPath(repo.login(), absPath);
    }

    @Override
    public IdentifierBuilder fromPath(final Session session, final String absPath) throws RepositoryException {
        return newInstance(session, absPath);
    }

    @Override
    public IdentifierBuilder fromResource(final Resource subject) throws RepositoryException {
        return fromResource(repo.login(), subject);
    }

    @Override
    public IdentifierBuilder fromResource(final Session session, final Resource subject) throws RepositoryException {

        if (!isFedoraGraphSubject(subject)) {
            return null;
        }

        final String absPath = subject.getURI()
                                   .substring(getBaseUri().length());

        return newInstance(session, absPath);
    }

    @Override
    public IdentifierBuilder fromNode(final Node node) throws RepositoryException {
        return newInstance(node.getSession(), node.getPath());
    }

    @Override
    public IdentifierBuilder replacePath(final String absPath) throws RepositoryException {
        return fromPath(session, absPath);
    }

    @Override
    public IdentifierBuilder path(final String pathToAppend) throws RepositoryException {
        return fromPath(session, path + "/" + pathToAppend);
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Node getNode() throws RepositoryException {
        return session.getNode(getPath());
    }

    @Override
    public String getBaseUri() {
        return baseUri;
    }

    @Override
    public Resource getResource() {
        final String absPath = getPath();

        if (absPath.endsWith(JCR_CONTENT)) {
            return createResource(getBaseUri() + absPath.replace(JCR_CONTENT, FCR_CONTENT));
        } else {
            return createResource(getBaseUri()  + absPath);
        }
    }

    public boolean isFedoraGraphSubject(final Resource subject) {
        checkNotNull(subject, "null cannot be a Fedora object!");
        return subject.isURIResource() &&
                   subject.getURI().startsWith(RESTAPI_NAMESPACE);
    }


    protected void setSession(final Session session) {
        this.session = session;

    }

    protected void setPath(final String absPath) {
        this.path = absPath;
    }
}
