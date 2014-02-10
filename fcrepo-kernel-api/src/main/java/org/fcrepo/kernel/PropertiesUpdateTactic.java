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
package org.fcrepo.kernel;

import org.fcrepo.kernel.utils.iterators.RdfStream;

import com.google.common.base.Function;


/**
 * Represents the calculation (but not the persistence) of an update to a resource's properties.
 *
 * @author ajs6f
 * @date Feb 6, 2014
 */
public interface PropertiesUpdateTactic extends Function<RdfStream, RdfStream> {

}
