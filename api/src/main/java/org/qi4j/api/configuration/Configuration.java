/*
 * Copyright (c) 2008, Rickard �berg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.api.configuration;

import org.qi4j.api.Qi4j;
import org.qi4j.api.concern.ConcernOf;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * Provide Configurations for Services. A Service that wants to be configurable
 * should inject a reference to Configuration with the Configuration type:
 * <code><pre>
 * &#64;This Configuration&#60;MyServiceConfiguration&#62; config;
 * </pre></code>
 * where MyServiceConfiguration extends EntityComposite. The Configuration implementation
 * will either locate an instance of the given Configuration type in the
 * persistent store using the identity of the Service, or create a new such instance
 * if one doesn't already exist.
 * <p/>
 * If a new Configuration instance is created then it will be populated with properties
 * from the properties file whose filesystem name is the same as the identity (e.g. "MyService.properties").
 * <p/>
 * The Configuration instance can be modified externally just like any other EntityComposite, but
 * its values will not be updated in the Service until Configuration.refresh is called. This allows
 * safe reloads of Configuration state to ensure that it is not reloaded while the Service is handling
 * a request.
 * <p/>
 * The Configuration will be automatically refreshed when the Service is activated through the Activatable.activate()
 * method by the Qi4j runtime. Any refreshes at other points will have to be done manually or triggered through some other
 * mechanism.
 */
@Mixins( Configuration.ConfigurationMixin.class )
public interface Configuration<T>
{
    T configuration();

    void refresh();

    // Implementation of Configuration
    @Concerns( Configuration.ConfigurationActivationRefreshConcern.class )
    public final class ConfigurationMixin<T>
        implements Configuration<T>
    {
        private T configuration;
        private UnitOfWork uow;

        public ConfigurationMixin( @Structure Qi4j api,
                                   @This ServiceComposite me,
                                   @Structure UnitOfWorkFactory uowf,
                                   @Uses ServiceDescriptor descriptor )
            throws Exception
        {
            uow = uowf.newUnitOfWork();
            Class<? extends T> configType = (Class<? extends T>) api.getConfigurationType( me );
            if( configType == null )
            {
                throw new InstantiationException( "Could not figure out type of Configuration" );
            }
            configuration = api.getConfigurationInstance( configType, me, uow );
        }

        public T configuration()
        {
            return configuration;
        }

        public void refresh()
        {
            if( uow != null )
            {
                uow.refresh( configuration );
            }
        }
    }

    // Refresh the Configuration when the Service is activated
    public abstract class ConfigurationActivationRefreshConcern
        extends ConcernOf<Activatable>
        implements Activatable
    {
        @This Configuration<Object> configuration;

        public void activate() throws Exception
        {
            configuration.refresh();
            next.activate();
        }
    }
}
