package com.github.chen0040.gp.treegp.program;


import com.github.chen0040.data.utils.TupleTwo;
import com.github.chen0040.gp.services.RandEngine;
import com.github.chen0040.gp.treegp.TreeGP;
import com.github.chen0040.gp.treegp.enums.TGPInitializationStrategy;
import com.github.chen0040.gp.treegp.gp.ProgramInitialization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by xschen on 14/5/2017.
 */
public class Program implements Serializable, Comparable<Program> {
   /// <summary>
   /// Function Set contains the operators which are the internal nodes of the GP tree, as specified in the Section 3.2 of "A Field Guide to Genetic Programming"
   /// </summary>
   private final OperatorSet operatorSet = new OperatorSet();
   /// <summary>
   /// Variable Set contains program's external inputs, which typically takes the form of named variables, as specified in Section 3.1 of "A Field Guide to Genetic Programming"
   /// </summary>
   private final VariableSet variableSet = new VariableSet();
   /// <summary>
   /// Constant Set contains two type of terminals: constant value and function with no arguments, as specified in Section 3.1 of "A Field Guide to Genetic Programming"
   /// </summary>
   private final ConstantSet constantSet = new ConstantSet();

   private TreeNode root;

   private int depth;
   private int length;

   public OperatorSet getOperatorSet(){
      return operatorSet;
   }

   public VariableSet getVariableSet(){
      return variableSet;
   }

   public ConstantSet getConstantSet(){
      return constantSet;
   }


   public String mathExpression(){
      if(root == null) return "";
      return root.mathExpression();
   }

   /// <summary>
   /// Method that performs deep copy of another GP
   /// </summary>
   /// <param name="that">The GP to copy</param>
   public void copy(Program that) {
      operatorSet.copy(that.operatorSet);
      variableSet.copy(that.variableSet);
      constantSet.copy(that.constantSet);
      depth = that.depth;
      length = that.length;

      if(that.root != null) {
         root = that.root.makeCopy(operatorSet, variableSet, constantSet);
      } else {
         root = null;
      }
   }


   public Program makeCopy(){
      Program clone = new Program();
      clone.copy(this);
      return clone;
   }

   /// <summary>
   /// Method that randomly select and returns a terminal primitive
   /// </summary>
   /// <returns>The randomly selected terminal primitive</returns>
   public Terminal anyTerminal(RandEngine randEngine)
   {
      int variable_count = variableSet.size();
      int constant_count = constantSet.size();
      int r = randEngine.nextInt(variable_count + constant_count);
      if (r < variable_count)
      {
         return (Terminal)variableSet.get(r);
      }
      else
      {
         return (Terminal)constantSet.get(r - variable_count);
      }
   }

   public boolean isBetterThan(Program rhs)
   {
      return compareTo(rhs) < 0;
   }


   /**
    * The method return -1 if this program is better than that program
    * @param that
    * @return
    */
   @Override public int compareTo(Program that) {
      if (depth < that.depth)
      {
         return -1;
      }
      else if (depth == that.depth)
      {
         if (length < that.length)
         {
            return -1;
         }
         else if(length == that.length){
            return 0;
         }
         else
         {
            return 1;
         }
      }
      else
      {
         return 1;
      }
   }

   /// <summary>
   /// Method that creates a GP tree with a maximum tree depth
   /// </summary>
   /// <param name="manager">TreeGP config</param
   public void createWithDepth(int allowableDepth, TreeGP manager){
      root = ProgramInitialization.createWithDepth(this, allowableDepth, manager);
      calcLength();
      calcDepth();
   }

   /// <summary>
   /// Method that calculates the size of the tree
   /// </summary>
   /// <returns>The tree size</returns>
   public int calcLength()
   {
      return length = root.length();
   }

   /// <summary>
   /// Method that calculates the tree depth
   /// </summary>
   /// <returns>The tree depth</returns>
   public int calcDepth()
   {
      return depth = root.depth();
   }


   public Primitive anyOperatorWithArityLessThan(int s, RandEngine randEngine) {
      return operatorSet.anyWithArityLessThan(s, randEngine);
   }


   public Primitive anyOperator(RandEngine randEngine) {
      return operatorSet.any(randEngine);
   }

   /// <summary>
   /// Method that returns a random primitive based on the weight associated with each primitive
   /// The method is similar to roulette wheel
   /// </summary>
   /// <returns>The randomly selected primitive</returns>
   public Primitive FindRandomPrimitive(RandEngine randEngine)
   {
      double r = randEngine.uniform();

      if(r < 0.3333) {
         return constantSet.any(randEngine);
      } else if(r < 0.6666) {
         return variableSet.any(randEngine);
      } else {
         return anyOperator(randEngine);
      }
   }
   
   public TupleTwo<TreeNode, TreeNode> anyNode(RandEngine randEngine){ return anyNode(false, randEngine); }
   

   /// <summary>
   /// Method that returns a randomly selected node from the current tree
   /// The tree is first flatten into a list from which a node is randomly selected
   /// </summary>
   /// <returns></returns>
   public TupleTwo<TreeNode, TreeNode> anyNode(boolean bias, RandEngine randEngine)
   {
      List<TupleTwo<TreeNode, TreeNode>> nodes = flattenNodes();
      if (bias)
      {
         if (randEngine.uniform() <= 0.1) // As specified by Koza, 90% select function node, 10% select terminal node
         {
            List<TupleTwo<TreeNode, TreeNode>> terminal_nodes = new ArrayList<>();
            for (TupleTwo<TreeNode, TreeNode> tuple : nodes)
            {
               TreeNode node = tuple._1();
               if (node.isTerminal())
               {
                  terminal_nodes.add(tuple);
               }
            }
            if (terminal_nodes.size() > 0)
            {
               return terminal_nodes.get(randEngine.nextInt(terminal_nodes.size()));
            }
            else
            {
               return nodes.get(randEngine.nextInt(nodes.size()));
            }
         }
         else
         {
            List<TupleTwo<TreeNode, TreeNode>> function_nodes = new ArrayList<>();
            for (TupleTwo<TreeNode, TreeNode> tuple : nodes)
            {
               TreeNode node = tuple._1();
               if (!node.isTerminal())
               {
                  function_nodes.add(tuple);
               }
            }
            if (function_nodes.size() > 0)
            {
               return function_nodes.get(randEngine.nextInt(function_nodes.size()));
            }
            else
            {
               return nodes.get(randEngine.nextInt(nodes.size()));
            }
         }
      }
      else
      {
         return nodes.get(randEngine.nextInt(nodes.size()));
      }

   }

   /// <summary>
   /// Method that flattens the tree and then stores all the nodes of the tree in a list
   /// </summary>
   /// <returns>The list of nodes in the tree</returns>
   public List<TupleTwo<TreeNode, TreeNode>> flattenNodes()
   {
      List<TupleTwo<TreeNode, TreeNode>> list = new ArrayList<>();
      collectNodes(root, null, list);
      return list;
   }

   private void collectNodes(TreeNode node, TreeNode parent_node, List<TupleTwo<TreeNode, TreeNode>> list) {
      if(node == null) return;
      list.add(new TupleTwo<>(node, parent_node));
      for(TreeNode child : node.getChildren()){
         collectNodes(child, node, list);
      }
   }
}
