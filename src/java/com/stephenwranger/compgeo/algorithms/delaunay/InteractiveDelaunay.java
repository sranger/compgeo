package com.stephenwranger.compgeo.algorithms.delaunay;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileFilter;

import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.Tuple3d;
import com.stephenwranger.graphics.math.intersection.IntersectionUtils;
import com.stephenwranger.graphics.math.intersection.LineSegment;
import com.stephenwranger.graphics.math.intersection.Triangle2d;
import com.stephenwranger.graphics.renderables.Circle;
import com.stephenwranger.graphics.utils.Iterative;
import com.stephenwranger.graphics.utils.IterativeListener;
import com.stephenwranger.graphics.utils.TimeUtils;
import com.stephenwranger.graphics.utils.models.PlyModelLoader;
import com.stephenwranger.graphics.utils.models.VertexListLoader;

public class InteractiveDelaunay {
   private static boolean isEnableCircles = false;

   private enum FileMode {
      LOAD, SAVE;
   }

   public static void main(final String[] args) {
      final JFrame frame = new JFrame("Interactive Delaunay Triangulation");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      final int width = 800;
      final int height = 500;
      final List<Point> clicks = new ArrayList<Point>();
      final Triangle2d boundingTriangle = new Triangle2d(new Tuple2d(0, 0), new Tuple2d(width * 10, 0), new Tuple2d(0, height * 10));
      final DelaunayTriangulation dt = new DelaunayTriangulation(boundingTriangle);
      final List<Tuple2d> eventVertices = new CopyOnWriteArrayList<Tuple2d>();
      final List<LineSegment> eventEdges = new CopyOnWriteArrayList<LineSegment>();
      final List<Circle> eventCircles = new CopyOnWriteArrayList<Circle>();
      final List<Triangle2d> eventTriangles = new CopyOnWriteArrayList<Triangle2d>();

      final JPanel panel = new JPanel() {
         private static final long serialVersionUID = -5113240506547207623L;

         @Override
         public void paint(final Graphics g) {
            g.setColor(new Color(0.8f, 0.8f, 0.8f));
            g.fillRect(0, 0, this.getWidth(), this.getHeight());

            Tuple2d[] corners;
            Circle c;
            Tuple3d bary;

            mainLoop: for (final Triangle2d t : dt.getTriangles()) {
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

            g.setColor(Color.green);
            ((Graphics2D) g).setStroke(new BasicStroke(2f));

            for(final LineSegment edge : eventEdges) {
               g.drawLine((int)edge.min.x, (int)edge.min.y, (int)edge.max.x, (int)edge.max.y);
            }

            for(final Triangle2d triangle : eventTriangles) {
               for(final LineSegment edge : triangle.getLineSegments()) {
                  g.drawLine((int)edge.min.x, (int)edge.min.y, (int)edge.max.x, (int)edge.max.y);
               }
            }

            g.setColor(Color.green.darker());
            for(final Circle circle : eventCircles) {
               g.drawOval((int) (circle.getCenter().x - circle.getRadius()), (int) (circle.getCenter().y - circle.getRadius()), (int) (circle.getRadius() * 2.0),
                     (int) (circle.getRadius() * 2.0));
            }

            g.setColor(Color.green.darker().darker());
            for(final Tuple2d vertex : eventVertices) {
               g.fillOval((int)(vertex.x - 5), (int)(vertex.y - 5), 10, 10);
            }
         }
      };

      final JPanel messagePanel = new JPanel(new GridLayout(1, 1));
      messagePanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.GRAY, Color.DARK_GRAY));
      final JTextArea messageArea = new JTextArea();
      messageArea.setWrapStyleWord(true);
      messageArea.setLineWrap(true);
      final JScrollPane messagePane = new JScrollPane(messageArea);
      messagePane.setPreferredSize(new Dimension(1000, 100));
      messagePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      messagePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      messagePanel.add(messagePane);

