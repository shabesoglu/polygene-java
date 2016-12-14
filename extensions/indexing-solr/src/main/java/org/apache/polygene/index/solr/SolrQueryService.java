/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.polygene.index.solr;

import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.activation.Activators;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceComposite;
import org.apache.polygene.api.service.ServiceReference;
import org.apache.polygene.index.solr.internal.SolrEntityIndexerMixin;
import org.apache.polygene.index.solr.internal.SolrEntityQueryMixin;
import org.apache.polygene.spi.entitystore.StateChangeListener;
import org.apache.polygene.spi.query.EntityFinder;

/**
 * JAVADOC
 */
@Mixins( { SolrEntityIndexerMixin.class, SolrEntityQueryMixin.class } )
@Activators( SolrQueryService.Activator.class )
public interface SolrQueryService
        extends EntityFinder, StateChangeListener, SolrSearch, ServiceComposite
{

    void inflateSolrSchema();

    void releaseSolrSchema();

    class Activator
            extends ActivatorAdapter<ServiceReference<SolrQueryService>>
    {

        @Override
        public void afterActivation( ServiceReference<SolrQueryService> activated )
                throws Exception
        {
            activated.get().inflateSolrSchema();
        }

        @Override
        public void beforePassivation( ServiceReference<SolrQueryService> passivating )
                throws Exception
        {
            passivating.get().releaseSolrSchema();
        }

    }

}