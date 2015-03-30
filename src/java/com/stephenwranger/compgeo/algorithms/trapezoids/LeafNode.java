package com.stephenwranger.compgeo.algorithms.trapezoids;

import java.util.ArrayList;
import java.util.List;

import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.IntersectionUtils;
import com.stephenwranger.graphics.math.intersection.LineSegment;
import com.stephenwranger.graphics.math.intersection.Trapezoid;

/**
 * A LeafNode contains a Trapezoid with no child nodes.
 */
public class LeafNode extends TrapezoidalMapNode {
   private final Trapezoid trapezoid;
   private final List<LeafNode> neighbors = new ArrayList<LeafNode>();

   /**
    * Creates a new LeafNode.
    *
    * @param trapezoid
    * @param neighbors
    */
   public LeafNode(final Trapezoid trapezoid, final LeafNode[] neighbors) {
      this.trapezoid = trapezoid;

      if (neighbors != null) {
         for (final LeafNode node : neighbors) {
            if (!this.neighbors.contains(node)) {
               this.neighbors.add(node);
               node.addNeighbor(this);
            }
         }
      }
   }

   /**
    * Returns the neighbor leaf node to the right of this leaf node that the given segment intersects.
    *
    * @param segment
    * @return
    */
   public LeafNode getNeighbor(final LineSegment segment) {
      LeafNode retVal = null;

      for (final LeafNode node : this.neighbors) {
         if (IntersectionUtils.isEqual(node.getTrapezoid().getLeft().x, this.trapezoid.getRight().x) && node.getTrapezoid().contains(segment)) {
            retVal = node;
         }
      }

      return retVal;
   }

   /**
    * Returns the number of neighboring leaf nodes.
    *
    * @return
    */
   public int getNeighborCount() {
      return this.neighbors.size();
   }

   public void addNeighbor(final LeafNode neighbor) {
      if (this != neighbor && !this.neighbors.contains(neighbor)) {
         final LineSegment thisLeft = this.trapezoid.getLeft();
         final LineSegment thisRight = this.trapezoid.getRight();

         final LineSegment otherLeft = neighbor.trapezoid.getLeft();
         final LineSegment otherRight = neighbor.trapezoid.getRight();

         if (IntersectionUtils.isEqual(thisLeft.x, otherRight.x)) {
            if (IntersectionUtils.overlap(thisLeft.min.y, thisLeft.max.y, otherRight.min.y, otherRight.max.y)) {
               this.neighbors.add(neighbor);
               neighbor.addNeighbor(this);
            }
         } else if (IntersectionUtils.isEqual(thisRight.x, otherLeft.x)) {
            if (IntersectionUtils.overlap(thisRight.min.y, thisRight.max.y, otherLeft.min.y, otherLeft.max.y)) {
               this.neighbors.add(neighbor);
               neighbor.addNeighbor(this);
            }
         }
      }
   }

   /**
    * Removes the given neighbor leaf node and removes itself from the given neighbor as well.
    *
    * @param neighbor
    */
   public void removeNeighbor(final LeafNode neighbor) {
      if (neighbor != this && this.neighbors.contains(neighbor)) {
         this.neighbors.remove(neighbor);
         neighbor.removeNeighbor(this);
      }
   }

   /**
    * Removes this leaf node as a neighbor to all of its current neighbors.
    */
   public void removeAsNeighbor() {
      final List<LeafNode> toRemove = new ArrayList<LeafNode>(this.neighbors);
      this.neighbors.clear();

      for (final LeafNode node : toRemove) {
         node.removeNeighbor(this);
      }
   }

   /**
    * Checks each given leaf node if it is a neighbor to this leaf node and if so, adds it to its neighbor list.
    *
    * @param potentialNeighbors
    */
   public void addNeighbors(final LeafNode[] potentialNeighbors) {
      for (final LeafNode node : potentialNeighbors) {
         this.addNeighbor(node);
      }
   }

   /**
    * Returns all neighbors to the left of this leaf node.
    *
    * @return
    */
   public LeafNode[] getNeighborsLeft() {
      final List<LeafNode> leftNeighbors = new ArrayList<LeafNode>();

      for (final LeafNode neighbor : this.neighbors) {
         if (IntersectionUtils.isEqual(neighbor.trapezoid.getRight().x, this.trapezoid.getLeft().x)) {
            leftNeighbors.add(neighbor);
         }
      }

      return leftNeighbors.toArray(new LeafNode[leftNeighbors.size()]);
   }

   /**
    * Returns all neighbors to the right of this leaf node.
    *
    * @return
    */
   public LeafNode[] getNeighborsRight() {
      final List<LeafNode> rightNeighbors = new ArrayList<LeafNode>();

      for (final LeafNode neighbor : this.neighbors) {
         if (IntersectionUtils.isEqual(neighbor.trapezoid.getLeft().x, this.trapezoid.getRight().x)) {
            rightNeighbors.add(neighbor);
         }
      }

      return rightNeighbors.toArray(new LeafNode[rightNeighbors.size()]);
   }

   @Override
   public TrapezoidalMapNode getLeftAbove() {
      return null;
   }

   @Override
   public TrapezoidalMapNode getRightBelow() {
      return null;
   }

   /**
    * Returns this leaf node's Trapezoid.
    *
    * @return
    */
   public Trapezoid getTrapezoid() {
      return this.trapezoid;
   }

   @Override
   public LeafNode queryMap(Tuple2d queryPoint) {
      return this;
   }

   @Override
   public void replaceChild(final TrapezoidalMapNode existingChild, final TrapezoidalMapNode newChild) {
      throw new UnsupportedOperationException("Leaf Nodes cannot have children.");
   }
}
