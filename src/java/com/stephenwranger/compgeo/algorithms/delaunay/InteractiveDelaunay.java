package com.stephenwranger.compgeo.algorithms.delaunay;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.Tuple3d;
import com.stephenwranger.graphics.math.intersection.IntersectionUtils;
import com.stephenwranger.graphics.math.intersection.Triangle2D;
import com.stephenwranger.graphics.renderables.Circle;
import com.stephenwranger.graphics.utils.Iterative;
import com.stephenwranger.graphics.utils.IterativeListener;
import com.stephenwranger.graphics.utils.TimeUtils;

public class InteractiveDelaunay {
   private static boolean isEnableCircles = false;

   public static void main(final String[] args) {
      final JFrame frame = new JFrame("Interactive Delaunay Triangulation");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      final int width = 800;
      final int height = 500;
      final List<Point> clicks = new ArrayList<Point>();
      final Triangle2D boundingTriangle = new Triangle2D(new Tuple2d(0, 0), new Tuple2d(width * 10, 0), new Tuple2d(0, height * 10));
      final DelaunayTriangulation dt = new DelaunayTriangulation(boundingTriangle);
      dt.addIterativeListener(new IterativeListener() {
         @Override
         public void step(final Iterative source, final String message, final List<Object> payload) {

            // TODO: get this to pause for the call to continueIteration
            System.out.println(message + ": " + payload.size());
            source.continueIteration();
         }
      });

      final JPanel panel = new JPanel() {
         private static final long serialVersionUID = -5113240506547207623L;

         @Override
         public void paint(final Graphics g) {
            g.setColor(new Color(0.8f, 0.8f, 0.8f));
            g.fillRect(0, 0, this.getWidth(), this.getHeight());

            Tuple2d[] corners;
            Circle c;
            Tuple3d bary;

            mainLoop: for (final Triangle2D t : dt.getTriangles()) {
               corners = t.getCorners();

               // don't draw if it's attached to the bounding triangle
               for (final Tuple2d corner : corners) {
                  bary = boundingTriangle.getBarycentricCoordinate(corner);

                  if (IntersectionUtils.isEqual(bary.x, 0.0) || IntersectionUtils.isEqual(bary.y, 0.0) || IntersectionUtils.isEqual(bary.z, 0.0)) {
                     continue mainLoop;
                  }
               }

               final Point mousePos = MouseInfo.getPointerInfo().getLocation();
               final Point onScreen = this.getLocationOnScreen();
               final boolean contains = t.contains(new Tuple2d(mousePos.x - onScreen.x, mousePos.y - onScreen.y));
               if (contains) {
                  ((Graphics2D) g).setStroke(new BasicStroke(3f));
               } else {
                  ((Graphics2D) g).setStroke(new BasicStroke(1f));
               }

               g.setColor(Color.DARK_GRAY.darker().darker());
               g.drawPolygon(new int[] { (int) corners[0].x, (int) corners[1].x, (int) corners[2].x }, new int[] { (int) corners[0].y, (int) corners[1].y,
                     (int) corners[2].y }, 3);

               if (InteractiveDelaunay.isEnableCircles) {
                  c = t.getCircumscribedCircle();

                  if (contains) {
                     g.setColor(Color.red);
                  } else {
                     g.setColor(Color.blue);
                  }

                  g.drawOval((int) (c.getCenter().x - c.getRadius()), (int) (c.getCenter().y - c.getRadius()), (int) (c.getRadius() * 2.0),
                        (int) (c.getRadius() * 2.0));

                  ((Graphics2D) g).setStroke(new BasicStroke(1f));
               }
            }

            g.setColor(Color.DARK_GRAY);

            for (final Point vert : clicks) {
               g.fillOval(vert.x - 3, vert.y - 3, 6, 6);
            }
         }
      };

      final MouseAdapter mouseListener = new MouseAdapter() {
         long lastAddition = 0;

         @Override
         public void mouseClicked(final MouseEvent e) {
            clicks.add(e.getPoint());
            dt.addVertex(new Tuple2d(e.getX(), e.getY()));
            panel.repaint();
         }

         @Override
         public void mouseDragged(final MouseEvent e) {
            if (System.nanoTime() - this.lastAddition > 200 * TimeUtils.NANOSECONDS_TO_MILLISECONDS) {
               this.mouseClicked(e);
               this.lastAddition = System.nanoTime();
            }
         }

         @Override
         public void mouseMoved(final MouseEvent e) {
            panel.repaint();
         }
      };

      panel.setPreferredSize(new Dimension(800, 500));
      panel.addMouseListener(mouseListener);
      panel.addMouseMotionListener(mouseListener);

      final JCheckBox enableCircles = new JCheckBox("Enable Circumcircles");
      enableCircles.setSelected(false);
      enableCircles.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(final ActionEvent e) {
            InteractiveDelaunay.isEnableCircles = enableCircles.isSelected();
            panel.repaint();
         }
      });

      final JButton clear = new JButton("Clear");
      clear.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(final ActionEvent e) {
            dt.clear();
            panel.repaint();
            clicks.clear();
         }
      });

      final JPanel leftPanel = new JPanel();
      leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
      leftPanel.add(enableCircles);
      leftPanel.add(clear);
      leftPanel.setMaximumSize(new Dimension(200, 500));

      final Container content = frame.getContentPane();
      content.setLayout(new BorderLayout());
      content.add(panel, BorderLayout.CENTER);
      content.add(leftPanel, BorderLayout.EAST);

      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            frame.setLocation(200, 200);
            frame.pack();
            frame.setVisible(true);
         }
      });
   }
}
