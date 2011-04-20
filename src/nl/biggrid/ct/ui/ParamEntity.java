/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.ui;

/**
 *
 * @author Souley
 */
public class ParamEntity extends TomoEntity {
    public enum ParamType { VECTOR, FILE }
    public enum ValueType { INTEGER, STRING }

    private ParamType paramType;
    private ValueType valueType;

    ParamEntity() {
        paramType = ParamType.VECTOR;
        valueType = ValueType.INTEGER;
    }

    ParamEntity(final ParamType paramType, final ValueType valueType) {
        this.paramType = paramType;
        this.valueType = valueType;
    }

    ParamEntity(final String id, final ParamType paramType, final ValueType valueType) {
        super(id, TomoEntity.Type.PARAMETER);
        this.paramType = paramType;
        this.valueType = valueType;
    }

    public ParamType getParamType() {
        return paramType;
    }

    public void setParamType(final ParamType paramType) {
        this.paramType = paramType;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setParamType(final ValueType valueType) {
        this.valueType = valueType;
    }
}
