import java.util.BitSet;
import java.util.HashSet;
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
    public int RunTime;
    
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
        RunTime = 0;
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
        
        while (true){
            for(CNFClause C1 : KBCopy){
                for(CNFClause C2 : KBCopy){
                    
                    Resolvents = PLResolve(KB1, C1, C2);
                    if (Resolvents == null)
                        return false;
                    
                    New.addAll(Resolvents);                        
                }
            }
                                
            if (KBCopy.containsAll(New))
                return true;

            KBCopy.addAll(New);     
            
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
            {
                RunTime = i + 1;
                return true;
            }           
            
            
            RandomClause = getRandomFalseClause(KB1, FalseClauses);
            if (doRandom(p))
                flipLiteral = getRandomLiteral(RandomClause);
            else 
                flipLiteral = getMaximizingLiteral(KB1, Model, FalseClauses, RandomClause);

                        
            chemical = flipLiteral.literal.chemical;
            container = flipLiteral.literal.container;
            Model[chemical].flip(container);

        }
        RunTime = max_flips;
        return false;
    }
    
}
  
class RandomInstanceGenerator{
        
        Random RandomGenerator;
        
        RandomInstanceGenerator(){
            RandomGenerator = new Random();
        }
        
        int doRandom(double y, double n){
            
            double random = RandomGenerator.nextDouble();
            /*System.out.println("Y " + y);
            System.out.println("N " + n);
            System.out.println("random " + random);
            System.out.println("y+n " + (y + n));*/
            if (random <= y)
            {
                return 1;
            }
            else if (random <= (y + n))
            {
                return -1;
            }
            return 0;
            
        }
        ConstraintSolver GenerateRandomInstance(int N, int M, double y, double n){
            
            ConstraintSolver SATSolver = new ConstraintSolver(N, M);
            SATSolver.addExactlyOneConstraint();
            int random;
            for(int i = 0; i < N; i++){
                for(int j = i + 1; j < N; j++){    
                    if (i!=j){
                        random = doRandom(y, n);
                        if(random == 1)
                        {
                            SATSolver.addTogetherConstraint(i, j);
                        }
                        else if(random == -1){
                            SATSolver.addSeparateConstraint(i, j);
                        }
                    }                    
                }
            }
            return SATSolver;
        }
 }

            
public class SATSolverRandomInstance {
    
    public static void printIncorrectArguments(){
        System.out.println("Incorrect command.");
        System.out.println("Possible Usages:");
        System.out.println("No List Test: java SATSolverRandomInstance <selector = 1> <Num_Sentences> <N> <M> <p> <max_flips> <y> <n start> <n end> <n interval>");
        System.out.println();
        System.out.println("Yes List Test: java SATSolverRandomInstance <selector = 2> <Num_Sentences> <N> <M> <p> <max_flips> <n> <y start> <y end> <y interval>");
        System.out.println();
        System.out.println("Clause/Symbol Ratio Test: java SATSolverRandomInstance <selector = 3> <Num_Satisfiable_Sentences> <N> <M> <p> <max_flips> <y> <n>");
    }


