package com.stephenwranger.compgeo.algorithms;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.Vector2d;

public class AlgorithmUtils {
   private AlgorithmUtils() {
      // statics only
   }
   
   public static void getRandomPoints(final int pointCount, final List<Tuple2d> output) {
      final int range = (int) Math.pow(10.0, Math.floor(Math.log10(pointCount)));
      final Random random = new Random();

      for (int i = 0; i < pointCount; i++) {
         output.add(new Tuple2d(random.nextInt(range), random.nextInt(range)));
      }
   }

   public static Comparator<Tuple2d> getComparator(final Tuple2d origin) {
      final Vector2d up = new Vector2d(0,1);

      return new Comparator<Tuple2d>() {
         @Override
         public int compare(final Tuple2d p0, final Tuple2d p1) {
            final Vector2d v0 = new Vector2d(p0.x - origin.x, p0.y - origin.y).normalize();
            final Vector2d v1 = new Vector2d(p1.x - origin.x, p1.y - origin.y).normalize();
            double angle0 = up.angleSigned(v0);
            double angle1 = up.angleSigned(v1);

            if(angle0 < 0) {
               angle0 = Math.PI + (Math.PI - Math.abs(angle0));
            }
            
            if(angle1 < 0) {
               angle1 = Math.PI + (Math.PI - Math.abs(angle1));
            }
            
            return (angle0 > angle1) ? -1 : (angle0 == angle1) ? 0 : 1;
         }
      };
   }
}
