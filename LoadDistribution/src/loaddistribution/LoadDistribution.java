package loaddistribution;
import java.io.*;
import java.util.List;
import org.jgap.*;
import org.jgap.impl.CrossoverOperator;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.SwappingMutationOperator;
//------------------------------------------------------
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
//------------------------------------------------------
import java.util.Vector;
import java.util.Iterator;
import org.apache.poi.hssf.usermodel.*;
//------------------------------------------------------


/**
 *
 * @author A0092669M
 */
public class LoadDistribution {
private ShipContainer container;
private ShipContainer[] payload;
private IChromosome chromosome; 
//User input data
private int numEvolutions;
private double payloadMaxWeight; 
private String inputFile;
private double genotypeAvgFitness;
    public LoadDistribution(String inputFile) throws Exception{
      this.inputFile = inputFile;
      initPayload();
      Genotype genotype = configureJGAP();
      doEvolution(genotype);
    }

  // need modification all 64 can't be of one type
  public void initPayload() throws FileNotFoundException,IOException{
    payload = new ShipContainer[65];
    // read from excel avg container weights
    readExcelFile(inputFile);
    for (int i = 1; i < payload.length; i++) {
      writer("Container: "+payload[i].getContainerID()+
              " of type: "+payload[i].getType()+ "ft"+
              " with Weight: "+payload[i].getWeight()
              );
    }
  }
  
  public void readExcelFile(String fileName) throws FileNotFoundException,IOException
    {
 
        try{
        /** Creating Input Stream**/
        //InputStream myInput= ReadExcelFile.class.getResourceAsStream( fileName );
        FileInputStream myInput = new FileInputStream(fileName);
 
        /** Create a POIFSFileSystem object**/
        POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
 
        /** Create a workbook using the File System**/
         HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
 
         /** Get the first sheet from workbook**/
        HSSFSheet mySheet = myWorkBook.getSheetAt(0);
 
        /** We now need something to iterate through the cells.**/
        Iterator rowIter = mySheet.rowIterator(); 
        int i=0,type=0,containerID=0;
        double weight=0.0d;
          while(rowIter.hasNext()){
              HSSFRow myRow = (HSSFRow) rowIter.next();
              Iterator cellIter = myRow.cellIterator();
              
              while(cellIter.hasNext()){
                  HSSFCell cell = (HSSFCell) cellIter.next();
                  switch (cell.getCellType ())
                    {
                        case HSSFCell.CELL_TYPE_NUMERIC :
                        {       
                            // cell type numeric.
                            if(cell.getColumnIndex() == 0){
                                containerID = (new Double
                                  (cell.getNumericCellValue())).intValue();
                            }
                            else if(cell.getColumnIndex() == 2){
                               type = (new Double
                                  (cell.getNumericCellValue())).intValue();
                            }
                            else{
                               weight = new Double(cell.getNumericCellValue());
                            }
                            break;
                        }
                        case HSSFCell.CELL_TYPE_STRING :
                        {
                            // cell type string.
                            HSSFRichTextString richTextString = cell.getRichStringCellValue ();
                            System.out.println ("String value: " + richTextString.getString ());
                            break;
                        }
                        default :
                        {
                            // types other Numeric.
                            System.out.println ("Type not supported.");
                            break;
                        }
                    }
              }
              
            if(i!=0){
                        switch(type){
                            case 20:
                                container = new Standard20ft();
                             break;
                            case 40:
                                container = new Standard40ft();
                             break;
                            case 10:
                                container = new Standard10ft();
                             break;        
                        }
                container.setContainerID(containerID);
                container.setWeight(weight);
                payload[i] = container;
            }
              i++;
          }
        }catch (Exception e){e.printStackTrace(); }
        //return cellVectorHolder;
    }
 
