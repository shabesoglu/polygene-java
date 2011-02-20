package org.qi4j.samples.dddsample.application.ws.assembly;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.jws.WebService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import se.citerus.dddsample.application.ws.HandlingEventServiceEndpoint;
import se.citerus.dddsample.application.ws.InvalidEventTypeException;
import se.citerus.dddsample.domain.model.cargo.TrackingId;
import se.citerus.dddsample.domain.model.carrier.CarrierMovementId;
import se.citerus.dddsample.domain.model.handling.HandlingEvent;
import se.citerus.dddsample.domain.model.handling.HandlingEventFactory;
import se.citerus.dddsample.domain.model.location.UnLocode;
import se.citerus.dddsample.domain.service.HandlingEventService;
import se.citerus.dddsample.domain.service.UnknownCarrierMovementIdException;
import se.citerus.dddsample.domain.service.UnknownLocationException;
import se.citerus.dddsample.domain.service.UnknownTrackingIdException;

@WebService( endpointInterface = "se.citerus.dddsample.application.ws.HandlingEventServiceEndpoint" )
public class HandlingEventServiceEndpointImpl
    implements HandlingEventServiceEndpoint
{
    private static final Logger logger = LoggerFactory.getLogger( HandlingEventServiceEndpointImpl.class );

    private HandlingEventFactory handlingEventFactory;
    private HandlingEventService handlingEventService;
    private TransactionTemplate transactionTemplate;
    protected static final String ISO_8601_FORMAT = "yyyy-mm-dd HH:MM:SS.SSS";

    public void register( final String completionTime, final String trackingId, final String carrierMovementId,
                          final String unlocode, final String eventType
    )
    {
        try
        {
            final Date date = parseIso8601Date( completionTime );
            final TrackingId tid = new TrackingId( trackingId );
            final CarrierMovementId cid;
            if( StringUtils.isNotEmpty( carrierMovementId ) )
            {
                cid = new CarrierMovementId( carrierMovementId );
            }
            else
            {
                cid = null;
            }
            final HandlingEvent.Type type = parseEventType( eventType );
            final UnLocode ul = new UnLocode( unlocode );

            doRegister( date, tid, cid, type, ul );
        }
        catch( IllegalArgumentException iae )
        {
            handleIllegalArgument( iae );
        }
        catch( ParseException pe )
        {
            handleInvalidDateFormat( completionTime );
        }
        catch( InvalidEventTypeException iete )
        {
            handleInvalidEventType( iete );
        }
        catch( Exception e )
        {
            handleOtherError( e );
        }
    }

    // TODO this entire step would be well suited to move to a consumer of asynchronous messages
    private void doRegister( final Date date,
                             final TrackingId tid,
                             final CarrierMovementId cid,
                             final HandlingEvent.Type type,
                             final UnLocode ul
    )
    {
        // Using programmatic demarcation here due to weaving conflicts
        // between jax-ws and Spring transaction annotations
        transactionTemplate.execute( new TransactionCallbackWithoutResult()
        {
            protected void doInTransactionWithoutResult( TransactionStatus status )
            {
                try
                {
                    HandlingEvent event = handlingEventFactory.createHandlingEvent( date, tid, cid, ul, type );
                    handlingEventService.register( event );
                }
                catch( UnknownCarrierMovementIdException e )
                {
                    handleUnknownCarrierMovementId( e );
                }
                catch( UnknownTrackingIdException e )
                {
                    handleUnknownTrackingId( e );
                }
                catch( UnknownLocationException e )
                {
                    handleUnknownLocation( e );
                }
            }
        } );
    }

    private HandlingEvent.Type parseEventType( final String eventType )
        throws InvalidEventTypeException
    {
        try
        {
            return HandlingEvent.Type.valueOf( eventType );
        }
        catch( IllegalArgumentException e )
        {
            throw new InvalidEventTypeException( eventType );
        }
    }

    private Date parseIso8601Date( final String completionTime )
        throws ParseException
    {
        return new SimpleDateFormat( ISO_8601_FORMAT ).parse( completionTime );
    }

    // Validation/translation errors

    private void handleIllegalArgument( IllegalArgumentException iae )
    {
        logger.error( iae.getMessage(), iae );
    }

    private void handleOtherError( Exception e )
    {
        logger.error( e.getMessage(), e );
    }

    private void handleInvalidEventType( InvalidEventTypeException iete )
    {
        logger.error( iete.getMessage(), iete );
    }

    private void handleInvalidDateFormat( String completionTime )
    {
        logger.error( "Invalid date format: " + completionTime + ", must be on ISO 8601 format: " + ISO_8601_FORMAT );
    }

    // Domain errors, don't belong here really

    private void handleUnknownLocation( UnknownLocationException e )
    {
        logger.error( e.getMessage(), e );
    }

    private void handleUnknownCarrierMovementId( UnknownCarrierMovementIdException e )
    {
        logger.error( e.getMessage(), e );
    }

    private void handleUnknownTrackingId( Exception e )
    {
        logger.error( e.getMessage(), e );
    }

    // Setters

    public void setHandlingEventService( HandlingEventService handlingEventService )
    {
        this.handlingEventService = handlingEventService;
    }

    public void setTransactionManager( PlatformTransactionManager transactionManager )
    {
        transactionTemplate = new TransactionTemplate( transactionManager );
    }

    public void setHandlingEventFactory( HandlingEventFactory handlingEventFactory )
    {
        this.handlingEventFactory = handlingEventFactory;
    }
}