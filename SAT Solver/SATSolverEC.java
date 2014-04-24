import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

class Literal{
    public int chemical;
    public int container;    
    
    Literal (int chemical, int container){
        this.chemical = chemical;
        this.container = container;        
    }
    
  

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Literal other = (Literal) obj;
        if (this.chemical != other.chemical) {
            return false;
        }
        return this.container == other.container;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.chemical;
        hash = 89 * hash + this.container;
        return hash;
    }
}

class ClauseLiteral{
    public Literal literal;
    public boolean isNegated;
    
    ClauseLiteral (Literal literal, boolean isNegated){
        this.literal = literal;
        this.isNegated = isNegated;        
    }
    

    public boolean isNegation(ClauseLiteral other){
        return this.literal.equals(other.literal) && this.isNegated != other.isNegated;
    }
    
    public ClauseLiteral getNegation(){
        ClauseLiteral Negation = new ClauseLiteral(this.literal, !this.isNegated);
        return Negation;        
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ClauseLiteral other = (ClauseLiteral) obj;
        if (!Objects.equals(this.literal, other.literal)) {
            return false;
        }
        return this.isNegated == other.isNegated;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.literal);
        hash = 67 * hash + (this.isNegated ? 1 : 0);
        return hash;
    }
    
}

class CNFClause{
    
    public Set<ClauseLiteral> Literals;
    
    CNFClause(){
        Literals = new HashSet<>();
    }

    public void addLiteral(ClauseLiteral lit){        
        Literals.add(lit);        
    }

    public void addLiterals(Set<ClauseLiteral> lits){        
        Literals.addAll(lits);        
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CNFClause other = (CNFClause) obj;

        return Objects.equals(this.Literals, other.Literals);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.Literals);
        return hash;
    }

    
}


class ConstraintSolver{
    public int num_chemicals;
    public int num_containers;
    public Set<CNFClause> KB;
    public ClauseLiteral PositiveLiterals[][];
    public ClauseLiteral NegatedLiterals[][];
    public BitSet Model[];
    public Random randomGenerator;
    public Set<CNFClause> FalseClauses;
    
    
    ConstraintSolver(int num_chemicals, int num_containers){
        this.num_chemicals = num_chemicals;
        this.num_containers = num_containers;
        PositiveLiterals = new ClauseLiteral[num_chemicals][num_containers];
        NegatedLiterals = new ClauseLiteral[num_chemicals][num_containers];
        Model = new BitSet[num_chemicals];
        Literal literal;
        for (int i = 0; i < num_chemicals; i++){            
            for (int j = 0; j < num_containers; j++){
                literal = new Literal(i, j);
                PositiveLiterals[i][j] = new ClauseLiteral(literal, false);
                NegatedLiterals[i][j] = new ClauseLiteral(literal, true);
            }
        }
        
        for (int i = 0; i < num_chemicals; i++){
            Model[i] = new BitSet(num_containers);
        }
        KB = new HashSet<>();
        FalseClauses = new HashSet<>();
        randomGenerator = new Random();
    }

    
        
    void addExactlyOneConstraint(){
        
        CNFClause AtLeastOneClause, AtMostOneClause;
        for (int i = 0; i < num_chemicals; i++){                        
            AtMostOneClause = new CNFClause();
            for (int j = 0; j < num_containers; j++){

                AtMostOneClause.addLiteral(PositiveLiterals[i][j]);                
                
                for (int k = j + 1; k < num_containers; k++){
                    AtLeastOneClause = new CNFClause();
                    AtLeastOneClause.addLiteral(NegatedLiterals[i][j]);
                    AtLeastOneClause.addLiteral(NegatedLiterals[i][k]);                    
                    KB.add(AtLeastOneClause);
                }
            }
            
            KB.add(AtMostOneClause);
        }            
        
    }
    
    void addSeparateConstraint(int chemical1, int chemical2){
        CNFClause SeparateClause;
        for (int i = 0; i < num_containers; i++){
            SeparateClause = new CNFClause();
            SeparateClause.addLiteral(NegatedLiterals[chemical1][i]);
            SeparateClause.addLiteral(NegatedLiterals[chemical2][i]);
            KB.add(SeparateClause);
        }        
    }
    
    void addTogetherConstraint(int chemical1, int chemical2){
        CNFClause TogetherClause;
        for (int j = 0; j < num_containers; j++){
            TogetherClause = new CNFClause();
            TogetherClause.addLiteral(NegatedLiterals[chemical1][j]);
            TogetherClause.addLiteral(PositiveLiterals[chemical2][j]);
            
            KB.add(TogetherClause);
            
            TogetherClause = new CNFClause();
            TogetherClause.addLiteral(PositiveLiterals[chemical1][j]);
            TogetherClause.addLiteral(NegatedLiterals[chemical2][j]);
            
            KB.add(TogetherClause);
        }
    }
    
