/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loaddistribution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import org.jgap.*;
import org.jgap.impl.CrossoverOperator;
import org.jgap.util.ICloneable;

/**
 *
 * @author Muthukumar C
 */
public class OrderCrossover extends CrossoverOperator{
    private ShipContainer[] payload;
    private IUniversalRateCalculator m_crossoverRateCalc;
    private int [] crossoverValues;

    public int[] getCrossoverValues() {
        return crossoverValues;
    }
    
      public OrderCrossover( Configuration a_configuration,
                             IUniversalRateCalculator a_crossoverRateCalculator, ShipContainer[] payload)
      throws InvalidConfigurationException {
          super(a_configuration,a_crossoverRateCalculator);
          m_crossoverRateCalc = a_crossoverRateCalculator;
          this.payload = payload;
      }

      public OrderCrossover( Configuration a_configuration,
                            int a_desiredCrossoverRate, ShipContainer[] payload)
      throws InvalidConfigurationException {
        super(a_configuration,a_desiredCrossoverRate);
        this.payload = payload;
      }

  public void operate(final Population a_population,
                      final List a_candidateChromosomes) {
    // Work out the number of crossovers that should be performed.
    // -----------------------------------------------------------
    int m_crossoverRate = getCrossOverRate();
    double m_crossoverRatePercent = getCrossOverRatePercent();
    int size = Math.min(getConfiguration().getPopulationSize(),
                        a_population.size());
    int numCrossovers = 0;
    
    if (m_crossoverRate >= 0) {
      numCrossovers = size / m_crossoverRate;
    }
    else if (m_crossoverRateCalc != null) {
      numCrossovers = size /6;
    }
    else {
      numCrossovers = (int) (size * m_crossoverRatePercent);
    }
    RandomGenerator generator = getConfiguration().getRandomGenerator();
    IGeneticOperatorConstraint constraint = getConfiguration().
        getJGAPFactory().getGeneticOperatorConstraint();
    // For each crossover, grab two random chromosomes, pick a random
    // locus (gene location), and then swap that gene and all genes
    // to the "right" (those with greater loci) of that gene between
    // the two chromosomes.
    // --------------------------------------------------------------
    int index1, index2;
   
    
    crossoverValues = new int[size];
    
    for (int i=0;i<size;i++){
        crossoverValues[i]=0;
    }
    
    List<Integer> chromeSel = new ArrayList<Integer>();
    for (int i = 0; i < size; i++) {
          if (m_crossoverRateCalc != null) {
            CrossoverRateCalculator ccalc = (CrossoverRateCalculator)m_crossoverRateCalc;
            ccalc.setCurrentFitness(a_population.getChromosome(i).getFitnessValue());
            
            int numbers = ccalc.calculateCurrentRate();
            crossoverValues[i] = numbers;
             while(numbers > 0){
                 chromeSel.add(i); numbers --;
             }
 
        }
    }
        int justtocheck = chromeSel.size();
        
      if (m_crossoverRateCalc != null) {
        if (justtocheck == 0)
        {
            System.out.println("Exception Caught.. Crossover.. chromesel array is null!!");
            for(int fix =0; fix <= size - 2 ;fix++){chromeSel.add(fix);}
        }
      }
    
    //
    for (int i = 0; i < numCrossovers; i++) {
        IChromosome chrom1;
        IChromosome chrom2;
        
     if (m_crossoverRateCalc != null) {   
        index1 = generator.nextInt(chromeSel.size());
        index2 = generator.nextInt(chromeSel.size());
         chrom1 = a_population.getChromosome(chromeSel.get(index1));
         chrom2 = a_population.getChromosome(chromeSel.get(index2));
     }
     else{
        index1 = generator.nextInt(size);
        index2 = generator.nextInt(size);
         chrom1 = a_population.getChromosome(index1);
         chrom2 = a_population.getChromosome(index2); 
         
     }
      // Verify that crossover is allowed.
      // ---------------------------------
      if (!isXoverNewAge() && chrom1.getAge() < 1 && chrom2.getAge() < 1) {
        // Crossing over two newly created chromosomes is not seen as helpful
        // here.
        // ------------------------------------------------------------------
        continue;
      }
      if (constraint != null) {
        List v = new Vector();
        v.add(chrom1);
        v.add(chrom2);
        if (!constraint.isValid(a_population, v, this)) {
          // Constraint forbids crossing over.
          // ---------------------------------
          continue;
        }
      }
      // Clone the chromosomes.
      // ----------------------
      IChromosome firstMate = (IChromosome) ((ICloneable)chrom1).clone();
      IChromosome secondMate = (IChromosome) ((ICloneable)chrom2).clone();
      
      // Cross over the chromosomes.
      // ---------------------------
      doCrossover(firstMate, secondMate, a_candidateChromosomes, generator);
    }
  }
      
