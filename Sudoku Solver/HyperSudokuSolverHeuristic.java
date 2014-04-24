
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

class BitVector {

    int []bitvector;
    int num_set;
    int size;
    
    BitVector(int s){
        size = s;
        if (size < 32)
            size = 32;
        bitvector = new int[size >> 5];
        num_set = 0;
    }
    
    boolean get(int bit){
        if (bitvector.length < (bit >> 5))
            return false;
        
        return ((bitvector[bit >> 5]& (1 << (bit & 0x1F))) != 0);
    }
            
    void set (int bit, boolean value){
        
        if (bit/32 < bitvector.length){
            boolean isBitSet = get(bit);
            if (value && !isBitSet){
                bitvector[bit >> 5]|= (1 << (bit & 0x1F));
                num_set++;
            }
            else if (isBitSet){
                bitvector[bit >> 5]&= ~(1 << (bit & 0x1F));
                num_set--;
            }
                
        }        
    }
    
    boolean isOnlySet(int bit){
        boolean ret;
        if (get(bit) == false)
            return false;
        
        set(bit, false);
        ret = isAllReset();
        set(bit, true);
        return ret;
    }
    
    boolean isAllSet(){
        for(int i = 0; i < bitvector.length; i++)
            if (bitvector[i] != ~0)
                return false;
        
        return true;
    }
    
     boolean isAllReset(){
        for(int i = 0; i < bitvector.length; i++)
            if (bitvector[i] != 0)
                return false;
        
        return true;
    }
    
    void setAll(){
        for(int i = 0; i < bitvector.length; i++)
            bitvector[i] = ~0;
        
        num_set = bitvector.length<<5;
    }
    
    void clearAll(){
        for(int i = 0; i < bitvector.length; i++)
            bitvector[i] = 0;
        
        num_set = 0;
    }
    
    BitVector getIntersection(BitVector bv2){
        BitVector bv = new BitVector(this.size);
        int new_num_set = 0;
        int curbv;
        for (int i = 0; i < bv.bitvector.length; i++)
            bv.bitvector[i] = this.bitvector[i] & bv2.bitvector[i];
        
        for (int i = 0; i < bv.bitvector.length; i++){
            curbv = bv.bitvector[i];
            while (curbv!= 0){
                new_num_set++;
                curbv &= (curbv - 1);
            }
        }
        bv.num_set = new_num_set;
        return bv;
    }
    
}

class Cell {
    
    int set_value;
    int row;
    int col;
    boolean fixed;
    
    Cell (int r, int c){
        row = r;
        col = c;
        set_value = -1;
        fixed = false;
    }
    
}


class SudokuConstraints{
   
    BitVector[] rows;
    BitVector[] cols;
    BitVector[] squares;
    BitVector[] hypersquares;
    int size;
    int square_size;
    int hypersquare_size;
    
    SudokuConstraints (int s){
        size = s;
        
        double sq_size = Math.sqrt(size);
        square_size = (int)sq_size;
        hypersquare_size = (square_size - 1)*(square_size - 1);
        
        rows = new BitVector[size];
        cols = new BitVector[size];
        squares = new BitVector[size];
        hypersquares = new BitVector[hypersquare_size];
        
        for (int i = 0; i < size; i++){
            rows[i] = new BitVector(size);
            cols[i] = new BitVector(size);
            squares[i] = new BitVector(size);
                        
            rows[i].setAll();
            cols[i].setAll();
            squares[i].setAll();
            
            if (i < hypersquare_size)
            {
                hypersquares[i] = new BitVector(size);
                hypersquares[i].setAll();
            }
        }
            
    }

