/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loaddistribution;

import org.jgap.Configuration;
import org.jgap.Genotype;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultCrossoverRateCalculator;

/**
 *
 * @author Muthukumar C
 */
public class MutationRateCalculator extends DefaultCrossoverRateCalculator{
    
  public MutationRateCalculator(Configuration a_config)
      throws InvalidConfigurationException {
        super(a_config);
  }
    
  public int calculateCurrentRate() {
    int size = getConfiguration().getChromosomeSize();
    
    if (size < 1) {
      size = 1;
    }
    return size;
  }
}
