
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

class BitVector {

    int []bitvector;
    
    BitVector(int size){
        if (size < 32)
            size = 32;
        bitvector = new int[size >> 5];
    }
    
    boolean get(int bit){
        if (bitvector.length < (bit >> 5))
            return false;
        
        return ((bitvector[bit >> 5]& (1 << (bit & 0x1F))) != 0);
    }
            
    void set (int bit, boolean value){
        
        if (bit/32 < bitvector.length){
            if (value)
                bitvector[bit >> 5]|= (1 << (bit & 0x1F));
            else
                bitvector[bit >> 5]&= ~(1 << (bit & 0x1F));
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
    }
    
    void clearAll(){
        for(int i = 0; i < bitvector.length; i++)
            bitvector[i] = 0;
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
    
}

class FileIO{
    String FilePath;
    
    FileIO(String p){
        FilePath = p;
    }
    
    SudokuGrid readTextFile() {
        SudokuGrid sudoku = new SudokuGrid();
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
                    if(!sudoku.setInitialValue(row, col, number))
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
            if (solveSudokuFromCell(getNextCell(curcell)))
                return true;
            
            if (!constraints.propagateConstraints(curcell.row, curcell.col, i, false))
                return false;
        }    
        return false;
    }
    
    boolean solveSudoku(){
        Cell c = cells[0][0];
        branch_count = 0;
        if (c.fixed)
            c = getNextCell(c);
            
        return solveSudokuFromCell(c);
    }    
}

public class HyperSudokuSolver {

    public static void main(String[] args) {
        
        if (args.length != 2)
        {
            System.out.println("Incorrect command.\nUsage:java HyperSudokuSolver <input_file> <output_file>");
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
        System.out.println("Solved Hyper-Sudoku.\nResults:" );
        System.out.println("-----------------------------------------------------------------------------" );
        System.out.println("\tSize of the Hyper-Sudoku: " + sudoku.size + "X"+ sudoku.size);
        System.out.println("\tNumber of given values: " + sudoku.initial_value_count);
        System.out.println("\tTotal time taken to solve it: " + (endTime-startTime) + " ms");
        System.out.println("\tNumber of nodes expanded: " + sudoku.branch_count);
        System.out.println("-----------------------------------------------------------------------------" );
        
    }
    
}
