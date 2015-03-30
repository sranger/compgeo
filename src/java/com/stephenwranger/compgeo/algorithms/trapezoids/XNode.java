package com.stephenwranger.compgeo.algorithms.trapezoids;

import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.IntersectionUtils;

/**
 * XNode contains a segment endpoint and child nodes to the left or right.
 */
public class XNode extends TrapezoidalMapNode {
   public final Tuple2d segmentEndpoint;
   public String        label = null;

   /**
    * Creates a new XNode with the given segmentEndpoint and the left/right child nodes.
    * 
    * @param segmentEndpoint
    * @param left
    * @param right
    */
   public XNode(final Tuple2d segmentEndpoint, final TrapezoidalMapNode left, final TrapezoidalMapNode right) {
      this.segmentEndpoint = segmentEndpoint;
      this.setLeftAbove(left);
      this.setRightBelow(right);
   }

   @Override
   public LeafNode queryMap(final Tuple2d queryPoint) {
      if (IntersectionUtils.isLessOrEqual(queryPoint.x, this.segmentEndpoint.x)) {
         return this.getLeftAbove().queryMap(queryPoint);
      } else {
         return this.getRightBelow().queryMap(queryPoint);
      }
   }

   @Override
   public void replaceChild(final TrapezoidalMapNode existingChild, final TrapezoidalMapNode newChild) {
      if (existingChild == this.getLeftAbove()) {
         this.setLeftAbove(newChild);
      } else if (existingChild == this.getRightBelow()) {
         this.setRightBelow(newChild);
      }
   }
}
