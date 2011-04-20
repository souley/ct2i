/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.util;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Souley
 */
public class MessageBox {
    public static void showInfoMsg(final JFrame parent, final String msg) {
        JOptionPane.showMessageDialog(
                parent,
                "<html><b><font color=\"#0000ff\">" + msg + "</b></html>",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showErrorMsg(final JFrame parent, final String msg) {
        String decoratedMsg;
        if (msg.indexOf('\n') == -1) {
            decoratedMsg = "<html><b><font color=\"#ff0000\">" + msg + "</b></html>";
        } else {
            decoratedMsg = "<html><b><p style=\"color:red\">" + msg + "</b></html>";
        }
        JOptionPane.showMessageDialog(
                parent,
                decoratedMsg,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static int askUser(final JFrame parent, final String title, final String msg) {
        //String title = "Run command ...";
        return JOptionPane.showConfirmDialog(
                parent,
                msg,
                title,
                JOptionPane.OK_CANCEL_OPTION);
    }

}
