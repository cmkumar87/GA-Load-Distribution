/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loaddistribution;

import java.util.List;
import org.jgap.Configuration;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultCrossoverRateCalculator;

/**
 *
 * @author Muthukumar C
 */
public class CrossoverRateCalculator extends DefaultCrossoverRateCalculator{

    
    private double genotypeAvgFitness;
    private double maxGenotypeFitness;
    private double chromeOneFitness;
    private double chromeTwoFitness;
    private double currentFitness;
    private int    rate;
    



    public void setMaxGenotypeFitness(double maxGenotypeFitness) {
        this.maxGenotypeFitness = maxGenotypeFitness;
    }
    

    public void setChromeOneFitness(double chromeOneFitness) {
        this.chromeOneFitness = chromeOneFitness;
    }

    public void setChromeTwoFitness(double chromeTwoFitness) {
        this.chromeTwoFitness = chromeTwoFitness;
    }

    public void setCurrentFitness(double currentFitness) {
        this.currentFitness = currentFitness;
    }


    public void setGenotypeAvgFitness(double genotypeAvgFitness) {
        this.genotypeAvgFitness = genotypeAvgFitness;
    }

    // Rate calulator method
    public CrossoverRateCalculator(Configuration a_config)
        throws InvalidConfigurationException {
        super(a_config);
    }

    public int calculateCurrentRate(){
        
        //currentFitness = Math.max(chromeOneFitness,chromeTwoFitness);
        
      if ( currentFitness == maxGenotypeFitness )  {
        rate = 0;  // max fit. probability of mutation is 0
      }
      else if(currentFitness <= genotypeAvgFitness){
        rate = (int)(((currentFitness - maxGenotypeFitness)/(genotypeAvgFitness - maxGenotypeFitness ))*100);
        // high fit - diff will be smaller for numerator than denominator => value <= 1
      }
      else{
          rate = 100;
      }
      
      writer("Running calculateCurrentRate..."+rate);
      return rate;
        
  }
  
  public void writer(String str){
      System.out.println(str);
  }
}
