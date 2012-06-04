/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loaddistribution;

import org.jgap.*;

/**
 *
 * @author Muthukumar C
 */
public class MyChromosome extends Chromosome {

    public MyChromosome() throws InvalidConfigurationException {
        super();
    }
    
    public MyChromosome(Configuration a_configuration,Gene a_sampleGene,int a_desiredSize)
     throws InvalidConfigurationException {
        super(a_configuration,a_sampleGene,a_desiredSize);
    }
    
  @Override
    public void setGenes(Gene[] a_genes)
      throws InvalidConfigurationException {
    super.setGenes(a_genes);
    //Registry reg = new Registry(true);
    if (getConstraintChecker() != null){
        Constraint c = (Constraint) getConstraintChecker();
        c.setCall(true);
    }
    verify(getConstraintChecker());   
  }
    
}
