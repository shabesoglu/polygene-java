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

package org.apache.polygene.index.rdf.assembly;

import org.apache.polygene.api.common.Visibility;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.index.rdf.RdfIndexingService;
import org.apache.polygene.index.rdf.query.RdfQueryParserFactory;
import org.apache.polygene.library.rdf.entity.EntityStateSerializer;
import org.apache.polygene.library.rdf.entity.EntityTypeSerializer;
import org.apache.polygene.library.rdf.repository.MemoryRepositoryService;

public class RdfMemoryStoreAssembler extends AbstractRdfIndexingAssembler<RdfNativeSesameStoreAssembler>
{
    private Visibility repositoryVisibility;

    public RdfMemoryStoreAssembler()
    {
        this(Visibility.application, Visibility.module);
    }

    public RdfMemoryStoreAssembler( Visibility indexingVisibility, Visibility repositoryVisibility )
    {
        this.repositoryVisibility = repositoryVisibility;
        visibleIn( indexingVisibility );
    }

    @Override
    public void assemble( ModuleAssembly module )
    {
        module.services( MemoryRepositoryService.class )
              .visibleIn( repositoryVisibility )
              .instantiateOnStartup();
        module.services( RdfIndexingService.class )
              .taggedWith( "rdf", "query", "indexing" )
              .visibleIn( visibility() )
              .instantiateOnStartup();
        module.services( RdfQueryParserFactory.class ).visibleIn( visibility() );
        module.objects( EntityStateSerializer.class, EntityTypeSerializer.class );
    }
}
