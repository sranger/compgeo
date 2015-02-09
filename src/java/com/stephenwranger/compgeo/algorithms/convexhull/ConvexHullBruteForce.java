package com.stephenwranger.compgeo.algorithms.convexhull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.stephenwranger.compgeo.algorithms.Algorithm;
import com.stephenwranger.compgeo.algorithms.AlgorithmUtils;
import com.stephenwranger.graphics.collections.Pair;
import com.stephenwranger.graphics.math.Tuple2d;

public class ConvexHullBruteForce implements Algorithm<Tuple2d> {

   public ConvexHullBruteForce() {
      // nothing
   }

   @Override
   public void compute(final List<Tuple2d> input, final List<Tuple2d> output, final List<Pair<Tuple2d, Tuple2d>> outputEdges) {
      final Set<Tuple2d> outputSet = new HashSet<Tuple2d>();
      final Set<Pair<Tuple2d, Tuple2d>> outputEdgesSet = new HashSet<Pair<Tuple2d, Tuple2d>>();
      Tuple2d vi, vj, vk;
      double cA, cB, cC, temp;
      boolean isValid;
      int currentSign;

      for (int i = 0; i < input.size(); i++) {
         for (int j = 0; j < input.size(); j++) {
            vi = input.get(i);
            vj = input.get(j);

            if (vi != vj && !vi.equals(vj)) {
               cA = vj.y - vi.y;
               cB = vi.x - vj.x;
               cC = vi.x * vj.y - vi.y * vj.x;

               isValid = true;
               currentSign = 0;

               /*
                * check if all other points lie on the same side of the line (xi,yi) and (xj,yj) i.e solving axk+byk-c=0 => xk(yi-yj)+(xi-xj)yk-xiyi+yixj=0
                * gives you either 0 or a positive number or a negative number add (xi,yi) to (xj,yj) to the list/vector of extreme points
                */
               for(int k = 0; k < input.size(); k++) {
                  vk = input.get(k);

                  if ((vk != vi) && !vk.equals(vi) && (vk != vj) && !vk.equals(vj)) {
                     temp = cA * vk.x + cB * vk.y - cC;

                     if (currentSign != 0) {
                        if (temp > 0 && currentSign < 0) {
                           isValid = false;
                           break;
                        } else if (temp < 0 && currentSign > 0) {
                           isValid = false;
                           break;
                        }
                     } else {
                        if (temp > 0) {
                           currentSign = 1;
                        } else if (temp < 0) {
                           currentSign = -1;
                        }
                     }
                  }
               }

               if (isValid) {
                  outputSet.add(vi);
                  outputSet.add(vj);
                  outputEdgesSet.add(Pair.getInstance(vi, vj));
               }
            }
         }
      }

      output.addAll(outputSet);
      outputEdges.addAll(outputEdgesSet);
      Collections.sort(output, AlgorithmUtils.getComparator());
   }
}