  protected void doCrossover(IChromosome firstMate, IChromosome secondMate,
                           List a_candidateChromosomes,
                           RandomGenerator generator) {
    Gene[] firstGenes = firstMate.getGenes();
    Gene[] secondGenes = secondMate.getGenes();
    int locus = generator.nextInt(firstGenes.length);
    // Swap the genes.
    // ---------------
    Gene gene1,gene11;
    Gene gene2,gene22;
    Integer firstAllele, firstAllele1;
    Integer secondAllele, secondAllele1;
    
      for (int j = locus; j < firstGenes.length; j++) {  
            gene1 = firstGenes[j]; 
            gene2 = secondGenes[j];
            
            firstAllele = (Integer) gene1.getAllele();
            secondAllele =(Integer) gene2.getAllele();  
            
            if (payload[firstAllele] != null && payload[secondAllele]!= null){
               if (!checkAlleles(firstMate,j,secondAllele))
                    gene1.setAllele(secondAllele);
               if (!checkAlleles(secondMate,j,firstAllele))
                    gene2.setAllele(firstAllele);
            }
            
            if (payload[firstAllele] == null && payload[secondAllele] == null ){
                
                if(payload[64 + firstAllele].getPair() != payload[64 + secondAllele].getPair())
                   continue;
                
                if (payload[64 + firstAllele].getPair() == 1){
                  if (!checkAlleles(firstMate,j,secondAllele) && !checkAlleles(secondMate,j,firstAllele))
                       {
                           if (chkonepair(j) || chktwopair(j+1)){
                               System.out.println("Crossover.. something wrong");
                           }
                                   
                            gene11 = firstGenes[j+1];
                            firstAllele1 = (Integer) gene11.getAllele();
                            
                            gene22 = secondGenes[j+1];
                            secondAllele1 =(Integer) gene22.getAllele();
                            
                          if (!checkAlleles(firstMate,j+1,secondAllele1)){
                                gene1.setAllele(secondAllele);
                                gene11.setAllele(secondAllele1);
                          }
                           
                           if (!checkAlleles(secondMate,j+1,firstAllele1)){
                                gene2.setAllele(firstAllele);
                                gene22.setAllele(firstAllele1);
                           }
                       }
                }
                
                else if (payload[64 + firstAllele].getPair() == 2){
                    
                  if (!checkAlleles(firstMate,j,secondAllele) && !checkAlleles(secondMate,j,firstAllele)){
                      
                      gene11 = firstGenes[j-1];
                      firstAllele1 = (Integer) gene11.getAllele();
                      
                      gene22 = secondGenes[j-1];
                      secondAllele1 =(Integer) gene22.getAllele();
                      
                      if (!checkAlleles(firstMate,j-1,secondAllele1)){
                        gene1.setAllele(secondAllele);
                        gene11.setAllele(secondAllele1);
                      }    
                      
                      if (!checkAlleles(secondMate,j-1,firstAllele1)){
                          gene2.setAllele(firstAllele); 
                          gene22.setAllele(firstAllele1);
                      } 
                  }
               }
                
               else{
                    System.out.println("...Invalid Configuration Exception...");
                }
 
            }

        }
    // Add the modified chromosomes to the candidate pool so that
    // they'll be considered for natural selection during the next
    // phase of evolution.
    // -----------------------------------------------------------
    a_candidateChromosomes.add(firstMate);
    a_candidateChromosomes.add(secondMate);
  } 
  
   boolean checkAlleles(IChromosome chrom, int j, int nextInt)  {
      Gene[] newgene = chrom.getGenes();
      Integer[] alleles = new Integer[j];
      for (int i=0; i<j;i++){
         alleles[i] =  (Integer)newgene[i].getAllele();
      }
      if(Arrays.asList(alleles).contains(new Integer(nextInt))){
          return true;
      }
      return false;
  }
   
     boolean chkonepair(int j){
      if ((j+1)%4 ==0) {
       return true;
      }
      return false;
  }

  boolean chktwopair(int j){
      if ((j%4) ==0) {
       return true;
      }
      return false;
  }
  
}