    void getKClauses(int container, Set<CNFClause> KClauses, int start, int k, CNFClause KClause){

        CNFClause K1Clause;
        if (KClause == null)
            KClause = new CNFClause();
        
        for (int i = start; i < num_chemicals; i++){            
            K1Clause = new CNFClause();
            K1Clause.addLiterals(KClause.Literals);
            K1Clause.addLiteral(NegatedLiterals[i][container]);
            
            if (k == 1)
            {   
                KClauses.add(K1Clause);                
                return;
            }
            else {                
                for (int j = i+1; j + k - 1 <= num_chemicals; j++){
                    getKClauses(container, KClauses, j, k-1, K1Clause);
                }

            }
        }

        
    }
    
    void addAtMostKConstraint(int k){
        
        if (k >= num_chemicals)
            return;
        Set<CNFClause> KClauses;
        
        for (int i = 0; i < num_containers; i ++){
            KClauses = new HashSet<>();
            getKClauses(i, KClauses, 0, k + 1, null);
            KB.addAll(KClauses);
        }
    }
    
        boolean containsSubset(Set<CNFClause> KB1, CNFClause C1){
        int size = C1.Literals.size();
        CNFClause C2 = new CNFClause();
        ClauseLiteral lit[] = C1.Literals.toArray(new ClauseLiteral[0]);
        long j;
        int bit;
        for (long i = 0; i < Math.pow(2, size); i++){
            j = i;
            bit = 0;
            while (j!=0){
                if ((j & 1) == 1){
                    C2.addLiteral(lit[bit]);                    
                }
                j = j>>1;
                bit++;
            }
            if (KB1.contains(C2))
                return true;
            C2.Literals.clear();
        }
        return false;
    }
    
    Set<CNFClause> PLResolve(Set<CNFClause> KB1, CNFClause C1, CNFClause C2){
        Set<CNFClause> Resolvents = new HashSet<>();
        CNFClause clause = new CNFClause();
        ClauseLiteral ResolvingLiteral = null;        
        
        int count = 0;
        for (ClauseLiteral lit1 : C1.Literals){
            for (ClauseLiteral lit2 : C2.Literals){
                if(!lit1.equals(lit2) && lit1.isNegation(lit2)){
                    if (count == 0)
                      ResolvingLiteral = lit1;
                    else
                       return Resolvents;
                    count++;
                }                    
            }
        }
        
        if (ResolvingLiteral == null)
            return Resolvents;
        
        for (ClauseLiteral lit1 : C1.Literals){
                if(!lit1.equals(ResolvingLiteral) && !lit1.isNegation(ResolvingLiteral))
                    clause.addLiteral(lit1);
        }

        for (ClauseLiteral lit2 : C2.Literals){
                if(!lit2.equals(ResolvingLiteral) && !lit2.isNegation(ResolvingLiteral))
                    clause.addLiteral(lit2);
        }
        
        if(clause.Literals.isEmpty())
            return null;
    
        else if (containsSubset(KB1, clause))
            return Resolvents;
        
        Resolvents.add(clause);        
        return Resolvents;
    }
    
    boolean PLSatisfiability(Set<CNFClause> KB1){
        Set<CNFClause> Resolvents;
        Set<CNFClause> KBCopy = new HashSet<>(KB1);
        Set<CNFClause> New = new HashSet<>();
        Set<CNFClause> NewCopy = new HashSet<>(KB1);
        int size;
        
        while (true){
            for(CNFClause C1 : KBCopy){
                for(CNFClause C2 : NewCopy){
                    
                    Resolvents = PLResolve(KB1, C1, C2);
                    if (Resolvents == null)
                        return false;
                    
                    New.addAll(Resolvents);
                }
                NewCopy.addAll(New);
            }
            
            size = KBCopy.size();
            KBCopy.addAll(New);   
            
            if (size == KBCopy.size())
                return true;
        }
    }
    
    int getRandomNumber(int start, int end){
        int random = start + randomGenerator.nextInt(end - start + 1);
        return random;
    }
    
    boolean doRandom(double p){
        return randomGenerator.nextDouble() <= p;
    }
    