    public static void main( String[] args) {
        try{        
            if (args.length < 8)
            {
                printIncorrectArguments();
                return;
            }

            int selector = Integer.parseInt(args[0]);
            int num_sentences = Integer.parseInt(args[1]);
            int num_chemicals = Integer.parseInt(args[2]);
            int num_containers = Integer.parseInt(args[3]);
            double p = Double.parseDouble(args[4]);
            int max_flips = Integer.parseInt(args[5]);
            
            double nstart, nend, ninterval, ystart, yend, yinterval;
            ConstraintSolver ConstraintSolver1;
            
            RandomInstanceGenerator rgen = new RandomInstanceGenerator();
            int WalkSatSatisfiability, PLSatisfiability, TestCount = 0;
            
            if (selector == 1){
                if (args.length < 10)
                {
                    printIncorrectArguments();
                    return;
                }
                ystart = Double.parseDouble(args[6]);
                nstart = Double.parseDouble(args[7]);
                nend = Double.parseDouble(args[8]);
                ninterval = Double.parseDouble(args[9]);
                
                
                if (ninterval <= 0)
                    ninterval = 0.02;
                
                for (double j = nstart; j <= nend; j+= ninterval ){
                    
                    System.out.println();
                    System.out.println("Test Number: " + (TestCount + 1));
                    PLSatisfiability = 0;
                    WalkSatSatisfiability = 0;
                    for (int i = 0; i < num_sentences; i++){        
                        ConstraintSolver1 = rgen.GenerateRandomInstance(num_chemicals, num_containers, ystart, j);
                        
                        if (ConstraintSolver1.PLSatisfiability(ConstraintSolver1.KB)){
                            PLSatisfiability++;
                        }
                            
                        if (ConstraintSolver1.WalkSat(ConstraintSolver1.KB, p, max_flips)){
                            WalkSatSatisfiability++;
                        }

                    }
                    System.out.println("n = " + j);
                    System.out.println("P(Satisfiability) for PL Resolution = " + (double)PLSatisfiability/num_sentences);
                    System.out.println("P(Satisfiability) for WalkSat = " + (double)WalkSatSatisfiability/num_sentences);
                    System.out.println();
                    
                    TestCount++;                    
                }
                
            }
            
            else if (selector == 2){
                if (args.length < 10)
                {
                    printIncorrectArguments();
                    return;
                }
                nstart = Double.parseDouble(args[6]);
                ystart = Double.parseDouble(args[7]);
                yend = Double.parseDouble(args[8]);
                yinterval = Double.parseDouble(args[9]);
                
                if (yinterval <= 0)
                    yinterval = 0.02;
                
                for (double j = ystart; j <= yend; j+= yinterval ){
                    
                    System.out.println();
                    System.out.println("Test Number: " + (TestCount + 1));
                    WalkSatSatisfiability = 0;
                    for (int i = 0; i < num_sentences; i++){        
                        ConstraintSolver1 = rgen.GenerateRandomInstance(num_chemicals, num_containers, j, nstart);
                        
                        if (ConstraintSolver1.WalkSat(ConstraintSolver1.KB, p, max_flips)){
                            WalkSatSatisfiability++;
                        }

                    }
                    System.out.println("y = " + j);
                    System.out.println("P(Satisfiability) for WalkSat = " + (double)WalkSatSatisfiability/num_sentences);
                    System.out.println();
                    
                    TestCount++;    
                }
            }
            
            else if (selector == 3){
                ystart = Double.parseDouble(args[6]);
                nstart = Double.parseDouble(args[7]);
                int i = 0; 
                double SumOfClauseSymbolRatio = 0.0;
                int num_clauses, num_symbols;
                int SumOfRunTime = 0;
                while (i < num_sentences){
                    
                    ConstraintSolver1 = rgen.GenerateRandomInstance(num_chemicals, num_containers, ystart, nstart);                    
                    num_symbols = num_chemicals*num_containers;
                    num_clauses = ConstraintSolver1.KB.size();
                    if (ConstraintSolver1.WalkSat(ConstraintSolver1.KB, p, max_flips)){
                        i++;
                        SumOfClauseSymbolRatio += (double)num_clauses/num_symbols;
                        SumOfRunTime += ConstraintSolver1.RunTime;
                    }

                    TestCount++;
                }
                System.out.println("Total Sentences Generated = " + TestCount);
                System.out.println("Average Clause/Symbol Ratio = " + SumOfClauseSymbolRatio/num_sentences);
                System.out.println("Average RunTime = " + (double)SumOfRunTime/num_sentences);
                System.out.println();
                    
            }
            else
                printIncorrectArguments();

        }
        catch(NumberFormatException e){
                printIncorrectArguments();
        }
            
    }
    
}
