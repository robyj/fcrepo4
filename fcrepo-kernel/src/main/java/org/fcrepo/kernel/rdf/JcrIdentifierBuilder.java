package org.fcrepo.kernel.rdf;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface JcrIdentifierBuilder extends IdentifierBuilder {
    IdentifierBuilder fromPath(String absPath) throws RepositoryException;

    IdentifierBuilder fromPath(Session session, String absPath) throws RepositoryException;

    IdentifierBuilder fromNode(Node node) throws RepositoryException;

    IdentifierBuilder replacePath(String absPath) throws RepositoryException;

    IdentifierBuilder path(String pathToAppend) throws RepositoryException;

}
