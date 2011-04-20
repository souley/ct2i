/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.run;

/**
 *
 * @author Souley
 */
public class LSFOutputParserFactory implements IOutputParserFactory {
    public IOutputParser getOutputParser() {
        return new LSFOutputParser();
    }
}
