package loaddistribution;

/**
 *
 * @author Muthukumar C
 */

import java.util.Arrays;
import org.jgap.*;
import org.jgap.impl.*;

public class EqualWeightsFitnessFunction 
            extends FitnessFunction {
 private ShipContainer[] payload;
 int group;
 
    public EqualWeightsFitnessFunction(ShipContainer[] payload) {
        this.payload = payload;
    }
 
   public double evaluate(IChromosome a_subject) {

    ShipContainer onecontainer =null;
    double squaredDiff = 0.0d;
    double[] groupWeight = new double[16];
    int allweight = 0;
    
    for (int i = 0; i < 64; i++) {
      Integer myallele = (Integer)a_subject.getGene(i).getAllele();
                  if (payload[myallele] != null){
                      onecontainer = payload[myallele];
                  }
                  else{
                      onecontainer = payload[64 + myallele];
                  }
                  
        group = getGroup(i);
        groupWeight[group] += onecontainer.getWeight();
        allweight += onecontainer.getWeight();
      }
    
   // for (int i=0; i<16; i++){
       // System.out.println("Group weight "+ i + " W: "+groupWeight[i]);
  //  }
      
        for (int k = 0; k < 16; k++) {
          double diff = Math.abs(((groupWeight[k])/4) - (allweight/64));
          squaredDiff += (diff * diff);
          diff=0;
          
        }

    // we can put a chart here. squaredDiff vs generation/evolution step
   // System.out.println("squaredDiff: "+squaredDiff);
    return squaredDiff;
  }
 


    public int getGroup(int position){
        int group = 0;
        int[][] groups = {{0,7,8,15},{1,6,9,14},{2,5,10,13},{3,4,11,12},
                          {16,23,24,31},{17,22,25,30},{18,21,26,29},{19,20,27,28},
                          {32,39,40,47},{33,38,41,46},{34,37,42,45},{35,36,43,44},
                          {48,55,56,63},{49,54,57,62},{50,53,58,61},{51,52,59,60}};
        
        for (int i = 0 ; i<16 ;i++){
            for (int j=0;j<4;j++){
            if (Arrays.asList(groups[i][j]).contains(position)){
                group = i; break;
            }
          }
        }
        return group;
    }
}