    protected final Genotype configureJGAP()
            throws Exception
    {
    System.out.println("--configure JGAP--");
    numEvolutions = 50;
    Configuration gaConf = new DefaultConfiguration();
    gaConf.resetProperty(Configuration.PROPERTY_FITEVAL_INST);
    gaConf.setFitnessEvaluator(new DeltaFitnessEvaluator());
    // Just use a swapping operator instead of mutation and others.
    // ------------------------------------------------------------
    gaConf.getGeneticOperators().clear();
    
    // Operators applied in the order in which they are added
    // Define crossover op with rossover rate
    CrossoverOperator crossover = new CrossoverOperator(gaConf,10);
    System.out.println(crossover.getCrossOverRate());
    gaConf.addGeneticOperator(crossover);
    
    SwappingMutationOperator swapper = new SwappingMutationOperator(gaConf);
    //swapper.setMutationRate(integer);
    gaConf.addGeneticOperator(swapper);
    System.out.println(swapper.getMutationRate());
    // Setup some other parameters.
    // ----------------------------
    gaConf.setPreservFittestIndividual(true);
    gaConf.setKeepPopulationSizeConstant(false);
    // Set number of individuals (=tries) per generation.
    // --------------------------------------------------
    gaConf.setPopulationSize(50);
    int chromeSize = payload.length;
    Genotype genotype = null;
    try {
      // Setup the structure with which to evolve the
      // solution of the problem.
      // --------------------------------------------
      IChromosome sampleChromosome = new Chromosome(gaConf,
          new IntegerGene(gaConf), chromeSize);
      gaConf.setSampleChromosome(sampleChromosome);

      /********* SET FITNESS FUNCTION******/
     gaConf.setFitnessFunction(new EqualWeightsFitnessFunction(payload));
      //
      genotype = Genotype.randomInitialGenotype(gaConf);
      // each number from 1..64
      // is represented by exactly one gene.
      // ---------------------------------------------------------
      List chromosomes = genotype.getPopulation().getChromosomes();
      for (int i = 0; i < chromosomes.size(); i++) {
        IChromosome chrom = (IChromosome) chromosomes.get(i);
        for (int j = 0; j < chrom.size(); j++) {
          Gene gene = (Gene) chrom.getGene(j);
          gene.setAllele(new Integer(j));
        }
      }
    } catch (InvalidConfigurationException e) {
      e.printStackTrace();
      System.exit( -2);
    }
    return genotype;
  }
    
  // do Evolution of the chromosome population 
    
    public void doEvolution(Genotype a_genotype) throws FileNotFoundException,IOException{      
    writer("--doEvolution--");
    int progress = 0;
    int percentEvolution = numEvolutions / 100;
        writer("numEvolutions "+numEvolutions);
    for (int i = 0; i < numEvolutions; i++) {
        a_genotype.evolve();
      // Print progress.
      // ---------------
      if (percentEvolution > 0 && i % percentEvolution == 0) {
          System.out.println("inside..");
        progress++;
        IChromosome fittest = a_genotype.getFittestChromosome();
        double fitness = fittest.getFitnessValue();
        System.out.println("Currently best solution has fitness " +
                           fitness);
        printSolution(fittest);
      }
      List<IChromosome> chromosomes = a_genotype.getPopulation().getChromosomes();
      int sumFitness = 0;
      for (IChromosome chromosome:chromosomes){
          sumFitness += chromosome.getFitnessValue();
      }
          genotypeAvgFitness = sumFitness/a_genotype.getPopulation().size();
          System.out.println("Population Size: "+a_genotype.getPopulation().size());
          System.out.println("Average Fitness of the Population: "+ genotypeAvgFitness);
          sumFitness = 0;
          chromosomes = null;
    }

    // Print summary.
    // --------------
    IChromosome fittest = a_genotype.getFittestChromosome();
    System.out.println("Best solution has fitness " +
                       fittest.getFitnessValue());
    printSolution(fittest);
  }
  
  // Print the Best Chromosome found to be the best solution by GA
    
    public void printSolution(IChromosome a_solution) throws FileNotFoundException,IOException{
    double groupWeights = 0.0d;
    for (int i = 0; i < 16; i++) {
      System.out.println("\nGroup " + i);
      System.out.println("-------");
      double groupWeight = 0.0d;
      for (int j = 1; j < 5; j++) {
        IntegerGene containerID = (IntegerGene) a_solution.getGene( (i * 4 + j));
        ShipContainer container = (ShipContainer) payload[containerID.intValue()];
        double weight = container.getWeight();
        groupWeight += weight;
        System.out.println(" Container with id "
                           + containerID.intValue()
                           + " with weight "
                           + weight);
      }
      groupWeights += groupWeight;
      System.out.println("  --> Group weight: " + groupWeight);
    }
    writer("\n Average group weight: " + groupWeights / 16);
  }
    
  public void writer(String str) throws FileNotFoundException,IOException{
      FileWriter fr = new FileWriter("C:\\temp\\1.log");
      Writer output = new BufferedWriter(fr);
      output.write(str);
  }
    public static void main(String[] args) {
        try {
            System.out.println("Excution Starts....");
            new LoadDistribution("C:\\temp\\test.xls");
            System.out.println("....Done.....");
        } catch (Exception e) {
            System.out.println("Errored out in main");
            e.printStackTrace();
        }
        
    }
}