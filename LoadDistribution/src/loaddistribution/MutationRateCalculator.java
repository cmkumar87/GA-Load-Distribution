/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loaddistribution;

import org.jgap.Configuration;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultCrossoverRateCalculator;

/**
 *
 * @author Muthukumar C
 */
public class MutationRateCalculator extends DefaultCrossoverRateCalculator{
    
    private double currentFitness;
    private double genotypeAvgFitness;
    private double maxGenotypeFitness;
    private double fitnessDelta;
    private int rate;
    private double unnormalizedRate;

    public void setFitnessDelta(double fitnessDelta) {
        this.fitnessDelta = fitnessDelta;
    }

    public void setGenotypeAvgFitness(double genotypeAvgFitness) {
        this.genotypeAvgFitness = genotypeAvgFitness;
    }

    public void setMaxGenotypeFitness(double maxGenotypeFitness) {
        this.maxGenotypeFitness = maxGenotypeFitness;
    }
    
    public MutationRateCalculator(Configuration a_config)
          throws InvalidConfigurationException {
            super(a_config);
    }
  
    public void setcurrentFitness(double currentFitness){
        this.currentFitness = currentFitness;
    }
    
  @Override
  public int calculateCurrentRate() {
      System.out.print("Average Fitness"+genotypeAvgFitness);
      if ( currentFitness == maxGenotypeFitness )  {
        rate = 999;  // max fit. probability of mutation is 0
      }
      else if (currentFitness <= genotypeAvgFitness){
        unnormalizedRate =  ((genotypeAvgFitness - maxGenotypeFitness )/(currentFitness - maxGenotypeFitness)) ;
        rate = (int) unnormalizedRate;
        //rate =(int) (1 + ( ((unnormalizedRate - 1)*63)/ (genotypeAvgFitness - maxGenotypeFitness )));
        // high fit - diff will be smaller for numerator than denominator => value <= 1
            if (rate <= 0){
                System.out.println("asjasljdl");
            }
        
      }
//      else if (currentFitness == genotypeAvgFitness){
//        rate =  (int)(Math.pow(currentFitness - maxGenotypeFitness)*64));
//      }
      else{
          // less fit
        rate = 1; // one third chance of being mutated
      }
      return rate;
  }
}
