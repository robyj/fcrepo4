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
/**
 *
 */
package org.fcrepo.kernel.impl;

import java.util.Iterator;

import org.fcrepo.kernel.PropertiesUpdateTactic;
import org.fcrepo.kernel.utils.iterators.RdfStream;

import com.hp.hpl.jena.graph.Triple;


/**
 * {@link PropertiesUpdateTactic} that simply adds triples/properties.
 *
 * @author ajs6f
 * @date Feb 6, 2014
 */
public class AddUpdateTactic implements PropertiesUpdateTactic {

    private final Iterator<Triple> newTriples;

    /**
     * @param triples
     */
    public AddUpdateTactic(final Iterator<Triple> triples) {
        this.newTriples = triples;
    }

    /* (non-Javadoc)
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    @Override
    public RdfStream apply(final RdfStream input) {
        return input.concat(newTriples);
    }

}
