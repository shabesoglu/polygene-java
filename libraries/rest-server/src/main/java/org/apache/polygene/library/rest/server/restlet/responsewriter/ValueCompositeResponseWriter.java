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

package org.apache.polygene.library.rest.server.restlet.responsewriter;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.polygene.api.injection.scope.Service;
import org.apache.polygene.api.serialization.Serializer;
import org.apache.polygene.api.value.ValueComposite;
import org.apache.polygene.spi.serialization.JsonSerializer;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;

/**
 * JAVADOC
 */
public class ValueCompositeResponseWriter extends AbstractResponseWriter
{
    private static final List<MediaType> supportedMediaTypes = Arrays.asList( MediaType.TEXT_HTML, MediaType.APPLICATION_JSON );

    @Service
    private Configuration cfg;

    @Service
    private JsonSerializer serializer;

    @Override
    public boolean writeResponse( final Object result, final Response response )
        throws ResourceException
    {
        if( result instanceof ValueComposite )
        {
            MediaType type = getVariant( response.getRequest(), ENGLISH, supportedMediaTypes ).getMediaType();
            if( MediaType.APPLICATION_JSON.equals( type ) )
            {
                StringRepresentation representation = new StringRepresentation( serializer.serialize( Serializer.Options.ALL_TYPE_INFO, result ),
                                                                                MediaType.APPLICATION_JSON );
                response.setEntity( representation );
                return true;
            }
            else if( MediaType.TEXT_HTML.equals( type ) )
            {
                Representation rep = new WriterRepresentation( MediaType.TEXT_HTML )
                {
                    @Override
                    public void write( Writer writer )
                        throws IOException
                    {
                        // Look for type specific template
                        Template template;
                        try
                        {
                            template = cfg.getTemplate( "/rest/template/" + result.getClass()
                                .getInterfaces()[ 0 ].getSimpleName() + ".htm" );
                        }
                        catch( Exception e )
                        {
                            // Use default
                            template = cfg.getTemplate( "value.htm" );
                        }

                        Map<String, Object> context = new HashMap<String, Object>();
                        context.put( "request", response.getRequest() );
                        context.put( "response", response );
                        context.put( "result", result );
                        context.put( "util", this );
                        try
                        {
                            template.process( context, writer );
                        }
                        catch( TemplateException e )
                        {
                            throw new IOException( e );
                        }
                    }

                    public boolean isSequence( Object obj )
                    {
                        return obj instanceof Collection;
                    }
                };
                response.setEntity( rep );
                return true;
            }
        }
        return false;
    }
}
