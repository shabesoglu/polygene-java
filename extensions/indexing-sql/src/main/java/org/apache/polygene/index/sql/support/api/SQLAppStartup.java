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
package org.apache.polygene.index.sql.support.api;

import java.sql.SQLException;
import org.apache.polygene.api.activation.ActivatorAdapter;
import org.apache.polygene.api.activation.Activators;
import org.apache.polygene.api.service.ServiceReference;

/**
 * This is the interface which is called by SQL-Indexing when it is activated. This allows implementation specific
 * initializations for RDBMS (for example, possibly re-building database structure).
 */
@Activators( SQLAppStartup.Activator.class )
public interface SQLAppStartup
{

    /**
     * This method is called when connection may be safely initialized - for example, possibly (re-)building database
     * structure.
     * 
     * @throws SQLException If SQL error.
     */
    void initConnection()
        throws SQLException;

    class Activator
        extends ActivatorAdapter<ServiceReference<SQLAppStartup>>
    {

        @Override
        public void afterActivation( ServiceReference<SQLAppStartup> activated )
            throws Exception
        {
            activated.get().initConnection();
        }

    }

}
