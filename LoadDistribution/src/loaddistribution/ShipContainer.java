/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loaddistribution;

/**
 *
 * @author A0092669M
 */
 public abstract class ShipContainer {
    protected int containerID;
    protected double weight;
    protected int pair = 0;
    public int getContainerID() {
        return containerID;
    }

    public int getType() {
        return 0;
    }

    public void setContainerID(int containerID) {
        this.containerID = containerID;
    }
    
    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    public int getPair() {
        return pair;
    }
    
}
