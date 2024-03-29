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

package password.pwm.svc.intruder;

import password.pwm.error.PwmException;
import password.pwm.util.java.ClosableIterator;

import java.util.Optional;

public interface IntruderRecordManager
{
    boolean checkSubject( String subject );

    void markSubject( String subject );

    void clearSubject( String subject );

    boolean isAlerted( String subject );

    void markAlerted( String subject );

    Optional<IntruderRecord> readIntruderRecord( String subject );

    ClosableIterator<IntruderRecord> iterator( ) throws PwmException;
}
