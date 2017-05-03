package com.github.chen0040.gp.lgp.program;


import com.github.chen0040.gp.lgp.enums.OperatorExecutionStatus;
import com.github.chen0040.gp.lgp.gp.BasicFitnessCase;
import com.github.chen0040.gp.lgp.gp.FitnessCase;
import com.github.chen0040.gp.lgp.helpers.InstructionHelper;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by xschen on 2/5/2017.
 */
@Getter
@Setter
public class Program {
   private RegisterSet registerSet = new RegisterSet();
   private ConstantSet constantSet = new ConstantSet();
   private OperatorSet operatorSet = new OperatorSet();
   private List<Instruction> instructions = new ArrayList<>();

   private double cost;
   private boolean costValid;

   /*
   Source: Brameier, M 2004  On Linear Genetic Programming (thesis)
   
   Algorithm 3.1 (detection of structural introns)
   1. Let set R_eff always contain all registers that are effective at the current program
     position. R_eff := { r | r is output register }.
     Start at the last program instruction and move backwards.
   2. Mark the next preceding operation in program with:
      destination register r_dest element-of R_eff.
     If such an instruction is not found then go to 5.
   3. If the operation directly follows a branch or a sequence of branches then mark these
     instructions too. Otherwise remove r_dest from R_eff .
   4. Insert each source (operand) register r_op of newly marked instructions in R_eff
     if not already contained. Go to 2.
   5. Stop. All unmarked instructions are introns.
   */
   public void markStructuralIntrons(ProgramManager manager) {
    
      int instruction_count=instructions.size();
      for (int i = instruction_count - 1; i >= 0; i--)
      {
         instructions.get(i).setStructuralIntron(true);
      }

      Set<Integer> Reff = new HashSet<>();
      int io_register_count = manager.getRegisterCount();
      for (int i = 0; i < io_register_count; ++i)
      {
         Reff.add(i);
      }

      Instruction current_instruction = null;
      Instruction prev_instruction = null;  // prev_instruction is the last visited instruction from bottom up of the program 

      for (int i = instruction_count - 1; i >= 0; i--)
      {
         prev_instruction = current_instruction;
         current_instruction = instructions.get(i);
         // prev_instruction is not an structural intron and the current_instruction
         // is a conditional construct then, the current_instruction is not structural intron either
         // this directly follows from Step 3 of Algorithm 3.1
         if (current_instruction.getOperator().isConditionalConstruct() && prev_instruction != null)
         {
            if (!prev_instruction.isStructuralIntron())
            {
               current_instruction.setStructuralIntron(false);
            }
         }
         else
         {
            if (Reff.contains(current_instruction.getTargetOperand().getIndex()))
            {
               current_instruction.setStructuralIntron(false);
               Reff.remove(current_instruction.getTargetOperand().getIndex());

               if (!current_instruction.getOperand1().isConstant())
               {
                  Reff.add(current_instruction.getOperand1().getIndex());
               }
               if (!current_instruction.getOperand2().isConstant())
               {
                  Reff.add(current_instruction.getOperand2().getIndex());
               }
            }
         }
      }
   }

   public void execute(FitnessCase fitness_case)
   {
      int inputRegisterCount = registerSet.size();
      for(int i=0; i < inputRegisterCount; ++i) {
         registerSet.get(i).setValue(fitness_case.readInput(i % fitness_case.inputCount()));
      }

      OperatorExecutionStatus command = OperatorExecutionStatus.LGP_EXECUTE_NEXT_INSTRUCTION;
      Instruction current_effective_instruction = null;
      Instruction prev_effective_instruction = null;
      for (Instruction instruction : instructions)
      {
         if (instruction.isStructuralIntron())
         {
            continue;
         }
         prev_effective_instruction = current_effective_instruction;
         current_effective_instruction = instruction;
         if (command == OperatorExecutionStatus.LGP_EXECUTE_NEXT_INSTRUCTION)
         {
            command = current_effective_instruction.execute();
            //fitness_case.ReportProgress(instruction.Operator, instruction.Operand1, instruction.Operand2, instruction.DestinationRegister, RegisterSet);
         }
         else
         {
            // Xianshun says:
            // as suggested in Linear Genetic Programming
            // the condictional construct is restricted to single condictional construct
            // an example of single conditional construct would be
            // line 1: if(register[a])
            // line 2: <action1>
            // line 3: <action2>
            // if register[a]==true, then <action1> and <action2> are executed
            // if register[a]==false, then <action1> is skipped and <action2> is executed
            // <action1> and <action2> are restricted to effective instruction
            if (prev_effective_instruction.getOperator().isConditionalConstruct())
            {
               command = OperatorExecutionStatus.LGP_EXECUTE_NEXT_INSTRUCTION;
            }
         }
      }


      int outputRegisterCount = Math.min(registerSet.size(), fitness_case.outputCount());
      for (int i = 0; i < outputRegisterCount; ++i)
      {
         fitness_case.writeOutput(i, registerSet.get(i).getValue());
      }
   }

   public double[] execute(double[] input){
      BasicFitnessCase fitnessCase = new BasicFitnessCase(input.length, input.length);
      execute(fitnessCase);
      double[] output = new double[input.length];
      for(int i=0; i < output.length; ++i) {
         output[i] = fitnessCase.readOutput(i);
      }
      return output;
   }


   public Program makeCopy() {
      Program clone = new Program();
      clone.copy(this);
      return clone;
   }

   public void copy(Program that) {
      registerSet.copy(that.registerSet);
      constantSet.copy(that.constantSet);
      operatorSet.copy(that.operatorSet);

      instructions.clear();
      for(int i=0; i < that.instructions.size(); ++i) {
         instructions.add(InstructionHelper.makeCopy(that.instructions.get(i), registerSet, constantSet, operatorSet));
      }

      cost = that.cost;
      costValid = that.costValid;
   }

   public long effectiveInstructionCount() {
      return instructions.stream().filter(instruction -> !instruction.isStructuralIntron()).count();
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < instructions.size(); ++i)
      {
         sb.append("instruction[").append(i).append("]: ").append(instructions.get(i)).append("\n");
      }
      if (costValid)
      {
         sb.append("fitness: ").append(cost);
      }
      else
      {
         sb.append("Invalid Fitness");
      }
      return sb.toString();
   }

   public void invalidateCost(){
      costValid = false;
   }

}
