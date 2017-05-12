package com.github.chen0040.gp.lgp.program;


/**
 * Created by xschen on 28/4/2017.
 * Containers for the read-only registers
 */
public class ConstantSet extends RegisterSet {
   public ConstantSet(){
      super();
   }

   @Override
   public boolean isReadOnly(){
      return true;
   }

   @Override
   public RegisterSet makeCopy(){
      RegisterSet clone = new ConstantSet();
      clone.copy(this);
      return clone;
   }

}