    void getAllFalseClauses(Set<CNFClause> KB1, BitSet[] Model, Set<CNFClause> FalseClauses){        
        int chemical, container;
        boolean isNegated, value, satisfied;

        for (CNFClause Clause : KB1){
            satisfied = false;
            for (ClauseLiteral Literal : Clause.Literals){
                chemical = Literal.literal.chemical;
                container = Literal.literal.container;
                isNegated = Literal.isNegated;
                value = Model[chemical].get(container);
                if (value == !isNegated){
                    satisfied = true;
                    break;
                }                    
            }
            if (!satisfied)
                FalseClauses.add(Clause);
            
        }
    }
    CNFClause getRandomFalseClause(Set<CNFClause> KB1, Set<CNFClause> FalseClauses){
        
        int random;
        int i = 0;

        random = getRandomNumber(0, FalseClauses.size() - 1);
        for (CNFClause Clause : FalseClauses){
            if (i == random)
               return Clause;            
            i++;
        }
        return null;

    }
    ClauseLiteral getRandomLiteral(CNFClause Clause){        

        int random = getRandomNumber(0, Clause.Literals.size() - 1);
        int i = 0;
        ClauseLiteral RandomLiteral = null;
                
        for (ClauseLiteral literal : Clause.Literals){
            if (i == random){
                RandomLiteral = literal;
                break;
            }                
            i++;
        }
        
        return RandomLiteral;
    }
    
    ClauseLiteral getMaximizingLiteral(Set<CNFClause> KB1, BitSet[] Model, Set<CNFClause> FalseClauses, CNFClause Clause){
        int max = 0;
        ClauseLiteral MaxLiteral = null;
        int refcount;
        int chemical, container;
        
        for (ClauseLiteral clauseliteral : Clause.Literals){
            chemical = clauseliteral.literal.chemical;
            container = clauseliteral.literal.container;
            refcount = 0;
            for (CNFClause FalseClause : FalseClauses){
                if (FalseClause.Literals.contains(clauseliteral))
                    refcount++;
            }
            
            if (refcount > max){
                max = refcount;
                MaxLiteral = clauseliteral;
            }
                
        }            
        return MaxLiteral;
    }
    
   
    boolean WalkSat(Set<CNFClause> KB1, double p, int max_flips){
        
        int random, chemical, container;
        ClauseLiteral flipLiteral;
        CNFClause RandomClause;

        for (int i = 0; i < num_chemicals; i++){
            random = getRandomNumber(0, num_containers - 1);
            Model[i].set(random);
        }
        
        for (int i = 0; i < max_flips; i++){
            
            FalseClauses.clear();
            getAllFalseClauses(KB1, Model, FalseClauses);
            
            if (FalseClauses.isEmpty())
                return true;           
            
            
            RandomClause = getRandomFalseClause(KB1, FalseClauses);
            if (doRandom(p))
                flipLiteral = getRandomLiteral(RandomClause);
            else 
                flipLiteral = getMaximizingLiteral(KB1, Model, FalseClauses, RandomClause);

                        
            chemical = flipLiteral.literal.chemical;
            container = flipLiteral.literal.container;                 
            
            if (Model[chemical].get(container)){
                Model[chemical].clear(container);
            }
            else {
                Model[chemical].set(container);
            }                
        }
        return false;
    }
    
}
  

class FileIO{
    String FilePath;
    
    FileIO(String p){
        FilePath = p;
    }
    
    ConstraintSolver readTextFile() {
        ConstraintSolver SATSolver = null;
        int row = 0, num_chemicals = 0, num_containers, value, k;
        try(FileReader inputFile = new FileReader(FilePath);
            BufferedReader bufRead = new BufferedReader(inputFile);){
            
            String fileLine;
            while ( (fileLine = bufRead.readLine()) != null)
            {    
                
                String[] numbers = fileLine.split(" ");                
                if (row == 0)
                {    
                    if (numbers.length < 3)
                        return null;
                    num_chemicals = Integer.parseInt(numbers[0]);
                    num_containers = Integer.parseInt(numbers[1]);
                    k = Integer.parseInt(numbers[2]);
                    SATSolver = new ConstraintSolver(num_chemicals, num_containers);
                    SATSolver.addExactlyOneConstraint();
                    SATSolver.addAtMostKConstraint(k);
                    
                }
                else {
                    for (int i = row; i < num_chemicals && i < numbers.length; i++) {
                        
                        if(SATSolver == null)
                            return null;
                        
                        value = Integer.parseInt(numbers[i]);  
                        if(value == 1)
                            SATSolver.addTogetherConstraint(row - 1, i);
                        else if (value == -1)
                            SATSolver.addSeparateConstraint(row - 1, i);                        
                    }                    
                }
                row++;
            }
            return SATSolver;
        }
        catch(IOException | NumberFormatException e){
            return null;
        }   
  }

    
    boolean writeMessageToFile(String s){

        try (PrintWriter writer = new PrintWriter(FilePath, "UTF-8")) {          
            writer.println(s);
            return true;
        }
      
      
        catch(FileNotFoundException | UnsupportedEncodingException e){
            return false;
      }      
  }
  
