/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loaddistribution;

import java.util.Arrays;
import java.util.List;
import org.jgap.*;
import org.jgap.impl.SwappingMutationOperator;
import org.jgap.util.ICloneable;

/**
 *
 * @author Muthukumar C
 */
public class SwapMutation extends SwappingMutationOperator{

    ShipContainer[] payload;
    private int [] mutationValues;

    public int[] getMutationValues() {
        return mutationValues;
    }
    
  public SwapMutation(final Configuration a_config,
                      final IUniversalRateCalculator
                                          a_mutationRateCalculator,
                      ShipContainer[] payload)
      throws InvalidConfigurationException {
    super(a_config, a_mutationRateCalculator);
    this.payload = payload;
  }
    
    
  public SwapMutation(final Configuration a_config,
                      final int a_desiredMutationRate,
                                ShipContainer[] payload)
      throws InvalidConfigurationException {
    super(a_config, a_desiredMutationRate);
    this.payload = payload;
  }
  

    @Override
  public void operate(final Population a_population,
                      List a_candidateChromosomes) {
    // this was a private variable, now it is local reference.
    final IUniversalRateCalculator m_mutationRateCalc = getMutationRateCalc();
    // If the mutation rate is set to zero and dynamic mutation rate is
    // disabled, then we don't perform any mutation.
    // ----------------------------------------------------------------
    if (getMutationRate() == 0 && m_mutationRateCalc == null) {
      return;
    }
    // Determine the mutation rate. If dynamic rate is enabled, then
    // calculate it based upon the number of genes in the chromosome.
    // Otherwise, go with the mutation rate set upon construction.
    // --------------------------------------------------------------
    int currentRate;
   
    RandomGenerator generator = getConfiguration().getRandomGenerator();
    // It would be inefficient to create copies of each Chromosome just
    // to decide whether to mutate them. Instead, we only make a copy
    // once we've positively decided to perform a mutation.
    // ----------------------------------------------------------------
    int size = a_population.size();
    double max_fitness,curr_fitness;
    mutationValues = new int[size];
    
        for (int i=0;i<size;i++){
            mutationValues[i]=0;
        }
    
    for (int i = 0; i < size; i++) {
      IChromosome x = a_population.getChromosome(i);
      // Adaptive Rate calculation is done here.
       if (m_mutationRateCalc != null) {
            MutationRateCalculator mcalc = (MutationRateCalculator)m_mutationRateCalc;
            curr_fitness =x.getFitnessValue();
            mcalc.setcurrentFitness(curr_fitness);
            max_fitness = a_population.determineFittestChromosome().getFitnessValue();
            if( curr_fitness < max_fitness){
                System.out.println("Exception.. Max fitness is less than curr fitness");
                }
            mcalc.setMaxGenotypeFitness(max_fitness);
            currentRate = m_mutationRateCalc.calculateCurrentRate();
            System.out.print(" Max: "+max_fitness);
            System.out.print(" Fitness: "+curr_fitness);
            System.out.print(" Rate: "+currentRate);
            if (currentRate!= 999){
                mutationValues[i]=currentRate;
            }
            else{
                mutationValues[i]= 0;
            }
        }
         else {
            currentRate = getMutationRate();
            System.out.print(" Rate: "+currentRate);
        }
      
      // This returns null if not mutated:
      IChromosome xm = operate(x, currentRate, generator);
      if (xm != null) {
        a_candidateChromosomes.add(xm);
      }
    }
  }
  
