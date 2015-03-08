package com.stephenwranger.compgeo.algorithms.segments;

import java.util.List;

import com.stephenwranger.compgeo.algorithms.Algorithm;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.renderables.LineSegment;

public class BruteForceSegmentIntersectionAlgorithm implements Algorithm<LineSegment, Tuple2d> {

   @Override
   public boolean compute(final List<LineSegment> input, final List<Tuple2d> output, final long timeout) {
      LineSegment sI, sJ;
      Tuple2d intersection;

      final long startTime = System.nanoTime();

      for (int i = 0; i < input.size() - 1; i++) {
         sI = input.get(i);

         for (int j = i + 1; j < input.size(); j++) {
            if ((System.nanoTime() - startTime) / 1000000l > timeout) {
               return false;
            }

            sJ = input.get(j);
            intersection = sI.intersect(sJ);

            if (intersection != null) {
               if (output != null) {
                  output.add(intersection);
               }
            }
         }
      }

      return true;
   }

}
