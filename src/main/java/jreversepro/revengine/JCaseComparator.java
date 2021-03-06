/*
  @(#)JCaseComparator.java JReversePro - Java Decompiler / Disassembler.
 * Copyright (C) 2000 2001 Karthik Kumar.
 * EMail: akkumar@users.sourceforge.net
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it , under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.If not, write to
 * The Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package jreversepro.revengine;

//import jreversepro.common.Helper;

import java.util.Comparator;

/**
 * Comparator for comparing two case entries.
 *
 * @author Karthik Kumar.
 */
public class JCaseComparator <T> implements Comparator<T> {

    /**
     * @param o1 First Object to be compared.
     * @param o2 Second object to be compared.
     * @return 0 if both the case statements' target are equal.
     * 1, if first target &gt; second target.
     * -1, otherwise.
     */
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof JCaseEntry) || !(o2 instanceof JCaseEntry)) {
            return 0;
        }
        JCaseEntry e1 = (JCaseEntry) o1;
        JCaseEntry e2 = (JCaseEntry) o2;

        int exec1 = e1.getTarget();
        int exec2 = e2.getTarget();

        return Integer.compare(exec1, exec2);
    }

    /**
     * @param obj Object to be compared.
     * @return true, if the object is JCaseEntry.
     * false, otherwise.
     */
    public boolean equals(Object obj) {
        return obj instanceof JCaseEntry;
    }
}
