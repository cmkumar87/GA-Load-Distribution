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
import org.jgap.audit.ChainedMonitors;
import org.jgap.audit.FitnessImprovementMonitor;
import org.jgap.audit.IEvolutionMonitor;
import org.jgap.audit.TimedMonitor;
//------------------------------------------------------


/**
 *
 * @author A0092669M
 */
public class LoadDistribution {

    //Application specific class variables
    private ShipContainer container;
    private ShipContainer[] payload;

    // JGAP class variables
    private IChromosome chromosome; 
    private CrossoverOperator crossover;
    private SwappingMutationOperator swapper;
    private CrossoverRateCalculator crossRateCalc;
    private MutationRateCalculator muteRateCalc;
    private Constraint lightWeightConstraint;

    //User input data
    private int numEvolutions;
    private double payloadMaxWeight;
    private String inputFile;

    //other intermediate variables
    private boolean adaptive = false;
    private double indFitness;
    private double genotypeAvgFitness;
    private double maxGenotypeFitness;
    private double fitnessDelta;

    //File and external stuff variables
    String logFile = "C:\\temp\\1.log";
    FileWriter file = new FileWriter(logFile,true);
    BufferedWriter output = new BufferedWriter(file);
    IEvolutionMonitor monitor;
      
    public LoadDistribution(String inputFile, String configFile) throws Exception{
      this.inputFile = inputFile;
      initPayload();
      lightWeightConstraint = new Constraint(payload);
      Genotype genotype = configureJGAP();
      //setupMonitor();
      doEvolution(genotype);
    }

    
    public void initPayload() throws FileNotFoundException,IOException{
    // instantiate payload    
    payload = new ShipContainer[64];
    
    // read payload data from excel spreadsheet
    readExcelFile(inputFile);
    for (int i = 0; i < payload.length; i++) {
      writer("Container: "+payload[i].getContainerID()+
              " of type: "+payload[i].getType()+ "ft"+
              " with Weight: "+payload[i].getWeight()
              );
    }
  }

 
    protected final Genotype configureJGAP()
            throws Exception
    {
    writer("-------------configure JGAP-----------");
    numEvolutions = 2;
    Configuration gaConf = new DefaultConfiguration();
    gaConf.resetProperty(Configuration.PROPERTY_FITEVAL_INST);
    gaConf.setFitnessEvaluator(new DeltaFitnessEvaluator());
    
    // Setup some other parameters.
    // --------------------------------------------------
    gaConf.setPreservFittestIndividual(true);
    //constant population size
    gaConf.setKeepPopulationSizeConstant(true);
    // Set number of individuals per generation.
    // --------------------------------------------------
    // get from user
    gaConf.setPopulationSize(50);
    
    writer("numEvolutions "+ numEvolutions);
    
    int chromeSize = payload.length;
    Genotype genotype = null;
    try {   
     
      // Setup the structure with which to evolve the
      // solution of the problem.
      // Sample Chromosome for the genotype
      // --------------------------------------------
      // application Constraints
      Gene sampleGene = new IntegerGene(gaConf);
      sampleGene.setConstraintChecker(lightWeightConstraint);
  
      IChromosome sampleChromosome = new Chromosome(gaConf,sampleGene,chromeSize);
      sampleChromosome.setConstraintChecker(lightWeightConstraint);
      System.out.println("-----------------------------------------------------");
      // IChromosome sampleChromosome = new Chromosome(gaConf, new IntegerGene(gaConf),chromeSize);  
      // Add sample chromosome to Config
      gaConf.setSampleChromosome(sampleChromosome);

      /********* SET FITNESS FUNCTION******/
      gaConf.setFitnessFunction(new EqualWeightsFitnessFunction(payload));
      
       //Create Rate Calculators for Operators
       muteRateCalc = new MutationRateCalculator(gaConf);
       crossRateCalc = new CrossoverRateCalculator(gaConf);
     
      //Add Genetic Operators to config
      gaConf.getGeneticOperators().clear();
    
 
      // Operators applied in the order in which they are added
      // Define crossover op with crossover rate
      if(adaptive){
      crossover = new CrossoverOperator(gaConf,crossRateCalc);
      }
      else{
      crossover = new CrossoverOperator(gaConf,10);
      }
      
      gaConf.addGeneticOperator(crossover);
      System.out.println(crossover.getCrossOverRate());
      
      //Swapping operator instead of mutation
      //Swapper Op with muterate rate
      
      if(adaptive){
          swapper = new SwappingMutationOperator(gaConf,muteRateCalc);
      }
      else{
          swapper = new SwappingMutationOperator(gaConf,10);
      }
      
      //swapper.setMutationRate(integer);
      gaConf.addGeneticOperator(swapper);
      System.out.println(swapper.getMutationRate());

      //Initialise genotype with random population
      genotype = Genotype.randomInitialGenotype(gaConf);

      // Initialize all chromosomes in the genotype
      // each number from 1..64
      // is represented by exactly one gene.
      // --------------------------------------------------------
      writer("Populating randomly initialized chromosome population.");
      List<IChromosome> chromosomes = genotype.getPopulation().getChromosomes();
      for (int i = 0; i < chromosomes.size(); i++) {
        IChromosome chrom = (IChromosome) chromosomes.get(i);
        for (int j = 0; j < chrom.size(); j++) {
          IntegerGene gene = (IntegerGene) chrom.getGene(j);
          gene.setAllele(new Integer(j));
          System.out.println("Constraint Checker "+gene.getConstraintChecker());
        }
      }
    } catch (InvalidConfigurationException e) {
      e.printStackTrace();
      System.exit( -2);
    }
    return genotype;
   // GA Configurations ends
  }
    
