package org.fcrepo.kernel.rdf;

import com.hp.hpl.jena.rdf.model.Resource;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface IdentifierBuilder {

    Session getSession();

    String getPath();

    Node getNode() throws RepositoryException;

    IdentifierBuilder newInstance(Session session, String path);

    IdentifierBuilder fromPath(String absPath) throws RepositoryException;

    IdentifierBuilder fromPath(Session session, String absPath) throws RepositoryException;

    IdentifierBuilder fromNode(Node node) throws RepositoryException;

    IdentifierBuilder replacePath(String absPath) throws RepositoryException;

    IdentifierBuilder path(String pathToAppend) throws RepositoryException;


    IdentifierBuilder fromResource(Resource subject) throws RepositoryException;

    IdentifierBuilder fromResource(Session session, Resource subject) throws RepositoryException;

    Resource getResource();

    String getBaseUri();
}
