/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.ui;

/**
 *
 * @author Souley
 */

import java.util.Vector;

public class ParamDataEntity extends ParamEntity {
    private Vector<String> values = new Vector<String>();

    public ParamDataEntity(final Vector<String> data) {
        values = data;
    }

    public ParamDataEntity(final Vector<String> data, final ValueType valueType) {
        super(ParamType.VECTOR, valueType);
        values = data;
    }

    public ParamDataEntity(final String id, final Vector<String> data, final ValueType valueType) {
        super(id, ParamType.VECTOR, valueType);
        values = data;
    }

    public Vector<String> getValues() {
        return values;
    }

    public void setValues(final Vector<String> data) {
        values = data;
    }
}
