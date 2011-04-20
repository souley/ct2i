/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.ui;

/**
 *
 * @author Souley
 */
public class ProcessEntity extends TomoEntity {
    private String command;
    public ProcessEntity(final String executor) {
        this.command = executor;
    }

    public ProcessEntity(final String id, final Type type, final String command) {
        super(id, type);
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(final String command) {
        this.command = command;
    }
}