  // Chromosome Operation
    @Override
  protected IChromosome operate(final IChromosome a_chrom, final int a_rate,
                                final RandomGenerator a_generator) {
    IChromosome chromosome = null;
    int geneMutationCount = 0;
    // ----------------------------------------
    for (int j = 0; j < a_chrom.size(); j++) {
      // Ensure probability of 1/currentRate for applying mutation.
      if (a_generator.nextInt(a_rate) == 0) {
        geneMutationCount++;
        if (chromosome == null) {
          chromosome = (IChromosome) ((ICloneable)a_chrom).clone();
        }
        
        Gene[] genes = chromosome.getGenes();
        Gene[] mutated = operate(a_generator, j, genes);
        // setGenes is not required for this operator, but it may
        // be needed for the derived operators.
        // ------------------------------------------------------
        try {
          chromosome.setGenes(mutated);
        }
        catch (InvalidConfigurationException cex) {
          throw new Error("Gene type not allowed by constraint checker", cex);
        }
      }
    }
        System.out.println(" Genes count: "+geneMutationCount);
    return chromosome;
  }

  
  // Gene operation
    @Override
  protected Gene[] operate(final RandomGenerator a_generator,
                           final int a_target_gene, final Gene[] a_genes) {
    // swap this gene with the other one now:
    //  mutateGene(genes[j], generator);
    Gene t;
    Integer a_target_allele;
    int target_pair;
    
    int other = 0;
    Integer other_allele;
    int other_pair;
    
    // Fix Target Allele.
    
    a_target_allele = (Integer)a_genes[a_target_gene].getAllele();
    
        if (payload[a_target_allele] == null)
        {
            if (a_target_gene == 63) {return a_genes;}
            a_target_allele = 64 + a_target_allele;
        }
        
        target_pair = payload[a_target_allele].getPair();
       
        if (target_pair == 1){
                if(chkonepair(a_target_gene)){
                    return a_genes;
                }
        }
        else if(target_pair == 2){
                if(chktwopair(a_target_gene)){
                    return a_genes;
                }
        }
   
   // Search for the other Allele.
        other = a_generator.nextInt(a_genes.length);
        other_allele = (Integer)a_genes[other].getAllele();
            
        if (payload[other_allele] == null)
        {
            other_allele = 64 + other_allele;
        }
        other_pair = payload[other_allele].getPair();
  
   // Now check for pair and search for the right 'other'
        
        if (target_pair == 0){ //type 20
                while(other_pair != 0 )
                {
                    other = a_generator.nextInt(a_genes.length);
                    other_allele = (Integer)a_genes[other].getAllele();
                    if ( payload[other_allele] == null ){ continue; }
                    else { other_pair = payload[other_allele].getPair(); }
                }            
        }
        else if(target_pair == 1){ //type 40
               while(other_pair != 1 || chkonepair(other))
                {
                    other = a_generator.nextInt(a_genes.length);
                    other_allele = (Integer)a_genes[other].getAllele();     
                    
                    if ( payload[other_allele] != null ) 
                    {
                        continue; 
                    
                    }
                    if ( payload[other_allele + 64].getPair()!= 1) {
                        continue;
                    } 
                    if ( chkonepair(other)) {
                        continue;
                    }
                    
                    other_pair = payload[other_allele + 64].getPair();
                }
        }
        else{ //type 40
                while(other_pair != 2 || chktwopair(other))
                {
                    other = a_generator.nextInt(a_genes.length);
                    other_allele = (Integer)a_genes[other].getAllele();     
                    
                    if ( payload[other_allele] != null ) continue; 
                    if ( payload[other_allele + 64].getPair()!= 2) continue; 
                    if ( chktwopair(other)) continue;
                    
                    other_pair = payload[other_allele + 64].getPair();
                }
        }
        
    // Equal pair identified
    
    if ( other_pair!= 0 ){
    // type 40 identified
        if ( other_pair == 1){
           // swap first gene for the pair
                if (chkonepair(a_target_gene) || chkonepair(other) ){
                     System.out.println("something wrong here.. Mutation 40 pair1");
                  }
            t = a_genes[a_target_gene];
            a_genes[a_target_gene] = a_genes[other];
            a_genes[other] = t;

           // swap second gene for the pair
            t = a_genes[a_target_gene+1];
            a_genes[a_target_gene+1] = a_genes[other+1];
            a_genes[other+1] = t;
       }
      else if ( other_pair == 2){
           // swap first gene for the pair
               if (chktwopair(a_target_gene) || chktwopair(other) ){
                     System.out.println("something wrong here.. Mutation 40 pair2");
                  }
            t = a_genes[a_target_gene];
            a_genes[a_target_gene] = a_genes[other];
            a_genes[other] = t;
            t = a_genes[a_target_gene-1];
            // swap second gene for the pair
         try{
            
            a_genes[a_target_gene-1] = a_genes[other-1];
            }
            catch(ArrayIndexOutOfBoundsException e){
                System.out.println(":) :ArrayIndexOutOfBoundsException: (:");
            }
            a_genes[other-1] = t;
       }
        
      else{
            System.out.println("Invalid Configuration Exception...in Mutation");
      }
    }
    //The most often case of 20ft container
    else{
        
        t = a_genes[a_target_gene];
        a_genes[a_target_gene] = a_genes[other];
        a_genes[other] = t;
    }
    
    return a_genes;
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