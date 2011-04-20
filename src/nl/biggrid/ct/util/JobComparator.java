/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.util;

/**
 *
 * @author Souley
 */
import java.util.Comparator;

public class JobComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        if (o1 instanceof String && o2 instanceof String) {
            int id1 = Integer.parseInt((String) o1);
            int id2 = Integer.parseInt((String) o2);
            return Integer.valueOf(id1).compareTo(Integer.valueOf(id2));
        }
        return 0;
    }
}
