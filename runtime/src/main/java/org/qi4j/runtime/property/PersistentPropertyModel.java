/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
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

import java.lang.reflect.Method;

import org.qi4j.api.common.MetaInfo;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.Queryable;
import org.qi4j.api.property.GenericPropertyInfo;
import org.qi4j.api.property.PropertyInfo;
import org.qi4j.runtime.composite.ValueConstraintsInstance;
import org.qi4j.runtime.structure.ModuleInstance;
import org.qi4j.runtime.structure.ModuleModel;
import org.qi4j.runtime.structure.ModuleVisitor;
import org.qi4j.runtime.value.ValueModel;
import org.qi4j.spi.property.PropertyType;
import org.qi4j.spi.property.PropertyTypeDescriptor;
import org.qi4j.spi.util.json.JSONException;
import org.qi4j.spi.util.json.JSONStringer;
import org.qi4j.spi.util.json.JSONTokener;
import org.qi4j.spi.value.ValueType;

/**
 * JAVADOC
 */
public abstract class PersistentPropertyModel
        extends AbstractPropertyModel
        implements PropertyTypeDescriptor
{
    private final boolean queryable;
    private final PropertyType propertyType;
    protected final PropertyInfo propertyInfo;

    public PersistentPropertyModel(Method accessor, Class compositeType, boolean immutable, ValueConstraintsInstance constraints, MetaInfo metaInfo, Object initialValue)
    {
        super(accessor, immutable, constraints, metaInfo, initialValue);

        final Queryable queryable = accessor.getAnnotation(Queryable.class);
        this.queryable = queryable == null || queryable.value();

        PropertyType.PropertyTypeEnum type;
        if (isComputed())
        {
            type = PropertyType.PropertyTypeEnum.COMPUTED;
        } else if (isImmutable())
        {
            type = PropertyType.PropertyTypeEnum.IMMUTABLE;
        } else
        {
            type = PropertyType.PropertyTypeEnum.MUTABLE;
        }

        propertyType = new PropertyType(qualifiedName(), ValueType.newValueType(type(), accessor.getDeclaringClass(), compositeType), this.queryable, type);

        propertyInfo = new GenericPropertyInfo(metaInfo, isImmutable(), isComputed(), qualifiedName(), type());

    }

    public PropertyType propertyType()
    {
        return propertyType;
    }

    public boolean isQueryable()
    {
        return queryable;
    }

    public String toJSON(Object value)
    {
        if (value == null)
        {
            return "null";
        }

        ValueType valueType = propertyType().type();
        JSONStringer writer = new JSONStringer();
        try
        {
            valueType.toJSON(value, writer);
            return writer.toString();
        }
        catch (JSONException e)
        {
            throw new IllegalStateException("Could not serialize value to JSON", e);
        }
    }

    public <T> T fromJSON(ModuleInstance moduleInstance, String value)
    {
        try
        {
            return (T) fromJSON(moduleInstance, new JSONTokener(value).nextValue());
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not deserialize JSON value:" + value, e);
        }
    }

    public <T> T fromJSON(ModuleInstance moduleInstance, Object value)
    {
        if (value == null)
        {
            return null;
        }

        ValueType valueType = propertyType().type();
        try
        {
            return (T) valueType.fromJSON(value, moduleInstance);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not deserialize JSON value:" + value, e);
        }
    }

    class ValueFinder
            implements ModuleVisitor
    {
        public Class type;
        public ValueModel model;
        public ModuleInstance module;

        public boolean visitModule(ModuleInstance moduleInstance, ModuleModel moduleModel, Visibility visibility)
        {

            model = moduleModel.values().getValueModelFor(type, visibility);
            if (model != null)
            {
                module = moduleInstance;
            }

            return model == null;
        }
    }
}
