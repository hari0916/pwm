/*
 * Password Management Servlets (PWM)
 * http://www.pwm-project.org
 *
 * Copyright (c) 2006-2009 Novell, Inc.
 * Copyright (c) 2009-2021 The PWM Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package password.pwm.svc.cr;

import com.novell.ldapchai.ChaiUser;
import com.novell.ldapchai.cr.ChaiCrFactory;
import com.novell.ldapchai.cr.ChaiResponseSet;
import com.novell.ldapchai.cr.ResponseSet;
import com.novell.ldapchai.exception.ChaiException;
import com.novell.ldapchai.exception.ChaiValidationException;
import password.pwm.PwmDomain;
import password.pwm.bean.ResponseInfoBean;
import password.pwm.bean.SessionLabel;
import password.pwm.bean.UserIdentity;
import password.pwm.config.option.DataStorageMethod;
import password.pwm.error.ErrorInformation;
import password.pwm.error.PwmError;
import password.pwm.error.PwmOperationalException;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.svc.db.DatabaseAccessor;
import password.pwm.svc.db.DatabaseException;
import password.pwm.svc.db.DatabaseTable;
import password.pwm.util.logging.PwmLogger;

import java.util.Optional;


public class DbCrOperator implements CrOperator
{

    private static final PwmLogger LOGGER = PwmLogger.forClass( DbCrOperator.class );

    final PwmDomain pwmDomain;

    public DbCrOperator( final PwmDomain pwmDomain )
    {
        this.pwmDomain = pwmDomain;
    }

    @Override
    public void close( )
    {
    }

    @Override
    public Optional<ResponseSet> readResponseSet(
            final SessionLabel sessionLabel,
            final ChaiUser theUser,
            final UserIdentity userIdentity,
            final String userGUID
    )
            throws PwmUnrecoverableException
    {
        if ( userGUID == null || userGUID.length() < 1 )
        {
            final String errorMsg = "user " + theUser.getEntryDN() + " does not have a guid, unable to search for responses in remote database";
            final ErrorInformation errorInformation = new ErrorInformation( PwmError.ERROR_MISSING_GUID, errorMsg );
            throw new PwmUnrecoverableException( errorInformation );
        }

        try
        {
            final DatabaseAccessor databaseAccessor = pwmDomain.getPwmApplication().getDatabaseService().getAccessor();
            final Optional<String> responseStringBlob = databaseAccessor.get( DatabaseTable.PWM_RESPONSES, userGUID );
            if ( responseStringBlob.isPresent() )
            {
                final ResponseSet userResponseSet = ChaiResponseSet.parseChaiResponseSetXML( responseStringBlob.get(), theUser );
                LOGGER.debug( sessionLabel, () -> "found responses for " + theUser.getEntryDN() + " in remote database: " + userResponseSet.toString() );
                return Optional.of( userResponseSet );
            }
            else
            {
                LOGGER.trace( sessionLabel, () -> "user guid for " + theUser.getEntryDN() + " not found in remote database (key=" + userGUID + ")" );
            }
        }
        catch ( final ChaiValidationException e )
        {
            final String errorMsg = "unexpected error reading responses for " + theUser.getEntryDN() + " from remote database: " + e.getMessage();
            final ErrorInformation errorInformation = new ErrorInformation( PwmError.ERROR_INTERNAL, errorMsg );
            throw new PwmUnrecoverableException( errorInformation );
        }
        catch ( final PwmOperationalException e )
        {
            final String errorMsg = "unexpected error reading responses for " + theUser.getEntryDN() + " from remote database: " + e.getMessage();
            final ErrorInformation errorInformation = new ErrorInformation( e.getErrorInformation().getError(), errorMsg );
            throw new PwmUnrecoverableException( errorInformation );
        }
        return Optional.empty();
    }

    @Override
    public Optional<ResponseInfoBean> readResponseInfo( final SessionLabel sessionLabel, final ChaiUser theUser, final UserIdentity userIdentity, final String userGUID )
            throws PwmUnrecoverableException
    {
        try
        {
            final Optional<ResponseSet> responseSet = readResponseSet( sessionLabel, theUser, userIdentity, userGUID );
            return responseSet.isEmpty() ? Optional.empty() : Optional.of( CrOperators.convertToNoAnswerInfoBean( responseSet.get(), DataStorageMethod.DB ) );
        }
        catch ( final ChaiException e )
        {
            throw new PwmUnrecoverableException( new ErrorInformation(
                    PwmError.ERROR_RESPONSES_NORESPONSES,
                    "unexpected error reading response info for " + theUser.getEntryDN() + ", error: " + e.getMessage()
            ) );
        }
    }

    @Override
    public void clearResponses(
            final SessionLabel sessionLabel,
            final UserIdentity userIdentity,
            final ChaiUser theUser,
            final String userGUID
    )
            throws PwmUnrecoverableException
    {
        if ( userGUID == null || userGUID.length() < 1 )
        {
            throw new PwmUnrecoverableException( new ErrorInformation(
                    PwmError.ERROR_MISSING_GUID,
                    "cannot clear responses to remote database, user " + theUser.getEntryDN() + " does not have a guid"
            ) );
        }

        try
        {
            final DatabaseAccessor databaseAccessor = pwmDomain.getPwmApplication().getDatabaseService().getAccessor();
            databaseAccessor.remove( DatabaseTable.PWM_RESPONSES, userGUID );
            LOGGER.info( sessionLabel, () -> "cleared responses for user " + theUser.getEntryDN() + " in remote database" );
        }
        catch ( final DatabaseException e )
        {
            final ErrorInformation errorInfo = new ErrorInformation(
                    PwmError.ERROR_CLEARING_RESPONSES,
                    "unexpected error clearing responses for " + theUser.getEntryDN() + " in remote database, error: " + e.getMessage()
            );
            throw new PwmUnrecoverableException( errorInfo, e );
        }
    }

    @Override
    public void writeResponses(
            final SessionLabel sessionLabel,
            final UserIdentity userIdentity,
            final ChaiUser theUser,
            final String userGUID,
            final ResponseInfoBean responseInfoBean
    )
            throws PwmUnrecoverableException
    {
        if ( userGUID == null || userGUID.length() < 1 )
        {
            throw new PwmUnrecoverableException( new ErrorInformation(
                    PwmError.ERROR_MISSING_GUID,
                    "cannot save responses to remote database, user " + theUser.getEntryDN() + " does not have a guid" )
            );
        }

        LOGGER.trace( sessionLabel, () -> "attempting to save responses for " + theUser.getEntryDN() + " in remote database (key=" + userGUID + ")" );

        try
        {
            final ChaiResponseSet responseSet = ChaiCrFactory.newChaiResponseSet(
                    responseInfoBean.getCrMap(),
                    responseInfoBean.getHelpdeskCrMap(),
                    responseInfoBean.getLocale(),
                    responseInfoBean.getMinRandoms(),
                    theUser.getChaiProvider().getChaiConfiguration(),
                    responseInfoBean.getCsIdentifier()
            );

            final DatabaseAccessor databaseAccessor = pwmDomain.getPwmApplication().getDatabaseService().getAccessor();
            databaseAccessor.put( DatabaseTable.PWM_RESPONSES, userGUID, responseSet.stringValue() );
            LOGGER.info( sessionLabel, () -> "saved responses for " + theUser.getEntryDN() + " in remote database (key=" + userGUID + ")" );
        }
        catch ( final ChaiException e )
        {
            throw PwmUnrecoverableException.fromChaiException( e );
        }
        catch ( final DatabaseException e )
        {
            final ErrorInformation errorInfo = new ErrorInformation(
                    PwmError.ERROR_WRITING_RESPONSES,
                    "unexpected error saving responses for " + theUser.getEntryDN() + " in remote database: " + e.getMessage()
            );
            LOGGER.error( sessionLabel, errorInfo::toDebugStr );
            throw new PwmUnrecoverableException( errorInfo, e );
        }
    }
}