  // do Evolution of the chromosome population (Genotype)
    
    public void doEvolution(Genotype a_genotype) throws FileNotFoundException,IOException{      
    writer("--doEvolution--");
    int progress = 0;
    int percentEvolution = numEvolutions / 100;
        
    // Evolution loop for numEvolution
    for (int i = 0; i < numEvolutions; i++) {
        // Configuring Rate Calculater for Dynamic rate calculation
        writer("Configuring Dynamic rate params...");
        configRateCalc(a_genotype);
        // Evolving current population
        writer("Evolving neXt-Gen!!");
        a_genotype.evolve();
        /*List<String> messages = a_genotype.evolve(monitor);
        if (messages.size() > 0) {
            for (String msg : messages) {
             System.out.println("Message from monitor: " + msg+"\n");
             }
        }*/
        writer("Evolution happened with rates..");
        //Printout current Rates
        printRates(a_genotype);
        writer("Evolution complete.");
     }
 
    // Print summary.
    // --------------
    IChromosome fittest = a_genotype.getFittestChromosome();
    writer("Best solution has fitness " +
                       fittest.getFitnessValue());
    printSolution(fittest);
  }
  
   // doEvolution ends
  
  // Print the Best Chromosome found to be the best solution by GA
    
    public void printSolution(IChromosome a_solution) throws FileNotFoundException,IOException{
    double groupWeights = 0.0d;
    for (int i = 0; i < 16; i++) {
      writer("\nGroup " + i);
      writer("-------");
      double groupWeight = 0.0d;
      for (int j = 0; j < 4; j++) {
        IntegerGene containerID = (IntegerGene) a_solution.getGene( (i * 4 + j));
        ShipContainer container = (ShipContainer) payload[containerID.intValue()];
        double weight = container.getWeight();
        groupWeight += weight;
        writer(" Container with id "
                           + containerID.intValue()
                           + " with weight "
                           + weight);
      }
      groupWeights += groupWeight;
      writer("  --> Group weight: " + groupWeight);
    }
    writer("\n Average group weight: " + groupWeights / 16);
    
  }
    
    // Adaptive GA Config
    public void configRateCalc(Genotype genotype)throws FileNotFoundException,IOException{
     // Finding MaxFitness in the Population
      IChromosome   best = genotype.getFittestChromosome();
      maxGenotypeFitness = best.getFitnessValue();
      
     // Finding Average Fitness of every population
      int sumFitness = 0,count=0;
      double[] indFitness = new double[65];
      List<IChromosome> population = genotype.getPopulation().getChromosomes();
      for (IChromosome chromosome:population){
          indFitness[count] = chromosome.getFitnessValue();
          sumFitness += chromosome.getFitnessValue();
          writer("indFitness: "+indFitness[count]);
          count++;
      }
      
        genotypeAvgFitness = sumFitness/50;
        writer("Average Fitness of the Population: "+ genotypeAvgFitness);
        
        //fitnessDelta between avgFitness and maxFitness
        fitnessDelta = genotypeAvgFitness - maxGenotypeFitness;
        writer("fitnessDelta: "+fitnessDelta);
        
        //fitnessDelta between avgFitness and indFitness
        crossRateCalc.setFitnessDelta(fitnessDelta);
        crossRateCalc.setGenotypeAvgFitness(genotypeAvgFitness);
        crossRateCalc.setIndFitness(indFitness);
    }
    
    public void setupMonitor(){
        List monitors = new Vector();
        monitors.add(new TimedMonitor(6));
        monitors.add(new FitnessImprovementMonitor(1, 3, 5.0d));
        monitor = new ChainedMonitors(monitors, 2);
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
                            // types other
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
                payload[i-1] = container;
            }
              i++;
          }
        }catch (Exception e){e.printStackTrace(); }
        //return cellVectorHolder;
    }
    
  public void writer(String str) throws FileNotFoundException,IOException{
      System.out.println(str);
      output.write(str);
  }
  
    public static void main(String[] args) {
        try {
            System.out.println("Excution Starts....");
            new LoadDistribution("C:\\temp\\test.xls", "C:\\temp\\params.cfg");
            System.out.println("....Done.....");
        } catch (Exception e) {
            System.out.println("Errored out in main");
            e.printStackTrace();
        }
        
    }
    
    public void printRates(Genotype a_genotype)throws FileNotFoundException,IOException{
        CrossoverOperator c = null;
        SwappingMutationOperator s = null;
        List<BaseGeneticOperator> genops= a_genotype.getConfiguration().getGeneticOperators();
        for(BaseGeneticOperator op:genops){
            if(op instanceof CrossoverOperator){
                c = (CrossoverOperator)op;
                System.out.println("Static CrossOver Rate: "+c.getCrossOverRate());
            }
            else if(op instanceof SwappingMutationOperator){
                s = (SwappingMutationOperator)op;
                System.out.println("Mutation Rate: "+s.getMutationRate());
            }
            else{
                writer("Unknown Operator");
            }
        }
        System.out.println("--From printRates: "+ a_genotype.getFittestChromosome().getFitnessValue());
    }
}