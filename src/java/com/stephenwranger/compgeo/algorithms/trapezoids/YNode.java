package com.stephenwranger.compgeo.algorithms.trapezoids;

import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.IntersectionUtils;
import com.stephenwranger.graphics.math.intersection.LineSegment;

/**
 * YNode contains a line segment and child nodes above and below.
 */
public class YNode extends TrapezoidalMapNode {
   public final LineSegment segment;

   /**
    * Creates a new YNode with the given segment and above/below child nodes.
    * 
    * @param segment
    * @param above
    * @param below
    */
   public YNode(final LineSegment segment, final TrapezoidalMapNode above, final TrapezoidalMapNode below) {
      this.segment = segment;
      this.setLeftAbove(above);
      this.setRightBelow(below);
   }

   @Override
   public LeafNode queryMap(Tuple2d queryPoint) {
      if (IntersectionUtils.isGreaterThan(queryPoint.y, this.segment.a * queryPoint.x + this.segment.b)) {
         return this.getLeftAbove().queryMap(queryPoint);
      } else {
         return this.getRightBelow().queryMap(queryPoint);
      }
   }
}