    boolean isInHyperSquare (int row, int col){
                
        return !(((row%(square_size + 1)) == 0) || ((col%(square_size + 1)) == 0));
    }
    
    
    boolean propagateConstraints(int row, int col, int value, boolean set){        
        
        int square_index = (row/square_size)*square_size + col/square_size;
        int hypersquare_index = (row/hypersquare_size)*(square_size-1) + col/hypersquare_size;
        
        if (!set){
            rows[row].set(value, true);
            cols[col].set(value, true);
            squares[square_index].set(value, true);
            if (isInHyperSquare(row, col))
                hypersquares[hypersquare_index].set(value, true);
            return true;
        }
        
        if (value > size)
            return false;
        
        if (!rows[row].get(value) || !cols[col].get(value) || !squares[square_index].get(value))
            return false;
        
        if (isInHyperSquare(row, col) && !hypersquares[hypersquare_index].get(value))
            return false;
        
        rows[row].set(value, false);
        cols[col].set(value, false);
        squares[square_index].set(value, false);

        if (isInHyperSquare(row, col))
                hypersquares[hypersquare_index].set(value, false);
                    
        return true;
    }
    
    int getNumOfPossibleValues(int row, int col){
        
        int square_index = (row/square_size)*square_size + col/square_size;
        int hypersquare_index = (row/hypersquare_size)*(square_size-1) + col/hypersquare_size;
        
        BitVector bv = rows[row].getIntersection(cols[col]);
        bv = bv.getIntersection(squares[square_index]);
        if (isInHyperSquare(row, col))
            bv = bv.getIntersection(hypersquares[hypersquare_index]);
        
        return bv.num_set;
    }
    
}

class FileIO{
    String FilePath;
    
    FileIO(String p){
        FilePath = p;
    }
    
