/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.swing.envisage.school.domain.model.school;

import java.io.Serializable;
import static org.qi4j.api.util.NullArgumentException.validateNotNull;

/**
 * @author edward.yakop@gmail.com
 */
public final class SchoolId
    implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String schoolId;

    public SchoolId( String aSchoolId )
        throws IllegalArgumentException
    {
        validateNotNull( "aSchoolId", aSchoolId );
        schoolId = aSchoolId;
    }

    public final String idString()
    {
        return schoolId;
    }
}