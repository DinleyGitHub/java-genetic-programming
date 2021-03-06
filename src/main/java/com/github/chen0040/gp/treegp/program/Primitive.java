package com.github.chen0040.gp.treegp.program;


import com.github.chen0040.data.utils.StringUtils;
import com.github.chen0040.gp.commons.Observation;
import com.github.chen0040.gp.exceptions.SizeMismatchedException;
import com.github.chen0040.gp.commons.Indexable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by xschen on 8/5/2017.
 */
public abstract class Primitive implements Serializable, Indexable<Primitive> {
   private static final long serialVersionUID = -249257238928605728L;
   private final List<Double> inputs = new ArrayList<>();
   private final List<String> textInputs = new ArrayList<>();
   private String textValue;
   private double value;
   private final String symbol;
   private final boolean readOnly;
   private int index;

   public Primitive(){
      symbol = "";
      readOnly = false;
   }

   public Primitive(int inputCount, String symbol, double value, String textValue, boolean readOnly) {
      for(int i=0; i < inputCount; ++i){
         inputs.add(0.0);
      }
      for(int i=0; i < inputCount; ++i) {
         textInputs.add(null);
      }

      this.symbol = symbol;
      this.value = value;
      this.textValue = textValue;
      this.readOnly = readOnly;
   }

   public boolean isTerminal(){
      return inputs.isEmpty();
   }

   public String getName(){
      if(StringUtils.isEmpty(symbol)){
         return (isReadOnly() ? "c" : "v") + index;
      } else {
         return symbol;
      }
   }

   public Primitive copy(Primitive that) {
      if(!symbol.equals(that.getSymbol())){
         throw new RuntimeException("Symbol not matched for copy to proceed");
      }
      inputs.clear();
      inputs.addAll(that.inputs);

      textInputs.clear();
      textInputs.addAll(that.textInputs);

      value = that.value;
      textValue = that.textValue;

      index = that.index;
      return this;
   }

   public int arity(){
      return inputs.size();
   }

   public void beforeExecute(List<Double> values, Observation observation){
      if(inputs.size() != values.size()) {
         throw new SizeMismatchedException(inputs.size(), values.size());
      }
      for(int i=0; i < values.size(); ++i) {
         inputs.set(i, values.get(i));
      }
   }

   public void beforeExecuteWithText(List<String> values, Observation observation){
      if(textInputs.size() != values.size()) {
         throw new SizeMismatchedException(inputs.size(), values.size());
      }
      for(int i=0; i < values.size(); ++i) {
         textInputs.set(i, values.get(i));
      }
   }

   public void setInput(int index, double value){
      if(index >= arity()){
         throw new IndexOutOfBoundsException(index + " is greater or equal to input size" + arity());
      }
      inputs.set(index, value);
   }

   public void setInput(int index, String value){
      if(index >= arity()){
         throw new IndexOutOfBoundsException(index + " is greater or equal to input size" + arity());
      }
      textInputs.set(index, value);
   }

   public double getInput(int index) {
      if(index >= arity()){
         throw new IndexOutOfBoundsException(index + " is greater or equal to input size" + arity());
      }
      return inputs.get(index);
   }

   public String getTextInput(int index) {
      if(index >= arity()){
         throw new IndexOutOfBoundsException(index + " is greater or equal to input size" + arity());
      }
      return textInputs.get(index);
   }

   public void setValue(double val){
      if(readOnly){
         throw new RuntimeException("The primitive is readonly");
      }
      value = val;
   }

   public void setValue(String val) {
      if(readOnly){
         throw new RuntimeException("The primitive is readonly");
      }
      textValue = val;
   }

   public double getValue(){
      return value;
   }

   public String getTextValue() {
      return textValue;
   }

   public String getSymbol(){
      return symbol;
   }

   public boolean isReadOnly(){
      return readOnly;
   }


   @Override public int getIndex() {
      return index;
   }


   @Override public void setIndex(int index) {
      this.index = index;
   }


   @Override public abstract Primitive makeCopy();


   @Override public boolean equals(Object o) {
      if (this == o)
         return true;
      if (o == null || getClass() != o.getClass())
         return false;

      Primitive primitive = (Primitive) o;

      /*
      if (Double.compare(primitive.value, value) != 0)
         return false;
      */



      if (readOnly != primitive.readOnly)
         return false;

      if (index != primitive.index)
         return false;

      /*
      if (inputs != null ? !inputs.equals(primitive.inputs) : primitive.inputs != null)
         return false;
      */

      return symbol != null ? symbol.equals(primitive.symbol) : primitive.symbol == null;

   }


   @Override public int hashCode() {
      int result;
      long temp;
      result = 0;

      // result = inputs != null ? inputs.hashCode() : 0;
      //temp = Double.doubleToLongBits(value);
      // result = 31 * result + (int) (temp ^ (temp >>> 32));
      result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
      result = 31 * result + (readOnly ? 1 : 0);
      result = 31 * result + index;
      return result;
   }

   public void execute(Observation observation) {

   }

   public void executeWithText(Observation observation) {

   }
}
