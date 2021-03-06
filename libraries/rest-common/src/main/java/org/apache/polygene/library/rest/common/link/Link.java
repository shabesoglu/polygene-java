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

package org.apache.polygene.library.rest.common.link;

import org.apache.polygene.api.common.Optional;
import org.apache.polygene.api.property.Property;
import org.apache.polygene.api.value.ValueComposite;

/**
 * Link value
 */
public interface Link
      extends ValueComposite
{
   /**
    * This is typically the description of the entity
    *
    * @return The text attribute of the Link.
    */
   @Optional
   Property<String> text();

   /**
    * This is typically the id of the entity
    *
    * @return The id attribute of the Link.
    */
   Property<String> id();

   /**
    * This is the href of the entity, relative to the producer of the LinkValue
    *
    * @return The href attribute of the Link.
    */
   Property<String> href();

   /**
    * This is the type of the link, typically to help differentiate between
    * different types of resources.
    *
    * @return The rel attribute of the Link.
    */
   @Optional
   Property<String> rel();

   /**
    * These are the classes of the link, typically to help categorize the link. Space-separated list.
    *
    * @return A space-separated list of classes of the Link.
    */
   @Optional
   Property<String> classes();
}