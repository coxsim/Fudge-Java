/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc. and other contributors.
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

package org.fudgemsg.mapping;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeRuntimeException;

/**
 * Attempt to create an object using a no-arg constructor and Java-bean style
 * setX methods.
 * 
 * @author Andrew
 */
/* package */ class ReflectionObjectBuilder<T> extends ReflectionBuilderBase<T> implements FudgeObjectBuilder<T> {
  
  private final Constructor<T> _constructor;
  
  /* package */ static <T> ReflectionObjectBuilder<T> create (final Class<T> clazz) {
    try {
      final Constructor<T> constructor = clazz.getConstructor ();
      return new ReflectionObjectBuilder<T> (clazz, constructor);
    } catch (SecurityException e) {
      // ignore
    } catch (NoSuchMethodException e) {
      // ignore
    }
    return null;
  }
  
  private ReflectionObjectBuilder (final Class<T> clazz, final Constructor<T> constructor) {
    super (clazz, "set", 1, null);
    _constructor = constructor;
  }
  
  @Override
  public T buildObject (final FudgeDeserialisationContext context, final FudgeFieldContainer message) {
    try {
      final T base = _constructor.newInstance ();
      for (FudgeField field : message.getAllFields ()) {
        final Method method = getMethods ().get (field.getName ());
        if (method != null) {
          final Class<?>[] params = method.getParameterTypes ();
          final Object v;
          if (params[0] == Object.class) {
            v = context.fieldValueToObject (field);
          } else {
            v = context.fieldValueToObject (params[0], field);
          }
          //System.out.println ("m: " + method + " v:" + v);
          method.invoke (base, v);
        }
      }
      return base;
    } catch (IllegalArgumentException e) {
      throw new FudgeRuntimeException ("Couldn't create " + _constructor.getDeclaringClass () + " object", e);
    } catch (InstantiationException e) {
      throw new FudgeRuntimeException ("Couldn't create " + _constructor.getDeclaringClass () + " object", e);
    } catch (IllegalAccessException e) {
      throw new FudgeRuntimeException ("Couldn't create " + _constructor.getDeclaringClass () + " object", e);
    } catch (InvocationTargetException e) {
      throw new FudgeRuntimeException ("Couldn't create " + _constructor.getDeclaringClass () + " object", e);
    }
  }
  
}