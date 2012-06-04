/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package loaddistribution;

import java.util.ArrayList;
import java.util.List;
import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.IGeneConstraintChecker;
import org.jgap.impl.IntegerGene;

/**
 *
 * @author Muthukumar C
 */
public class Constraint implements IGeneConstraintChecker{
    
    private ShipContainer[] payload;
    private List<Integer> mark = new ArrayList<Integer>();
    private boolean callFromChromo;
    int count = 0;

    public Constraint(ShipContainer[] payload) {
        this.payload = payload;
        callFromChromo = false;
        initMark();
    }
    public void setCall(boolean callFromChromo){
        this.callFromChromo = callFromChromo;
    }
    void initMark(){
         for (int i=0; i<64; i++){
             mark.add(i);
         }
    }
    
    boolean isInValid(Integer allele){
        if(allele == null ){
        return true;
        }
        if ( allele>=0 && allele < 64)
        {return false;}
        
        return true;
    }
    
    @Override
    public boolean verify(Gene a_gene, Object a_alleleValue, IChromosome a_chromosome, int a_geneIndex) {
         //System.out.println("Verify");
         
        //if (a_alleleValue == null) {
          //  return true;
        //}

        Integer allele = (Integer) a_alleleValue;
        
        if (((isInValid(allele) && a_chromosome!=null) || (a_geneIndex ==  -1 && isInValid(allele))) && (!callFromChromo)) {
            return true;
        }
        
   
        //call from setAllele while initializing genes. catch alleles until all 64
        if (mark.contains(allele)){
            mark.remove(allele);
            count++;
            if(count == 64){
               initMark();
               count = 0;
            }
        }
        else{
            //return false;
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
