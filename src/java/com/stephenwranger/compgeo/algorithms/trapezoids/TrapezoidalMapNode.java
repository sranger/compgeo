package com.stephenwranger.compgeo.algorithms.trapezoids;

import java.util.HashSet;
import java.util.Set;

import com.stephenwranger.graphics.math.Tuple2d;

/**
 * Abstract class for a Trapezoidal Map Node.
 */
public abstract class TrapezoidalMapNode {
   public Set<TrapezoidalMapNode> parents    = new HashSet<TrapezoidalMapNode>();
   private TrapezoidalMapNode leftAbove  = null;
   private TrapezoidalMapNode rightBelow = null;
   private String                 label      = null;
   private int                    index      = -1;

   /**
    * Returns the set of parent nodes to this node; not a deep-copy so only modify in-line if this node's list should change.
    *
    * @return
    */
   public Set<TrapezoidalMapNode> getParentNodes() {
      return this.parents;
   }

   /**
    * Adds the given node as a parent to this node.
    *
    * @param parent
    * @param oldChild
    */
   public void addParentNode(final TrapezoidalMapNode parent, final TrapezoidalMapNode oldChild) {
      this.parents.add(parent);

      if (oldChild != null) {
         parent.replaceChild(oldChild, this);
      }
   }

   /**
    * Adds the given node as a parent to this node.
    *
    * @param parent
    * @param oldChild
    */
   public void addParentNode(final Set<TrapezoidalMapNode> parents, final TrapezoidalMapNode oldChild) {
      this.parents.addAll(parents);

      if (oldChild != null) {
         for (final TrapezoidalMapNode parent : parents) {
            parent.replaceChild(oldChild, this);
         }
      }
   }

   /**
    * Removes the given node as a parent to this node.
    *
    * @param parent
    */
   public void removeParentNode(final TrapezoidalMapNode parent) {
      this.parents.remove(parent);
   }

   /**
    * Returns the left/above node to this node.
    *
    * @return
    */
   public TrapezoidalMapNode getLeftAbove() {
      return this.leftAbove;
   }

   /**
    * Sets the current left/above node to this node.
    *
    * @param leftAbove
    */
   public void setLeftAbove(final TrapezoidalMapNode leftAbove) {
      this.leftAbove = leftAbove;

      if (this.leftAbove != null) {
         this.leftAbove.addParentNode(this, null);
      }
   }

   /**
    * Return the right/below node to this node.
    *
    * @param leftAbove
    */
   public TrapezoidalMapNode getRightBelow() {
      return this.rightBelow;
   }

   /**
    * Sets the current right/below node to this node.
    *
    * @param rightBelow
    */
   public void setRightBelow(final TrapezoidalMapNode rightBelow) {
      this.rightBelow = rightBelow;

      if (this.rightBelow != null) {
         this.rightBelow.addParentNode(this, null);
      }
   }

   /**
    * Searches for the leaf node containing the given query point.
    *
    * @param queryPoint
    * @return
    */
   public abstract LeafNode queryMap(final Tuple2d queryPoint);

   /**
    * Replaces the child of this node with the given new child node.
    *
    * @param existingChild
    * @param newChild
    */
   public void replaceChild(final TrapezoidalMapNode existingChild, final TrapezoidalMapNode newChild) {
      if (existingChild == this.getLeftAbove()) {
         this.setLeftAbove(newChild);
      } else if (existingChild == this.getRightBelow()) {
         this.setRightBelow(newChild);
      }
   }

   public void setLabel(final String label) {
      this.label = label;
   }

   public String getLabel() {
      return this.label;
   }

   public void setIndex(final int index) {
      this.index = index;
   }

   public int getIndex() {
      return this.index;
   }
}
