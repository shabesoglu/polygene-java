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

package org.apache.polygene.library.rdf.repository;

import java.io.File;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.rdbms.RdbmsStore;
import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.activation.Activators;
import org.apache.polygene.api.configuration.Configuration;
import org.apache.polygene.api.injection.scope.This;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.service.ServiceReference;

@Mixins( RdbmsRepositoryService.RdbmsRepositoryMixin.class )
@Activators( RdbmsRepositoryService.Activator.class )
public interface RdbmsRepositoryService extends Repository
{

    @Override
    void initialize()
            throws RepositoryException;

    @Override
    void shutDown()
            throws RepositoryException;

    class Activator
            extends ActivatorAdapter<ServiceReference<RdbmsRepositoryService>>
    {

        @Override
        public void afterActivation( ServiceReference<RdbmsRepositoryService> activated )
                throws Exception
        {
            activated.get().initialize();
        }

        @Override
        public void beforePassivation( ServiceReference<RdbmsRepositoryService> passivating )
                throws Exception
        {
            passivating.get().shutDown();
        }

    }

    abstract class RdbmsRepositoryMixin
        implements RdbmsRepositoryService
    {
        @This
        private Configuration<RdbmsRepositoryConfiguration> configuration;

        private SailRepository repo;

        @Override
        public void setDataDir( File file )
        {
            repo.setDataDir( file );
        }

        @Override
        public File getDataDir()
        {
            return repo.getDataDir();
        }

        @Override
        public void initialize()
            throws RepositoryException
        {
            RdbmsRepositoryConfiguration conf = configuration.get();
            String jdbcDriver = conf.jdbcDriver().get();
            String jdbcUrl = conf.jdbcUrl().get();
            String user = conf.user().get();
            String password = conf.password().get();
            repo = new SailRepository( new RdbmsStore( jdbcDriver, jdbcUrl, user, password ) );
            repo.initialize();
        }

        @Override
        public boolean isInitialized()
        {
            return repo.isInitialized();
        }
        
        @Override
        public void shutDown()
            throws RepositoryException
        {
            repo.shutDown();
        }

        @Override
        public boolean isWritable()
            throws RepositoryException
        {
            return repo.isWritable();
        }

        @Override
        public RepositoryConnection getConnection()
            throws RepositoryException
        {
            return repo.getConnection();
        }

        @Override
        public ValueFactory getValueFactory()
        {
            return repo.getValueFactory();
        }
    }
}
