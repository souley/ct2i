/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nl.biggrid.ct.util;

/**
 *
 * @author Souley
 */
public class DecodeComparator {

    class Triple {
        int phi;
        int psi;
        int theta;
        Triple() {
            phi = psi = theta = 0;
        }
        Triple(int somePhi, int somePsi, int someTheta) {
            phi = somePhi;
            psi = somePsi;
            theta = someTheta;
        }

        @Override
        public String toString() {
            return "[phi="+phi+", psi="+psi+", theta="+theta+"]";
        }
    }

    int start = 0;
    int end = 179;
    int inc = 30;

    Triple getOmnimatchAngles(int index) {
        Triple angles = new Triple();
        int nphi = (end-start)/inc + 1;
        int npsi = (end-start)/inc + 1;

        angles.theta = (int)Math.floor(index /(nphi*npsi));
        int rest = index - angles.theta*(nphi*npsi);
        angles.psi = (int)Math.floor(rest/nphi);
        angles.phi = rest - angles.psi*nphi;
        angles.phi = angles.phi * inc + start;
        angles.psi = angles.psi * inc + start;
        angles.theta = angles.theta * inc + start;

        return angles;
    }

    int getInterval() {
        return ((int)Math.ceil((end-start)/inc))+1;
    }

    Triple getArgosAngles(int index) {
        Triple angles = new Triple();
        angles.phi = (start + ((int)index%getInterval()) * inc)%360;
        angles.theta = (start + ((int)Math.floor((index%(getInterval()*getInterval()))/(float)getInterval())) * inc)%360;
        angles.psi = (start + ((int)Math.floor(index/(float)(getInterval()*getInterval()))) * inc)%360;
        return angles;
    }

    public DecodeComparator() {

    }

    public static void main(String[] args) {
        DecodeComparator instance = new DecodeComparator();
        int ang_min=0, ang_max=216;
        for (int index=ang_min; index<ang_max; index++) {
            System.out.println("index="+index+" <-> OmniMatch: "+instance.getOmnimatchAngles(index)
                    +"\tArgos: "+instance.getArgosAngles(index));
        }
    }
}
