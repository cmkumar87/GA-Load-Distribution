package loaddistribution;

/**
 *
 * @author Muthukumar C
 */

import org.jgap.*;
import org.jgap.impl.*;

public class EqualWeightsFitnessFunction 
            extends FitnessFunction {
 private ShipContainer[] payload;

    public EqualWeightsFitnessFunction(ShipContainer[] payload) {
        this.payload = payload;
    }
 
   public double evaluate(IChromosome a_subject) {
      // System.out.println("Evaluate Now");  
    double[] groupWeights = new double[16];
    double squaredDiff = 0.0d;
    for (int i = 0; i < 16; i++) {
      double groupWeight = 0.0d;
      for (int j = 1; j < 5; j++) {
        IntegerGene containerID = (IntegerGene) a_subject.getGene( (i * 4 + j));
        ShipContainer container= (ShipContainer) payload[containerID.intValue()];
        groupWeight += container.getWeight();
      }
      if (i > 1) {
        for (int k = 0; k < i; k++) {
          double diff = Math.abs(groupWeight - groupWeights[k]);
          squaredDiff += diff * diff;
        }
      }
      groupWeights[i] = groupWeight;
    }
    // we can put a chart here. squaredDiff vs generation/evolution step
    //System.out.println("squaredDiff: "+squaredDiff);
    return squaredDiff;
  }
    
}