    SudokuGrid readTextFile() {
        SudokuGrid sudoku = null;
        int row = 0, col = 0;
        try(FileReader inputFile = new FileReader(FilePath);
            BufferedReader bufRead = new BufferedReader(inputFile);){
            
            String fileLine;
            while ( (fileLine = bufRead.readLine()) != null)
            {    
                
                String[] numbers = fileLine.split(" ");
                if (row == 0)
                    sudoku = new SudokuGrid(numbers.length);
                
                for (String number : numbers) {
                    if(sudoku == null || !sudoku.setInitialValue(row, col, number))
                        return null;
                    col++;
                }
                row++;
                col = 0;
            }
            return sudoku;
        }
        catch(IOException e){
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
  
    boolean writeSudokuToFile(SudokuGrid sudoku){
      
      if (sudoku == null)
          return false;
      
      try (PrintWriter writer = new PrintWriter(FilePath, "UTF-8")) {
          
            for (int row = 0; row < sudoku.size; row++){
                for (int col = 0; col < sudoku.size; col++)
                    if (col == sudoku.size-1)
                        writer.print((sudoku.cells[row][col].set_value + 1));
                    else
                        writer.print((sudoku.cells[row][col].set_value + 1) + " ");
            
                if (row != sudoku.size-1)
                    writer.println("");
             }
      }
      
      catch(FileNotFoundException | UnsupportedEncodingException e){
          return false;
      }
      return true;
  }
  
    void printSudoku(SudokuGrid sudoku){
            for (int row = 0; row < sudoku.size; row++){
                for (int col = 0; col < sudoku.size; col++)
                    if (col == sudoku.size-1)
                        System.out.print((sudoku.cells[row][col].set_value + 1));
                    else
                        System.out.print((sudoku.cells[row][col].set_value + 1) + " ");
            
                if (row != sudoku.size-1)
                    System.out.println("");
            }    
    }
}


class SudokuGrid {
    Cell [][] cells;
    SudokuConstraints constraints;
    int size;
    long branch_count;
    long initial_value_count;
    
    SudokuGrid(){      
    }
    
    SudokuGrid (int s){
        size = s;
        branch_count = 0;
        initial_value_count = 0;
        cells = new Cell[size][size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                cells[i][j] = new Cell(i, j);
        
         constraints = new SudokuConstraints (size);        
    }
    
    Cell getNextCell(Cell c){
        
        Cell nextCell = c;
        
        while (nextCell.row != size - 1 || nextCell.col != size - 1){            
            if (nextCell.col == size-1)
                nextCell = cells[nextCell.row + 1][0];
            else
                nextCell = cells[nextCell.row][nextCell.col + 1];
            
            if (!nextCell.fixed)
                return nextCell;
        }
        
        return null;
    }

    
    Cell getNextCellWithMinimumValueHeuristic(){
        int min = Integer.MAX_VALUE;
        int cellVal;
        Cell nextCell = null;
        for (int row = 0; row < size; row++){
            for (int col = 0; col < size; col++){
                cellVal = constraints.getNumOfPossibleValues(row, col);
                if (min > cellVal && !cells[row][col].fixed){                
                    min = cellVal;
                    nextCell = cells[row][col];                                        
                }
            }
        }
        
        if (nextCell != null)
            nextCell.fixed = true;
        
        return nextCell;        
    }
    
    boolean setInitialValue(int row, int col, String value){
        
        int val = -1;
        if(row > size-1 || col > size-1)
            return false;
        
        try {
            if (!value.equals("-"))
                val = Integer.parseInt(value);
        }
        catch(NumberFormatException e){
            return false;
        }
        
        if (val == -1)
            return true;
                
        cells[row][col].set_value = val - 1;
        cells[row][col].fixed = true;
        initial_value_count++;
        return constraints.propagateConstraints(row, col, val - 1, true);
    }
    
    void resetAll(){
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
            {   
                cells[r][c].set_value = -1;
                cells[r][c].fixed = false;                
            }
    }
    
    boolean solveSudokuFromCell(Cell curcell){
            
        if (curcell == null)
            return true;
                   
        for (int i = 0; i < size; i++)
        {            
            curcell.set_value = i;
            if (!constraints.propagateConstraints(curcell.row, curcell.col, i, true))
                continue;
            
            branch_count++;
            if (solveSudokuFromCell(getNextCellWithMinimumValueHeuristic()))
                return true;            
            
            if (!constraints.propagateConstraints(curcell.row, curcell.col, i, false)){
                curcell.fixed = false;
                return false;
            }
                
        }
        curcell.fixed = false;
        return false;
    }
    
    boolean solveSudoku(){
        branch_count = 0;
        return solveSudokuFromCell(getNextCellWithMinimumValueHeuristic());
    }   
}

public class HyperSudokuSolverHeuristic {

    public static void main(String[] args) {
        
        if (args.length != 2)
        {
            System.out.println("Incorrect command.\nUsage:java HyperSudokuSolverHeuristic <input_file> <output_file>");
            return;
        }
        
        
        
        FileIO IOHandle = new FileIO(args[0]);
        SudokuGrid sudoku = IOHandle.readTextFile();
        IOHandle.FilePath = args[1];
        if (sudoku == null){
            
            if (!IOHandle.writeMessageToFile("The Sudoku cannot be solved with the given values"))
                System.out.println("The Sudoku cannot be solved with the given values");
            
            return;
        }
        

        final long startTime = System.currentTimeMillis();
        if (!sudoku.solveSudoku()){
            
            if (!IOHandle.writeMessageToFile("The Sudoku cannot be solved with the given values"))
                System.out.println("The Sudoku cannot be solved with the given values");
            
            return;
        }
        final long endTime = System.currentTimeMillis();
        
        if (!IOHandle.writeSudokuToFile(sudoku)){
            System.out.println("Error writing to output file./nThe solved sudoku is as follows:");
            IOHandle.printSudoku(sudoku);
        }
        System.out.println("");
        System.out.println("-----------------------------------------------------------------------------" );
        System.out.println("Solved Hyper-Sudoku using Minimum Remaining Value Heuristic.\nResults:" );
        System.out.println("-----------------------------------------------------------------------------" );
        System.out.println("\tSize of the Hyper-Sudoku: " + sudoku.size + "X"+ sudoku.size);
        System.out.println("\tNumber of given values: " + sudoku.initial_value_count);
        System.out.println("\tTotal time taken to solve it: " + (endTime-startTime) + " ms");
        System.out.println("\tNumber of nodes expanded: " + sudoku.branch_count);
        System.out.println("-----------------------------------------------------------------------------" );
    }
    
}
