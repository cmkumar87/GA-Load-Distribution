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

    private double[] indFitness = new double[65];
    private double genotypeAvgFitness;
    private double maxGenotypeFitness;
    private double fitnessDelta;
    private int rate;

    public void setFitnessDelta(double fitnessDelta) {
        this.fitnessDelta = fitnessDelta;
    }

    public void setGenotypeAvgFitness(double genotypeAvgFitness) {
        this.genotypeAvgFitness = genotypeAvgFitness;
    }

    public void setIndFitness(double[] indFitness) {
        this.indFitness = indFitness;
    }
    
    private double pickIndFitnessAt(int index){
        return indFitness[index];
    }

  public CrossoverRateCalculator(Configuration a_config)
      throws InvalidConfigurationException {
    super(a_config);

  }
    
  public int calculateCurrentRate(){
      writer("Running calculateCurrentRate..."+rate);
      
      
//    int size = getConfiguration().getChromosomeSize();
//    if (size < 1) {
//      size = 1;
//    }
    return rate;
  }
  
  public void writer(String str){
      System.out.println(str);
  }
}
