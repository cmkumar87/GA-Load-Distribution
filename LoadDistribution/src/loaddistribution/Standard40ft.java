/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loaddistribution;

/**
 *
 * @author A0092669M
 */
public class Standard40ft extends ShipContainer{
    private static final int type = 40;
    private double length = 6.06;
    private double width = 2.44;
    private double height = 2.59;
    private double floorArea; 
    private double volume;
    

    public Standard40ft() {
        floorArea = this.length * this.width;
        volume = floorArea * height;
        // Realworld measure of volume considers internal dimensions
        // we neglect these differences here
    }
    
    public int getType() {
        return type;
    }

    public double getHeight() {
        return height;
    }

    public double getLength() {
        return length;
    }

    public double getVolume() {
        return volume;
    }

    public double getWidth() {
        return width;
    }
    

}
