/*
 * Copyright (c) 2007-2011, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2014, Paul Merlin. All Rights Reserved.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.apache.zest.runtime.bootstrap;

import org.apache.zest.api.association.Association;
import org.apache.zest.api.association.GenericAssociationInfo;
import org.apache.zest.api.association.ManyAssociation;
import org.apache.zest.api.association.NamedAssociation;
import org.apache.zest.api.common.*;
import org.apache.zest.api.property.GenericPropertyInfo;
import org.apache.zest.api.property.Property;
import org.apache.zest.api.util.Annotations;
import org.apache.zest.api.util.Classes;
import org.apache.zest.api.value.ValueComposite;
import org.apache.zest.bootstrap.StateDeclarations;
import org.apache.zest.bootstrap.ValueAssembly;
import org.apache.zest.runtime.association.*;
import org.apache.zest.runtime.composite.StateModel;
import org.apache.zest.runtime.composite.ValueConstraintsInstance;
import org.apache.zest.runtime.composite.ValueConstraintsModel;
import org.apache.zest.runtime.property.PropertyModel;
import org.apache.zest.runtime.value.ValueModel;
import org.apache.zest.runtime.value.ValueStateModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.List;

import static org.apache.zest.api.util.Annotations.isType;
import static org.apache.zest.api.util.Classes.typeOf;

/**
 * Declaration of a ValueComposite.
 */
public final class ValueAssemblyImpl
    extends CompositeAssemblyImpl
    implements ValueAssembly
{
    private AssociationsModel associationsModel;
    private ManyAssociationsModel manyAssociationsModel;
    private NamedAssociationsModel namedAssociationsModel;

    public ValueAssemblyImpl( Class<?> compositeType )
    {
        super( compositeType );
        // The composite must always implement ValueComposite, as a marker interface
        if( !ValueComposite.class.isAssignableFrom( compositeType ) )
        {
            types.add( ValueComposite.class );
        }
    }

    @Override
    protected StateModel createStateModel()
    {
        return new ValueStateModel( propertiesModel, associationsModel, manyAssociationsModel, namedAssociationsModel );
    }

    ValueModel newValueModel(
        StateDeclarations stateDeclarations,
        AssemblyHelper helper
    )
    {
        try
        {
            associationsModel = new AssociationsModel();
            manyAssociationsModel = new ManyAssociationsModel();
            namedAssociationsModel = new NamedAssociationsModel();
            buildComposite( helper, stateDeclarations );

            return new ValueModel(
                types, visibility, metaInfo, mixinsModel, (ValueStateModel) stateModel, compositeMethodsModel );
        }
        catch( Exception e )
        {
            throw new InvalidApplicationException( "Could not register " + types, e );
        }
    }

    @Override
    protected void addStateFor( AccessibleObject accessor,
                                List<Class<?>> constraintClasses
    )
    {
        String stateName = QualifiedName.fromAccessor( accessor ).name();

        if( registeredStateNames.contains( stateName ) )
        {
            return; // Skip already registered names
        }

        Class<?> accessorType = Classes.RAW_CLASS.apply( typeOf( accessor ) );
        if( Property.class.isAssignableFrom( accessorType ) )
        {
            propertiesModel.addProperty( newPropertyModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
        else if( Association.class.isAssignableFrom( accessorType ) )
        {
            associationsModel.addAssociation( newAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
        else if( ManyAssociation.class.isAssignableFrom( accessorType ) )
        {
            manyAssociationsModel.addManyAssociation( newManyAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
        else if( NamedAssociation.class.isAssignableFrom( accessorType ) )
        {
            namedAssociationsModel.addNamedAssociation( newNamedAssociationModel( accessor, constraintClasses ) );
            registeredStateNames.add( stateName );
        }
    }

    @Override
    protected PropertyModel newPropertyModel( AccessibleObject accessor,
                                              List<Class<?>> constraintClasses
    )
    {
        List<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn(accessor);
        boolean optional = annotations.stream().anyMatch( isType( Optional.class ) );
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations.stream(), GenericPropertyInfo.propertyTypeOf( accessor ), ( (Member) accessor )
            .getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        boolean useDefaults = metaInfo.get( UseDefaults.class ) != null || stateDeclarations.useDefaults( accessor );
        Object initialValue = stateDeclarations.initialValueOf( accessor );
        return new PropertyModel( accessor, true, useDefaults, valueConstraintsInstance, metaInfo, initialValue );
    }

    public AssociationModel newAssociationModel( AccessibleObject accessor,
                                                 List<Class<?>> constraintClasses
    )
    {
        List<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn(accessor);
        boolean optional = annotations.stream().anyMatch( isType( Optional.class ) );

        // Constraints for Association references
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations.stream(), GenericAssociationInfo
            .associationTypeOf( accessor ), ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        // Constraints for the Association itself
        valueConstraintsModel = constraintsFor( annotations.stream(), Association.class, ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance associationValueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            associationValueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        return new AssociationModel( accessor, valueConstraintsInstance, associationValueConstraintsInstance, metaInfo );
    }

    public ManyAssociationModel newManyAssociationModel( AccessibleObject accessor,
                                                         List<Class<?>> constraintClasses
    )
    {
        List<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn(accessor);
        boolean optional = annotations.stream().anyMatch( isType( Optional.class ) );

        // Constraints for entities in ManyAssociation
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations.stream(), GenericAssociationInfo
            .associationTypeOf( accessor ), ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        // Constraints for the ManyAssociation itself
        valueConstraintsModel = constraintsFor( annotations.stream(), ManyAssociation.class, ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance manyValueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            manyValueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        return new ManyAssociationModel( accessor, valueConstraintsInstance, manyValueConstraintsInstance, metaInfo );
    }

    public NamedAssociationModel newNamedAssociationModel( AccessibleObject accessor,
                                                           List<Class<?>> constraintClasses
    )
    {
        List<Annotation> annotations = Annotations.findAccessorAndTypeAnnotationsIn(accessor);
        boolean optional = annotations.stream().anyMatch( isType( Optional.class ) );

        // Constraints for entities in NamedAssociation
        ValueConstraintsModel valueConstraintsModel = constraintsFor( annotations.stream(), GenericAssociationInfo
            .associationTypeOf( accessor ), ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance valueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            valueConstraintsInstance = valueConstraintsModel.newInstance();
        }

        // Constraints for the NamedAssociation itself
        valueConstraintsModel = constraintsFor( annotations.stream(), NamedAssociation.class, ( (Member) accessor ).getName(), optional, constraintClasses, accessor );
        ValueConstraintsInstance namedValueConstraintsInstance = null;
        if( valueConstraintsModel.isConstrained() )
        {
            namedValueConstraintsInstance = valueConstraintsModel.newInstance();
        }
        MetaInfo metaInfo = stateDeclarations.metaInfoFor( accessor );
        return new NamedAssociationModel( accessor, valueConstraintsInstance, namedValueConstraintsInstance, metaInfo );
    }
}