/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.ui;

/**
 *
 * @author Souley
 */
public class ParamFileEntity extends ParamEntity {
    private String fileURL;
    private boolean isInput = true;
    public ParamFileEntity(final String url) {
        fileURL = url;
    }

    public ParamFileEntity(final String id, final String url) {
        super(id, ParamType.FILE, ValueType.STRING);
        fileURL = url;
    }

    public String getURL() {
        return fileURL;
    }

    public void setURL(final String url) {
        fileURL = url;
    }

    public void setValues(final String url) {
        fileURL = url;
    }

    public boolean isInput() {
        return isInput;
    }

    public void setIsInput(final boolean isInput) {
        this.isInput = isInput;
    }
}
