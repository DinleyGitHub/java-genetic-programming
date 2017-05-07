package com.github.chen0040.gp.lgp.gp;


import com.github.chen0040.gp.lgp.program.Program;
import com.github.chen0040.gp.lgp.LGP;
import com.github.chen0040.gp.services.RandEngine;
import com.github.chen0040.gp.utils.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Created by xschen on 3/5/2017.
 */
@Getter
@Setter
public class Population {
   @Setter(AccessLevel.NONE)
   @Getter(AccessLevel.NONE)
   private Optional<Program> globalBestProgram = Optional.empty();
   private List<Program> programs=new ArrayList<>();
   private int currentGeneration = 0;
   private final LGP manager;
   private final RandEngine randEngine;
   private Program bestProgramInCurrentGeneration = null;

   public Population(LGP manager, RandEngine randEngine){
      this.manager = manager;
      this.randEngine = randEngine;
   }

   protected void evaluate(LGP manager) {

      double bestCost = Double.MAX_VALUE;
      for(int i=0; i< programs.size(); ++i) {
         Program p = programs.get(i);
         double cost = manager.evaluateCost(p);
         p.setCost(cost);
         p.setCostValid(true);
         if (p.getCost() < bestCost) {
            bestProgramInCurrentGeneration = p;
            bestCost = p.getCost();
         }
      }

      updateGlobal(bestProgramInCurrentGeneration);
   }

   public void initialize(){
      PopulationInitialization.initialize(programs, manager, randEngine);
      evaluate(manager);
   }


   private void updateGlobal(Program lgp) {
      if(!globalBestProgram.isPresent() || CollectionUtils.isBetterThan(lgp, globalBestProgram.get()))
      {
         globalBestProgram = Optional.of(lgp.makeEffectiveCopy());
      }
   }


   public boolean isTerminated() {
      return currentGeneration >= manager.getMaxGeneration();
   }


   public void evolve()
   {
      int iPopSize = manager.getPopulationSize();
      int program_count=0;

      while(program_count < iPopSize)
      {

         TournamentSelectionResult<Program> tournament = TournamentSelection.select(programs, randEngine);
         TupleTwo<Program> tournament_winners = tournament.getWinners();
         TupleTwo<Program> tournament_losers = tournament.getLosers();

         Program tp1 = tournament_winners._1().makeCopy();
         Program tp2 = tournament_winners._2().makeCopy();


         double r = randEngine.uniform();
         if (r < manager.getCrossoverRate())
         {
            Crossover.apply(tp1, tp2, manager, randEngine);
         }

         r = randEngine.uniform();
         if (r < manager.getMacroMutationRate())
         {
            MacroMutation.mutate(tp1, manager, randEngine);
         }

         r = randEngine.uniform();
         if (r < manager.getMacroMutationRate())
         {
            MacroMutation.mutate(tp2, manager, randEngine);
         }

         r=randEngine.uniform();
         if(r < manager.getMicroMutationRate())
         {
            MicroMutation.mutate(tp1, manager, randEngine);
         }

         r=randEngine.uniform();
         if(r < manager.getMicroMutationRate())
         {
            MicroMutation.mutate(tp2, manager, randEngine);
         }

         tp1.setCost(manager.evaluateCost(tp1));
         tp1.setCostValid(true);

         tp2.setCost(manager.evaluateCost(tp2));
         tp2.setCostValid(true);

         Program loser1 = Replacement.compete(programs, tournament_losers._1(), tp1, manager, randEngine);
         Program loser2 = Replacement.compete(programs, tournament_losers._2(), tp2, manager, randEngine);

         if(loser1==tournament_losers._1())
         {
            ++program_count;
         }
         if(loser2==tournament_losers._2())
         {
            ++program_count;
         }
      }

      double bestCost = Double.MAX_VALUE;
      for(int i=0; i< programs.size(); ++i) {
         Program p = programs.get(i);
         if (p.getCost() < bestCost) {
            bestProgramInCurrentGeneration = p;
            bestCost = p.getCost();
         }
      }
      updateGlobal(bestProgramInCurrentGeneration);


      currentGeneration++;
   }


   public Program getGlobalBestProgram() {
      return globalBestProgram.get();
   }


   public double getCostInCurrentGeneration() {
      return bestProgramInCurrentGeneration.getCost();
   }
}
