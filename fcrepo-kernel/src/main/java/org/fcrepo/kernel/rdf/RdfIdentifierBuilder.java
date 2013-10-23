package org.fcrepo.kernel.rdf;

import com.hp.hpl.jena.rdf.model.Resource;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

public interface RdfIdentifierBuilder extends IdentifierBuilder {

    IdentifierBuilder fromResource(Resource subject) throws RepositoryException;

    IdentifierBuilder fromResource(Session session, Resource subject) throws RepositoryException;

    Resource getResource();

    String getBaseUri();
}
