/*
 * Copyright (c) 2007, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.runtime.property;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.List;
import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.RDF;
import org.qi4j.api.property.Computed;
import org.qi4j.api.property.ComputedPropertyInstance;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.Property;
import org.qi4j.api.property.PropertyInfo;
import org.qi4j.runtime.composite.ConstraintsCheck;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.value.ValueState;

/**
 * TODO
 */
public abstract class AbstractPropertyModel
    implements Serializable, PropertyDescriptor, ConstraintsCheck
{
    private static final long serialVersionUID = 1L;

    private final String name;
    private final Type type;
    private final Method accessor; // Interface accessor
    private final String qualifiedName;
    private final String uri;
    private final String rdf;

    private final ValueConstraintsInstance constraints; // May be null
    protected final MetaInfo metaInfo;
    private final Object initialValue;
    private final boolean useDefaults;
    private final boolean immutable;
    private final boolean computed;
    private final boolean needsWrapper;

    private PropertyInfo builderInfo;

    public AbstractPropertyModel(
        Method anAccessor, boolean immutable, ValueConstraintsInstance constraints, MetaInfo aMetaInfo, Object anInitialValue )
    {
        this.immutable = immutable;
        metaInfo = aMetaInfo;
        name = anAccessor.getName();
        type = GenericPropertyInfo.getPropertyType( anAccessor );
        accessor = anAccessor;
        qualifiedName = GenericPropertyInfo.getQualifiedName( anAccessor );

        // Check for @UseDefaults annotation
        if (anInitialValue == null)
        {
            if (metaInfo.get( UseDefaults.class ) != null)
                anInitialValue = DefaultValues.getDefaultValue( type );
        }

        initialValue = anInitialValue;
        useDefaults = metaInfo.get( UseDefaults.class ) != null;

        uri = GenericPropertyInfo.toURI( qualifiedName() );
        RDF uriAnnotation = metaInfo.get( RDF.class );
        rdf = uriAnnotation == null ? null : uriAnnotation.value();

        this.constraints = constraints;

        computed = metaInfo.get( Computed.class ) != null;
        needsWrapper = !accessor.getReturnType().equals(Property.class);

        builderInfo = new GenericPropertyInfo( metaInfo, false, computed, name, qualifiedName, type );
    }

    public <T> T metaInfo( Class<T> infoType )
    {
        return metaInfo.get( infoType );
    }

    public String name()
    {
        return name;
    }

    public String qualifiedName()
    {
        return qualifiedName;
    }

    public Type type()
    {
        return type;
    }

    public Method accessor()
    {
        return accessor;
    }

    public boolean isImmutable()
    {
        return immutable;
    }

    public boolean isComputed()
    {
        return computed;
    }

    public Object initialValue()
    {
        Object value = initialValue;

        // Check for @UseDefaults annotation
        if (value == null && useDefaults)
        {
            value = DefaultValues.getDefaultValue( type );
        }

        return value;
    }

    public String toURI()
    {
        return uri;
    }

    public String toRDF()
    {
        return rdf;
    }

    public Property<?> newBuilderInstance()
    {
        // Properties cannot be immutable during construction

        Property<?> property;
        if( computed )
        {
            property = new ComputedPropertyInfo<Object>( builderInfo );
        }
        else
        {
            property = new PropertyInstance<Object>( builderInfo, initialValue(), this );
        }

        return wrapProperty(property);
    }

    public Property<?> newBuilderInstance(Object initialValue)
    {
        // Properties cannot be immutable during construction

        Property<?> property;
        if( computed )
        {
            property = new ComputedPropertyInfo<Object>( builderInfo );
        }
        else
        {
            property = new PropertyInstance<Object>( builderInfo, initialValue, this );
        }

        return wrapProperty(property);
    }

    public Property<?> newInitialInstance()
    {
        // Construct instance without using a builder

        return newInstance( initialValue() );
    }

    public abstract Property<?> newInstance(Object value);

    public void checkConstraints( Object value, boolean allowNull )
        throws ConstraintViolationException
    {
        if( constraints != null )
        {
            List<ConstraintViolation> violations = constraints.checkConstraints( value, allowNull );
            if( !violations.isEmpty() )
            {
                throw new ConstraintViolationException( accessor, violations );
            }
        }
    }

    public <T> T getValue( ModuleInstance moduleInstance, ValueState valueState )
    {
        return null;
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( o == null || getClass() != o.getClass() )
        {
            return false;
        }

        AbstractPropertyModel that = (AbstractPropertyModel) o;

        if( !accessor.equals( that.accessor ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return accessor.hashCode();
    }

    @Override
    public String toString()
    {
        return accessor.toGenericString();
    }

    protected Property<?> wrapProperty( Property<?> property )
    {
        if (needsWrapper && !accessor.getReturnType().isInstance( property ))
        {
            // Create proxy
            property = (Property<?>) Proxy.newProxyInstance( accessor.getReturnType().getClassLoader(), new Class[] {accessor.getReturnType()}, new PropertyHandler(property) );
        }
        return property;
    }

    protected static class ComputedPropertyInfo<T>
        extends ComputedPropertyInstance<T>
    {
        public ComputedPropertyInfo( PropertyInfo aPropertyInfo )
            throws IllegalArgumentException
        {
            super( aPropertyInfo );
        }

        public T get()
        {
            throw new IllegalStateException( "Property [" + name() + "] must be computed" );
        }
    }

    static class PropertyHandler
        implements InvocationHandler
    {
        Property p;

        public PropertyHandler( Property<?> property )
        {
            p = property;
        }

        public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
        {
            try
            {
                return method.invoke( p, args );
            }
            catch( InvocationTargetException e )
            {
                throw e.getCause();
            }
        }
    }
}