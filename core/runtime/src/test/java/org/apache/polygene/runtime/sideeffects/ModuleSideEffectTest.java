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

package org.apache.polygene.runtime.sideeffects;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.apache.polygene.api.composite.TransientComposite;
import org.apache.polygene.api.mixin.Mixins;
import org.apache.polygene.api.sideeffect.GenericSideEffect;
import org.apache.polygene.bootstrap.AssemblyException;
import org.apache.polygene.bootstrap.ModuleAssembly;
import org.apache.polygene.test.AbstractPolygeneTest;

/**
 * Test of declaring sideeffect in assembly
 */
public class ModuleSideEffectTest
    extends AbstractPolygeneTest
{
    public static boolean ok = false;

    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.transients( FooComposite.class ).withSideEffects( TraceSideEffect.class );
    }

    @Test
    public void testModuleSideEffect()
    {
        transientBuilderFactory.newTransient( Foo.class ).test( "Foo", 42 );
        Assert.assertThat( "SideEffect has been called", ok, CoreMatchers.equalTo( true ) );
    }

    @Mixins( FooMixin.class )
    public interface FooComposite
        extends TransientComposite, Foo
    {
    }

    public interface Foo
    {
        String test( String foo, int bar );
    }

    public static class FooMixin
        implements Foo
    {
        public String test( String foo, int bar )
        {
            return foo + " " + bar;
        }
    }

    public static class TraceSideEffect
        extends GenericSideEffect
    {
        public Object invoke( Object proxy, Method method, Object[] args )
            throws Throwable
        {
            ok = true;
            Object result = this.result.invoke( proxy, method, args );
            String str = method.getName() + Arrays.asList( args );
            System.out.println( str );
            return result;
        }
    }
}