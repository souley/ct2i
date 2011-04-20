/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.run;

/**
 *
 * @author Souley
 */
public class SGEOutputParserFactory {
    public IOutputParser getOutputParser() {
        return new SGEOutputParser();
    }
}
