/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loaddistribution;

import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.IGeneConstraintChecker;

/**
 *
 * @author Muthukumar C
 */
public class Constraint implements IGeneConstraintChecker{
    
    private ShipContainer[] payload;

    public Constraint(ShipContainer[] payload) {
        this.payload = payload;
    }
    
    @Override
    public boolean verify(Gene a_gene, Object a_alleleValue, IChromosome a_chromosome, int a_geneIndex) {
        Integer i = (Integer) a_alleleValue;
        
    if (a_alleleValue == null) {
        return true;
    }
    
    switch (a_geneIndex % 4) {
            case 1:
            // Stack 3
            /*for (int i=0;i<2;i++){
                if (payload[a_geneIndex - i].getWeight())
            }*/
                   System.out.println("Stack: "+a_geneIndex+" -- "+(a_geneIndex % 4));
            break;
            case 2:
            // Stack 2
                System.out.println("Stack: "+a_geneIndex+" -- "+(a_geneIndex % 4));
            break;
            case 3:
            // Stack 1
                System.out.println("Stack: "+a_geneIndex+" -- "+(a_geneIndex % 4));
            break;
            case 0:
            // Stack 4
                System.out.println("Stack: "+a_geneIndex+" -- "+(a_geneIndex % 4));
            break;
    }
    return true;
    }
}
