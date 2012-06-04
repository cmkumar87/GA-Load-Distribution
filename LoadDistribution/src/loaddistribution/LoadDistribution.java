package loaddistribution;
import java.io.*;
import java.util.*;
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
    private IChromosome fittest;
    private CrossoverOperator crossover;
    private OrderCrossover ordercrossover;
    private SwappingMutationOperator swapper;
    private SwapMutation swapmutation;
    private CrossoverRateCalculator crossRateCalc;
    private MutationRateCalculator muteRateCalc;
    private IGeneConstraintChecker lightWeightConstraint;

    //User input data
    private String inputFile;
    private int numEvolutions; // now hardcoded 50
    private int initialPopulation = 50; // now hardcoded 50 
    private double payloadMaxWeight; 
    private int mutationRate = 10; // now hardcoded 10
    private int crossRate = 6;  // now hardcoded 6
    private boolean adaptive = true; // now hardcoded as false
    
    // Output data to screens
    private int[] generation; 
    private double[] bestfitness;
    private ShipContainer[][] outputs = new ShipContainer[16][4];
    private int[][] crossoverArray;
    private int[][] mutationArray ;

    public int[][] getCrossoverArray() {
        return crossoverArray;
    }

    public int[][] getMutationArray() {
        return mutationArray;
    }
    

    
   // Getters
    
    public ShipContainer[][] getOutput() {
        return outputs;
    }

    public double[] getFittest() {
        return bestfitness;
    }
    
    public int getCounter20() {
        return counter20;
    }

    public int getCounter40() {
        return counter40;
    }

    public double getCrossRate() {
        return crossRate;
    }

    public int[] getGeneration() {
        return generation;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public int getNumEvolutions() {
        return numEvolutions;
    }

    public ShipContainer[] getPayload() {
        return payload;
    }

    public double getPayloadMaxWeight() {
        return payloadMaxWeight;
    }

    public boolean isAdaptive() {
        return adaptive;
    }

    public int getInitialPopulation() {
        return initialPopulation;
    }

    
    // Setters
    public void setAdaptive(boolean adaptive) {
        this.adaptive = adaptive;
    }

    public void setCrossRate(int crossRate) {
        this.crossRate = crossRate;
    }

    public void setMutationRate(int mutationRate) {
        this.mutationRate = mutationRate;
    }

    public void setNumEvolutions(int numEvolutions) {
        this.numEvolutions = numEvolutions;
        generation = new int[numEvolutions];
        bestfitness = new double[numEvolutions];
        crossoverArray = new int[numEvolutions][initialPopulation];
        mutationArray = new int[numEvolutions][initialPopulation];
    }

    public void setPayloadMaxWeight(double payloadMaxWeight) {
        this.payloadMaxWeight = payloadMaxWeight;
    }

    public void setInitialPopulation(int initialPopulation) {
        this.initialPopulation = initialPopulation;
    }
   
    

    //other intermediate variables
    private double indFitness;
    private double genotypeAvgFitness;
    private double maxGenotypeFitness;
    private double fitnessDelta;
    private int counter20 =0, counter40 =0;
    private ShipContainer[] bufferfor40;

    //File and external stuff variables
    String logFile = "C:\\temp\\1.log";
    FileWriter file = new FileWriter(logFile,true);
    BufferedWriter output = new BufferedWriter(file);
    IEvolutionMonitor monitor;
      
    public LoadDistribution(String inputFile) throws Exception{
      this.inputFile = inputFile;      
      initPayload();
      lightWeightConstraint = new Constraint(payload);
    }

    public void startGA () throws Exception{
        Genotype genotype = configureJGAP();
        //setupMonitor();
        doEvolution(genotype);
    }
        
    public void initPayload() throws FileNotFoundException,IOException{
    // instantiate payload    
    payload = new ShipContainer[64+64];
    
    // read payload data from excel spreadsheet
    readExcelFile(inputFile);
    writer("20ft containers loaded"+counter20);
    writer("40ft containers loaded"+counter40);
    for (int i = 0; i < (counter20 + (2 * counter40)); i++) {
        writer("Payload at Position: "+i);
        if (payload[i] != null){
            writer("Container: "+payload[i].getContainerID()+
                    " of type: "+payload[i].getType()+ "ft"+
                    " with Weight: "+payload[i].getWeight()
                    +" Pair "+payload[i].getPair());
        }
        else{
            writer("Container: "+payload[64+i].getContainerID()+
                    " of type: "+payload[64+i].getType()+ "ft"+
                    " with Weight: "+payload[64+i].getWeight()
                    +" Pair "+payload[64+i].getPair());
            
        }
    }
    
  }

 
    protected final Genotype configureJGAP()
            throws Exception
    {
    writer("-------------configure JGAP-----------");
    
    Configuration gaConf = new DefaultConfiguration();
    gaConf.reset();
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
    gaConf.setPopulationSize(initialPopulation);
    
    //writer("numEvolutions "+ numEvolutions);
    
    Genotype genotype = null;
    try {   
     
      // Setup the structure with which to evolve the
      // solution of the problem.
      // Sample Chromosome for the genotype
      // --------------------------------------------
      // application Constraints
      writer("Setting Constraint Checker for gene");
      Gene sampleGene = new IntegerGene(gaConf);
      sampleGene.setConstraintChecker(lightWeightConstraint);
      
      writer("Sample Chromosome Initialisation");
      IChromosome sampleChromosome = new MyChromosome(gaConf,sampleGene,64);
      writer("Setting Constraint Checker for Chromosome");
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
      //Swapping operator instead of normal mutation
      //Swapper Op with muterate rate
      
      if(adaptive){
          swapmutation = new SwapMutation(gaConf,muteRateCalc,payload);
      }
      else{
          swapmutation = new SwapMutation(gaConf,mutationRate,payload);
      }
      
      //swapper.setMutationRate(integer);
      gaConf.addGeneticOperator(swapmutation);
      System.out.println("Mutation Rate: "+swapmutation.getMutationRate());
      
      
      // Define crossover op with crossover rate
      if(adaptive){
            ordercrossover = new OrderCrossover(gaConf, crossRateCalc, payload);
      }
      else{
            ordercrossover = new OrderCrossover(gaConf,crossRate, payload);
      }
      
      gaConf.addGeneticOperator(ordercrossover);
      System.out.println("Crossover Rate: "+ordercrossover.getCrossOverRate());
      
      //Initialise genotype with random population
      
      genotype = Genotype.randomInitialGenotype(gaConf);

      // Initialize all chromosomes in the genotype
      // each number from 1..64
      // is represented by exactly one gene.
      // 
      RandomGenerator generator = gaConf.getRandomGenerator();
      writer("Populating randomly initialized chromosome population.");
      
      int nextInt=0;
      IntegerGene gene;
      List<IChromosome> chromosomes = genotype.getPopulation().getChromosomes();
      for (int i = 0; i < chromosomes.size(); i++) {
        IChromosome chrom = (IChromosome) chromosomes.get(i);
        //assign integers 0 to 63 to all alleles
        for (int j = 0; j < chrom.size(); j++) { 
          nextInt = generator.nextInt(64);
            if (!checkAlleles(chrom,j,nextInt)){
                    if (case40(nextInt,j)){
                        if (payload[64+nextInt].getPair()== 1){
                            if (chkonepair(j)){j--; continue;};
                            gene = (IntegerGene) chrom.getGene(j);
                            gene.setAllele(new Integer(nextInt));
                            
                            gene = (IntegerGene) chrom.getGene(j+1);
                            gene.setAllele(new Integer(nextInt+1));
                        }
                        else if (payload[64+nextInt].getPair()== 2){
                            if (chkonepair(j)){j--; continue;};
                            gene = (IntegerGene) chrom.getGene(j);
                            gene.setAllele(new Integer(nextInt-1));
                            gene = (IntegerGene) chrom.getGene(j+1);
                            gene.setAllele(new Integer(nextInt));
                        }
                        else{
                            writer("...InvalidConfigurationException....");
                        }
                        j++;
                    }
                    else{
                        gene = (IntegerGene) chrom.getGene(j);
                        gene.setAllele(new Integer(nextInt));
                    }         
                }
        else{
                j--;
            }       
          
      }
    }
   }
    catch (InvalidConfigurationException e) {
      e.printStackTrace();
      System.exit( -2);
    }
    return genotype;
   // GA Configurations ends
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
  
  boolean case40(int nextInt, int j){
      if (payload[nextInt]== null && j != 63){
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
    
  // do Evolution of the chromosome population (Genotype)
    
    public void doEvolution(Genotype a_genotype) throws FileNotFoundException,IOException{      
    writer("--doEvolution--");
       
    // Evolution loop for numEvolution
    for (int i = 0; i < numEvolutions; i++) {
        // Configuring Rate Calculater for Dynamic rate calculation
        writer("Configuring Dynamic rate params...");
        configRateCalc(a_genotype);
        // Evolving current population
        generation[i] = a_genotype.getConfiguration().getGenerationNr();
        writer("Evolving neXt-Gen!! to: "+generation[i]);
        a_genotype.evolve();
        
        crossoverArray[i] = ordercrossover.getCrossoverValues();
        mutationArray[i] = swapmutation.getMutationValues();
        
        bestfitness[i] = a_genotype.getFittestChromosome().getFitnessValue();
        writer("Evolution happened with rates..");
        //Printout current Rates
        printRates(a_genotype);
        writer("Evolution complete.");
     }
    
    // Print summary.
    // --------------
    fittest = a_genotype.getFittestChromosome();
    writer("Best solution has fitness " +
                       fittest.getFitnessValue());
    printSolution(fittest);
  }
  
  // doEvolution ends
  // Lightest good son top
   public int[] ReorderWeights(Map rows) throws FileNotFoundException,IOException{
       double jwt=0 ,iwt=0, tempweight;
       int temp = 0;
       int[] order = {0,1,2,3};
       //print out final result
       for(int i=0 ; i<4;i++){
           for (int j=i+1;j<4;j++){
               jwt = (Double)rows.get(j); //wt row j
               iwt = (Double)rows.get(i); //wt row i
               if ( jwt < iwt){ // if j is less than i
                   //change order
                   temp = order[i];
                   order[i] =order[j];
                   order[j] = temp;
                   //exchange weights
                   tempweight = (Double)rows.get(i);
                   rows.put(i, rows.get(j));
                   rows.put(j,tempweight);
               }
           }
       }
       for (int x =0; x < order.length; x++ ){
            writer("Final row order"+order[x]);
       }
       return order;
  } 
    
    
  // Print the Best Chromosome found to be the best solution by GA
    ShipContainer[][] containerstack = new ShipContainer[16][4];
    public void printSolution(IChromosome a_solution) throws FileNotFoundException,IOException{
    int row = 0;
    double rowweight = 0.0d;
    int[][] sortedRowOrder = new int[4][4];
    Map rows = new HashMap(); 
    ShipContainer[][] groupwise = new ShipContainer[16][4];
    ShipContainer onecontainer = null;
    int group;
    for (int j=0 ;j<64;j+=16){
     row = 0;
        for (int i=j;i<j+16;i+=4){
            rowweight = 0;
            for (int k=i; k<i+4; k++){
                onecontainer = null;
                Integer myallele = (Integer)a_solution.getGene(k).getAllele();
                        if (payload[myallele] != null){
                                onecontainer = payload[myallele];
                        }
                        else{
                                myallele = 64 + myallele;
                                onecontainer = payload[myallele];
                        }
                rowweight += onecontainer.getWeight();
                            writer("Container "+onecontainer.getContainerID()
                                    +" of type "+onecontainer.getType()
                                    +" with weight  "+onecontainer.getWeight()
                                    +" is in Row  "+row+" of the Ship"
                                    +" group "+getGroup(k)
                                    +" Pair "+onecontainer.getPair());
                            groupwise[getGroup(k)][row]= onecontainer;
             }
            
                rows.put(row, rowweight);
                row++;
        }
        
        sortedRowOrder[j/16] = ReorderWeights(rows);
             
     //for(int pos = j;pos < pos+16 ;pos++){   
     //}
   }
    
    // For Panel groups 0,1,2,3
    for (int x=0;x<4;x++){
       System.out.println("Group #"+x);
        for (int y=0;y<4;y++){
            System.out.print("ContainerID "+groupwise[x][sortedRowOrder[0][y]].getContainerID());
            System.out.println(" Weight "+groupwise[x][sortedRowOrder[0][y]].getWeight());
            outputs [x][y] = groupwise[x][sortedRowOrder[0][y]];
        }
    }
    
    // For Panel groups 4,5,6,7
    for (int x=4;x<8;x++){
       System.out.println("Group #"+x);
        for (int y=0;y<4;y++){
            System.out.print("ContainerID "+groupwise[x][sortedRowOrder[1][y]].getContainerID());
            System.out.println(" Weight "+groupwise[x][sortedRowOrder[1][y]].getWeight());
            outputs [x][y] = groupwise[x][sortedRowOrder[1][y]];
        }
    }
    
    // For Panel groups 8,9,10,11
    for (int x=8;x<12;x++){
      System.out.println("Group #"+x); 
        for (int y=0;y<4;y++){
            System.out.print("ContainerID "+groupwise[x][sortedRowOrder[2][y]].getContainerID());
            System.out.println(" Weight "+groupwise[x][sortedRowOrder[1][y]].getWeight());
            outputs [x][y] = groupwise[x][sortedRowOrder[2][y]];
        }
    }
    
    // For Panel groups 12,13,14,15
    for (int x=12;x<16;x++){
      System.out.println("Group #"+x);  
        for (int y=0;y<4;y++){
            System.out.print("ContainerID "+groupwise[x][sortedRowOrder[3][y]].getContainerID());
            System.out.println(" Weight "+groupwise[x][sortedRowOrder[1][y]].getWeight());
            outputs [x][y] = groupwise[x][sortedRowOrder[3][y]];
        }
    }
    
    for(int gen=0;gen<numEvolutions;gen++){
            System.out.print("Generations "+generation[gen]);
            writer(" :Fitnesses: "+bestfitness[gen]);
            for (int i=0;i<initialPopulation;i++)
            {
                System.out.print("Crossover for chromosome "+crossoverArray[gen][i]);
                System.out.print("Mutation for chromosome "+mutationArray[gen][i]);
            }
    }

 }
    
    public int getGroup(int position){
        int group = 0;
        int[][] groups = {{0,7,8,15},{1,6,9,14},{2,5,10,13},{3,4,11,12},
                          {16,23,24,31},{17,22,25,30},{18,21,26,29},{19,20,27,28},
                          {32,39,40,47},{33,38,41,46},{34,37,42,45},{35,36,43,44},
                          {48,55,56,63},{49,54,57,62},{50,53,58,61},{51,52,59,60}};
        
        for (int i = 0 ; i<16 ;i++){
            for (int j=0;j<4;j++){
            if (Arrays.asList(groups[i][j]).contains(position)){
                group = i; break;
            }
          }
        }
        return group;
    }
  
    
    // Adaptive GA Config
    public void configRateCalc(Genotype genotype)throws FileNotFoundException,IOException{
     // Finding MaxFitness in the Population
      IChromosome   best = genotype.getFittestChromosome();
      maxGenotypeFitness = best.getFitnessValue();
      
     // Finding Average Fitness of every population
      int sumFitness = 0;
      double[] indFitness = new double[65];
      List<IChromosome> population = genotype.getPopulation().getChromosomes();
      for (IChromosome chromosome:population){
          sumFitness += chromosome.getFitnessValue();
      }
      
        genotypeAvgFitness = sumFitness/50;
       // writer("Average Fitness of the Population: "+ genotypeAvgFitness);
        
        //fitnessDelta between avgFitness and maxFitness
        fitnessDelta = genotypeAvgFitness - maxGenotypeFitness;
       // writer("fitnessDelta: "+fitnessDelta);
        
        //Set values to Rate calculator
        crossRateCalc.setGenotypeAvgFitness(genotypeAvgFitness);
        crossRateCalc.setMaxGenotypeFitness(maxGenotypeFitness);
        //Set values to Mutation Rate calculator
        muteRateCalc.setFitnessDelta(fitnessDelta);
        muteRateCalc.setGenotypeAvgFitness(genotypeAvgFitness);
        muteRateCalc.setMaxGenotypeFitness(maxGenotypeFitness);
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
        int i=0,type=0,containerID=0,buf=0;
        List fragments = new ArrayList();
        bufferfor40 = new ShipContainer[64];
        double weight=0.0d;
       
          while(rowIter.hasNext() && i<=64){
              HSSFRow myRow = (HSSFRow) rowIter.next();
              Iterator cellIter = myRow.cellIterator();
              
              while(cellIter.hasNext() ){

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
                            System.out.println ("Type not supported."+i);
                            break;
                        }
                    }
              }
              
            if(i!=0){
                        switch(type){
                            case 20:
                                container = new Standard20ft();
                                container.setContainerID(containerID);
                                container.setWeight(weight);
                                if (!fragments.isEmpty()){
                                    int temp = (Integer)fragments.get(fragments.size()-1);
                                    fragments.remove(fragments.size()-1);
                                    payload[temp-1] = container;
                                    
                                    //allocate buffered 40 to current and next
                                    payload[i-1] = null;
                                    payload[i] = null;
                                    payload[64+i-1] = bufferfor40[buf];
                                    
                                    container = new Standard40ft();
                                    container.setContainerID(bufferfor40[buf].getContainerID());
                                    container.setWeight(bufferfor40[buf].getWeight());
                                    container.pair= 2;
                                    payload[64+i] = container;
                                    bufferfor40[buf] = null; buf --;
                                    counter20++;
                                    i+=2;
                                    continue;
                                }                                
                                payload[i-1] = container;
                                counter20++;
                             break;
                                
                            case 40:
                                container = new Standard40ft();
                                container.setContainerID(containerID);
                                container.setWeight(weight/2);
                                container.pair = 1;                              
                                if ((i % 4) ==0)
                                   { buf++;
                                     bufferfor40[buf] = container;
                                     counter40++;
                                     fragments.add(i);
                                     i++;
                                     continue;
                                   }
                                
                                payload[64+i-1] = container;
                                payload[i-1] = null;
                                counter40++;
                                
                                container = new Standard40ft();
                                container.setContainerID(containerID);
                                container.setWeight(weight/2);
                                container.pair = 2;
                                payload[64+i] = container;
                                payload[i] = null;
                                i++;
                             break;
                                
                            case 10:
                                //container = new Standard10ft();
                             break;        
                        }

            }
              i++;
          }
          while(!fragments.isEmpty()){
              writer("The Payload is fragmented by empty space# "+fragments.get(fragments.size()-1));
              fragments.remove(fragments.size()-1);
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
            LoadDistribution ld = new LoadDistribution("C:\\temp\\test.xls");
            ld.setNumEvolutions(50);
            System.out.println("Num evolutions.."+ld.getNumEvolutions());
            ld.startGA();
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
                s = (SwapMutation)op;
                System.out.println("Mutation Rate: "+s.getMutationRate());
            }
            else{
                writer("Unknown Operator");
            }
        }
    }
    
    
}