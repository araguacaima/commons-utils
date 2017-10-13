/*
 * Copyright 2017 araguacaima
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.araguacaima.commons.exception.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

/**
 * Es un tipo especial de excepcion que permita guardar multiples excepciones
 * en su interior, ideal para validaciones y otros procesos donde queremos
 * obtener mas de un fallo a la vez.
 * Esta pensada para representar problemas de negocio como validaciones, por lo
 * que hereda de ApplicationException.  Si se ve su utilidad para casos tecnicos
 * se puede poner a heredar de GeneralException.
 * Dado que es una excepcion Runtime, no debe ser lanzada dentro de un EJB.
 * Title: ApplicationException.java
 *
 * @author Alejandro Manuel Méndez Araguacaima (AMMA)
 * Changes:<br>
 * <ul>
 * <li> 2014-11-26 (AMMA)  Creacion de la clase. </li>
 * </ul>
 */

public class StackException extends ApplicationException implements Collection, Serializable {

    private static final long serialVersionUID = -3432443583065897052L;

    private final Collection detailedMsgs;

    public StackException(String detailMessage, Severity severity) {
        super(detailMessage, severity);
        detailedMsgs = new Vector<>();
    }

    public StackException(String detailMessage, Severity severity, String propertyString) {
        super(detailMessage, severity, propertyString);
        detailedMsgs = new Vector<>();
    }

    public boolean add(Object o) { /* Exception */
        return detailedMsgs.add(o);
    }

    public boolean addAll(Collection c) {
        return detailedMsgs.addAll(c);
    }

    public void clear() {
        detailedMsgs.clear();
    }

    public boolean contains(Object o) { /* Exception */
        return detailedMsgs.contains(o);
    }

    public boolean containsAll(Collection c) {
        return detailedMsgs.contains(c);
    }

    public boolean isEmpty() {
        return detailedMsgs.isEmpty();
    }

    public Iterator iterator() {
        return detailedMsgs.iterator();
    }

    public boolean remove(Object o) { /* Exception */
        return detailedMsgs.remove(o);
    }

    public boolean removeAll(Collection c) {
        return detailedMsgs.removeAll(c);
    }

    public boolean retainAll(Collection c) {
        return detailedMsgs.retainAll(c);
    }

    public int size() {
        return detailedMsgs.size();
    }

    public Object[] toArray() {
        return detailedMsgs.toArray();
    }

    public Object[] toArray(Object a[]) { /* Exception */
        return detailedMsgs.toArray(a);
    }

}