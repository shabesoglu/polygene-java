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
package org.apache.polygene.valueserialization.stax;

import java.util.function.Function;
import org.apache.polygene.api.structure.Application;
import org.apache.polygene.api.structure.Module;
import org.apache.polygene.api.value.ValueSerialization;
import org.apache.polygene.bootstrap.Assemblers;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;

/**
 * Assemble a ValueSerialization Service producing and consuming XML documents.
 */
public class StaxValueSerializationAssembler
    extends Assemblers.Visibility<StaxValueSerializationAssembler>
{
    private Function<Application, Module> valuesModuleFinder;

    public StaxValueSerializationAssembler withValuesModuleFinder( Function<Application, Module> valuesModuleFinder )
    {
        this.valuesModuleFinder = valuesModuleFinder;
        return this;
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        if( valuesModuleFinder == null )
        {
            module.services( StaxValueSerializationService.class ).
                visibleIn( visibility() ).
                taggedWith( ValueSerialization.Formats.XML );
        }
        else
        {
            module.services( StaxValueSerializationService.class ).
                visibleIn( visibility() ).
                taggedWith( ValueSerialization.Formats.XML ).
                setMetaInfo( valuesModuleFinder );
        }
    }
}