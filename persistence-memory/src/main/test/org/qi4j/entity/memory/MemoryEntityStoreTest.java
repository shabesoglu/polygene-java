package org.qi4j.entity.memory;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.composite.CompositeBuilder;
import org.qi4j.composite.Mixins;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.EntityCompositeNotFoundException;
import org.qi4j.entity.EntitySession;
import org.qi4j.entity.SessionCompletionException;
import org.qi4j.library.framework.entity.AssociationMixin;
import org.qi4j.library.framework.entity.PropertyMixin;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.UuidIdentityGeneratorComposite;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class MemoryEntityStoreTest
    extends AbstractQi4jTest
{
    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( MemoryEntityStoreComposite.class, UuidIdentityGeneratorComposite.class );
        module.addComposites( TestComposite.class );
    }

    @Test
    public void whenNewEntityThenFindEntity()
        throws Exception
    {
        String id = createEntity();
        EntitySession session;
        TestComposite instance;

        // Find entity
        session = entitySessionFactory.newEntitySession();
        instance = session.find( id, TestComposite.class );
        assertThat( "property has correct value", instance.name().get(), equalTo( "Rickard" ) );
        session.discard();
    }

    @Test
    public void whenRemovedEntityThenCannotFindEntity()
        throws Exception
    {
        String id = createEntity();

        // Remove entity
        EntitySession session = entitySessionFactory.newEntitySession();
        TestComposite instance = session.find( id, TestComposite.class );
        session.remove( instance );
        session.complete();

        // Find entity
        session = entitySessionFactory.newEntitySession();
        try
        {
            instance = session.find( id, TestComposite.class );
            fail( "Should not be able to find entity" );
        }
        catch( EntityCompositeNotFoundException e )
        {
            // Ok!
        }
        session.discard();
    }

    @Test
    public void whenNewEntitiesThenPerformanceIsOk()
        throws Exception
    {
        long start = System.currentTimeMillis();

        int nrOfEntities = 100000;
        for( int i = 0; i < nrOfEntities; i++ )
        {
            createEntity();
        }

        long end = System.currentTimeMillis();
        System.out.println( end - start );
    }

    @Test
    public void whenBulkNewEntitiesThenPerformanceIsOk()
        throws Exception
    {
        int nrOfEntities = 1000000;
        EntitySession session = entitySessionFactory.newEntitySession();

        for( int i = 0; i < nrOfEntities; i++ )
        {
            // Create entity
            CompositeBuilder<TestComposite> builder = session.newEntityBuilder( TestComposite.class );
            builder.propertiesOfComposite().name().set( "Rickard" );
            TestComposite instance = builder.newInstance();
        }

        session.clear();

        long start = System.currentTimeMillis();

        for( int i = 0; i < nrOfEntities; i++ )
        {
            // Create entity
            CompositeBuilder<TestComposite> builder = session.newEntityBuilder( TestComposite.class );
            builder.propertiesOfComposite().name().set( "Rickard" );
            TestComposite instance = builder.newInstance();
        }

        session.complete();
        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println( end - start );
        System.out.println( nrOfEntities / ( time / 1000.0D ) );
    }

    @Test
    public void whenFindEntityThenPerformanceIsOk()
        throws Exception
    {
        long start = System.currentTimeMillis();

        String id = createEntity();

        int nrOfLookups = 1000000;
        EntitySession session = entitySessionFactory.newEntitySession();
        for( int i = 0; i < nrOfLookups; i++ )
        {
            TestComposite instance = session.find( id, TestComposite.class );
            session.clear();
        }
        session.discard();

        long end = System.currentTimeMillis();
        long time = end - start;
        System.out.println( time );
        System.out.println( nrOfLookups / ( time / 1000.0D ) );
    }

    private String createEntity()
        throws SessionCompletionException
    {
        // Create entity
        EntitySession session = entitySessionFactory.newEntitySession();
        CompositeBuilder<TestComposite> builder = session.newEntityBuilder( TestComposite.class );
        builder.propertiesOfComposite().name().set( "Rickard" );
        TestComposite instance = builder.newInstance();
        String id = instance.identity().get();
        session.complete();
        return id;
    }

    @Mixins( { PropertyMixin.class, AssociationMixin.class } )
    public interface TestComposite
        extends EntityComposite
    {
        Property<String> name();
    }
}
