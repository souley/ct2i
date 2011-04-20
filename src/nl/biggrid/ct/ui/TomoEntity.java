/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.ui;

/**
 *
 * @author Souley
 */
import java.awt.Point;

public class TomoEntity {
    public enum Type { PARAMETER, PROCESS }

    private String id;
    private Type type;
    private Point geometry;

    TomoEntity() {
        type = Type.PARAMETER;
    }

    TomoEntity(final String id, final Type type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public Point getGeometry() {
        return geometry;
    }

    public Type getType() {
        return type;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setGeometry(final Point geometry) {
        this.geometry = geometry;
    }

    public void setType(final Type type) {
        this.type = type;
    }

}
