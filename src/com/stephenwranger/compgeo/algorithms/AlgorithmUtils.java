package com.stephenwranger.compgeo.algorithms;

import java.util.Comparator;

import com.stephenwranger.graphics.math.Tuple2d;

public class AlgorithmUtils {
   private AlgorithmUtils() {
      // statics only
   }

   public static Comparator<Tuple2d> getComparator() {
      return new Comparator<Tuple2d>() {
         @Override
         public int compare(final Tuple2d v0, final Tuple2d v1) {
            final int xCompare = Double.compare(v0.x, v1.x);
            final int yCompare = Double.compare(v0.y, v1.y);

            return (xCompare == 0) ? yCompare : xCompare;
         }
      };
   }
}