      final MouseAdapter mouseListener = new MouseAdapter() {
         long lastAddition = 0;

         @Override
         public void mouseClicked(final MouseEvent e) {
            if(!dt.isBusy()) {
               clicks.add(e.getPoint());
               dt.addVertex(new Tuple2d(e.getX(), e.getY()));
               panel.repaint();
            }
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
      enableCircles.setMaximumSize(new Dimension(200, 30));
      enableCircles.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(final ActionEvent e) {
            InteractiveDelaunay.isEnableCircles = enableCircles.isSelected();
            panel.repaint();
         }
      });

      final JButton clear = new JButton("Clear");
      clear.setMaximumSize(new Dimension(200, 30));
      clear.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(final ActionEvent e) {
            dt.clear();
            panel.repaint();
            clicks.clear();
            eventVertices.clear();
            eventEdges.clear();
            eventCircles.clear();
            eventTriangles.clear();
         }
      });

      // TODO: add file of points ingestion


      final JCheckBox useDelay = new JCheckBox("Use Step Delay");
      useDelay.setSelected(false);
      useDelay.setMaximumSize(new Dimension(200, 30));

      final JLabel delayLabel = new JLabel("Step Delay (ms)");
      final JSpinner delay = new JSpinner(new SpinnerNumberModel(100, 0, Integer.MAX_VALUE, 1));
      final JPanel delayPanel = new JPanel();
      delayPanel.setLayout(new BoxLayout(delayPanel, BoxLayout.Y_AXIS));
      delayPanel.setMaximumSize(new Dimension(200, 60));
      delayPanel.add(delayLabel);
      delayPanel.add(delay);

      final JButton loadFilePlyButton = new JButton("Load PLY File");
      loadFilePlyButton.addActionListener(InteractiveDelaunay.getFileListener(loadFilePlyButton, dt, ".ply", FileMode.LOAD));
      final JButton loadFileVl2Button = new JButton("Load VL2 File");
      loadFileVl2Button.addActionListener(InteractiveDelaunay.getFileListener(loadFileVl2Button, dt, ".vl2", FileMode.LOAD));

      final JButton saveFilePlyButton = new JButton("Save PLY File");
      saveFilePlyButton.addActionListener(InteractiveDelaunay.getFileListener(saveFilePlyButton, dt, ".ply", FileMode.SAVE));
      final JButton saveFileVl2Button = new JButton("Save VL2 File");
      saveFileVl2Button.addActionListener(InteractiveDelaunay.getFileListener(saveFileVl2Button, dt, ".vl2", FileMode.SAVE));

      final JPanel leftPanel = new JPanel();
      leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
      enableCircles.setAlignmentX( Component.LEFT_ALIGNMENT );
      leftPanel.add(enableCircles);
      clear.setAlignmentX( Component.LEFT_ALIGNMENT );
      leftPanel.add(clear);
      useDelay.setAlignmentX( Component.LEFT_ALIGNMENT );
      leftPanel.add(useDelay);
      delayPanel.setAlignmentX( Component.LEFT_ALIGNMENT );
      leftPanel.add(delayPanel);

      loadFilePlyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      leftPanel.add(loadFilePlyButton);

      loadFileVl2Button.setAlignmentX(Component.LEFT_ALIGNMENT);
      leftPanel.add(loadFileVl2Button);

      saveFilePlyButton.setAlignmentX(Component.LEFT_ALIGNMENT);
      leftPanel.add(saveFilePlyButton);

      saveFileVl2Button.setAlignmentX(Component.LEFT_ALIGNMENT);
      leftPanel.add(saveFileVl2Button);

      leftPanel.setMaximumSize(new Dimension(200, 500));
      leftPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED, Color.GRAY, Color.DARK_GRAY));

      final Container content = frame.getContentPane();
      content.setLayout(new BorderLayout());
      content.add(panel, BorderLayout.CENTER);
      content.add(leftPanel, BorderLayout.EAST);
      content.add(messagePanel, BorderLayout.SOUTH);

      dt.addIterativeListener(new IterativeListener() {
         @Override
         public void step(final Iterative source, final String message, final List<Object> payload) {
            messageArea.append("\n" + message);
            messageArea.setCaretPosition(messageArea.getText().length());
            eventVertices.clear();
            eventEdges.clear();
            eventCircles.clear();
            eventTriangles.clear();

            for(final Object p : payload) {
               if(p instanceof Tuple2d) {
                  eventVertices.add((Tuple2d)p);
               } else if(p instanceof LineSegment) {
                  eventEdges.add((LineSegment)p);
               } else if(p instanceof Circle) {
                  eventCircles.add((Circle)p);
               } else if(p instanceof Triangle2d) {
                  eventTriangles.add((Triangle2d)p);
               }
            }

            if(useDelay.isSelected()) {
               try {
                  Thread.sleep(100);
               } catch (final InterruptedException e) {
                  e.printStackTrace();
               }
            }

            source.continueIteration();
            panel.repaint();
         }
      });

      SwingUtilities.invokeLater(new Runnable() {
         @Override
         public void run() {
            frame.setLocation(200, 200);
            frame.pack();
            frame.setVisible(true);
         }
      });
   }

   private static void loadFile(final File file, final DelaunayTriangulation dt) throws FileNotFoundException, IOException {
      final List<Tuple2d> vertices = new ArrayList<Tuple2d>();

      if (file.getAbsolutePath().endsWith(".ply")) {
         final PlyModelLoader loader = new PlyModelLoader();
         final List<Tuple3d> outputVertices = new ArrayList<Tuple3d>();
         loader.loadModel(file, null, outputVertices);

         for (final Tuple3d vertex : outputVertices) {
            vertices.add(new Tuple2d(vertex));
         }
      } else if (file.getAbsolutePath().endsWith(".vl2")) {
         VertexListLoader.loadVertexList2d(file, vertices);
      }

      for (final Tuple2d vertex : vertices) {
         dt.addVertex(vertex);
      }
   }

   private static ActionListener getFileListener(final Component parent, final DelaunayTriangulation dt, final String extension, final FileMode mode) {
      final JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      fc.setAcceptAllFileFilterUsed(false);
      fc.setFileFilter(new FileFilter() {
         @Override
         public boolean accept(File pathname) {
            return pathname.getAbsolutePath().endsWith(extension);
         }

         @Override
         public String getDescription() {
            return extension;
         }
      });

      return new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            int value = -1;
            if (mode == FileMode.LOAD) {
               value = fc.showOpenDialog(parent);

               if (value == JFileChooser.APPROVE_OPTION) {
                  try {
                     InteractiveDelaunay.loadFile(fc.getSelectedFile(), dt);
                  } catch (final IOException e1) {
                     e1.printStackTrace();
                  }
               }
            } else if(mode == FileMode.SAVE) {
               value = fc.showSaveDialog(parent);

               if (value == JFileChooser.APPROVE_OPTION) {
                  File file = fc.getSelectedFile();

                  if (!file.getAbsolutePath().endsWith(extension)) {
                     file = new File(file.getAbsolutePath() + extension);
                  }

                  try {
                     if (extension.equals(".ply")) {
                        final Set<Triangle2d> triangles = new HashSet<Triangle2d>(dt.getImmutableTriangles());

                        PlyModelLoader.writePlyModel2d(file, triangles);
                     } else if (extension.equals(".vl2")) {
                        final Set<Tuple2d> vertices = new HashSet<Tuple2d>(dt.getImmutableVertices());

                        VertexListLoader.writeVertexList2d(file, vertices);
                     }
                  } catch (final IOException ex) {
                     ex.printStackTrace();
                  }
               }
            }
         }
      };
   }
}