   boolean writeSolutionToFile(BitSet[] Model, int num_chemicals, int num_containers){
      
      
      
      try (PrintWriter writer = new PrintWriter(FilePath, "UTF-8")) {
            if (Model == null)
            {
                writer.println("0");
                return true;
            }
            
            writer.println("1");
            for (int i = 0; i < num_containers; i++) {
                for (int j = 0; j < num_chemicals; j++) {
                    if (Model[j].get(i))
                        writer.print("1 ");
                    else
                        writer.print("0 ");

                }
                writer.println();
          }
      }
      
      catch(FileNotFoundException | UnsupportedEncodingException e){
          return false;
      }
      return true;
  }
  
  void printSolution(BitSet[] Model, int num_chemicals, int num_containers){      
      
        if (Model == null)
        {
            System.out.println("0");
            return;
        }
            
        System.out.println("1");
        for (int i = 0; i < num_containers; i++) {
            for (int j = 0; j < num_chemicals; j++) {
                if (Model[j].get(i))
                    System.out.print("1 ");
                else
                    System.out.print("0 ");

            }
            System.out.println();
      }
      
  }
  void printKB(Set<CNFClause> KB){
        
            Set<ClauseLiteral> Literals;
            Literal literal;
            int ClauseCount = 0, LiteralCount;
            System.out.println();
            for (CNFClause Clause : KB){
                Literals = Clause.Literals;
                System.out.print(ClauseCount + 1 + ") ");
                LiteralCount = 0;
                for (ClauseLiteral clauseliteral : Literals){
                    if(clauseliteral.isNegated)
                        System.out.print("~");
                    
                    literal = clauseliteral.literal;
                    
                    if(LiteralCount != Literals.size() - 1)
                        System.out.print("X" + literal.chemical + "," + literal.container + " V ");
                    else
                        System.out.print("X" + literal.chemical + "," + literal.container);
                    LiteralCount++;
                }
                ClauseCount++;
                System.out.println();    
                    
            }    
            System.out.println();
    }
}
            
public class SATSolverEC {

    public static void main( String[] args) {
        try{        
            if (args.length != 4)
            {
                System.out.println("Incorrect command.");
                System.out.println("Usage :java SATSolver <input_file> <output_file> <p> <max_flips>");
                return;
            }

            FileIO IOHandle = new FileIO(args[0]);
            double p = Double.parseDouble(args[2]);
            int max_flips = Integer.parseInt(args[3]);
            
            ConstraintSolver ConstraintSolver1 = IOHandle.readTextFile();
            if (ConstraintSolver1 == null){
                System.out.println("Input file format incorrect.");
                return;
            }
            System.out.println("Knowledge Base:");
            IOHandle.printKB(ConstraintSolver1.KB);
            IOHandle.FilePath = args[1];
            if (!ConstraintSolver1.PLSatisfiability(ConstraintSolver1.KB))
            {
                System.out.println("The Satisfiablity problem cannot be solved with the given values");
                if (!IOHandle.writeSolutionToFile(null, 0, 0)){
                    System.out.println("Error writing to file");
                    IOHandle.printSolution(null, 0, 0);
                }
                    
            }
            else
            {
                System.out.println("The Satisfiablity problem can be solved with the given values");
                if (ConstraintSolver1.WalkSat(ConstraintSolver1.KB, p, max_flips)){
                    if (!IOHandle.writeSolutionToFile(ConstraintSolver1.Model, ConstraintSolver1.num_chemicals, ConstraintSolver1.num_containers)){
                        System.out.println("Error writing to file");
                        IOHandle.printSolution(ConstraintSolver1.Model, ConstraintSolver1.num_chemicals, ConstraintSolver1.num_containers);
                    }
                }
                else {
                    System.out.println("WalkSAT failed to find a solution");
                    if (!IOHandle.writeSolutionToFile(null, 0, 0)){
                        System.out.println("Error writing to file");
                        IOHandle.printSolution(null, 0, 0);                        
                    }
                }
                    
            }

        }
        catch(NumberFormatException e){
            System.out.println("Incorrect command.");
            System.out.println("Usage :java SATSolver <input_file> <output_file> <p> <max_flips>");
        }
            
    }
    
}
