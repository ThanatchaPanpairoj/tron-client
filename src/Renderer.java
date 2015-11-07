import java.io.*;
import java.net.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JOptionPane;

import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.awt.Toolkit;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.image.BufferedImage;

public class Renderer extends JFrame {
    private long startTime;
    private int frame;
    private double mouseX, mouseY, numberOfDirectionsMoving, yRotation, xRotation;
    private boolean left, right, forward, backward, close;
    private static final double diagonalMoveSpeed = 50 / Math.sqrt(2);
    private RendererComponent comp;
    private String fromServer, toServer;

    public Renderer() throws Exception {
        super();

        startTime = System.currentTimeMillis();

        setCursor(getToolkit().createCustomCursor(
                new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB), new Point(0, 0),
                "null"));

        left = false;
        right = false;
        forward = false;
        backward = false;
        close = false;

        numberOfDirectionsMoving = 0;
        yRotation = 0;
        xRotation = 0;

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int)dim.getWidth(), (int)dim.getHeight());
        this.setTitle("Display");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        final int width = this.getWidth();
        final int height = this.getHeight();

        Robot robot = new Robot();
        robot.mouseMove(width / 2, height / 2);
        //System.out.println("" + width + ", " + height);

        //         frame.setUndecorated(true);
        //         frame.setShape(new Ellipse2D.Double(0,0, 800, 800));//circle frame?

        JPanel panel = new JPanel();
        panel.setDoubleBuffered(true);

        comp = new RendererComponent(width, height);

        class TimeListener implements ActionListener {
            public void actionPerformed(ActionEvent e){
                mouseX = MouseInfo.getPointerInfo().getLocation().getX();
                mouseY = MouseInfo.getPointerInfo().getLocation().getY();
                if(comp.hasFocus())	
                    robot.mouseMove(width / 2, height / 2);
                mouseX -= MouseInfo.getPointerInfo().getLocation().getX();
                mouseY -= MouseInfo.getPointerInfo().getLocation().getY();

                double speed = 50;
                if(numberOfDirectionsMoving > 1 && numberOfDirectionsMoving < 3) {
                    speed = diagonalMoveSpeed;
                }

                comp.transform(true, new double[] {1,                     0,                    0, 0, 
                        0,  Math.cos(-yRotation), Math.sin(-yRotation), 0, 
                        0, -Math.sin(-yRotation), Math.cos(-yRotation), 0, 
                        0,                     0,                    0, 1});

                double xSpinAngle = -mouseX / 400;
                comp.transform(true, new double[] {Math.cos(xSpinAngle), 0, Math.sin(xSpinAngle), 0,
                        0, 1,                    0, 0, 
                        -Math.sin(xSpinAngle), 0, Math.cos(xSpinAngle), 0, 
                        0, 0,                    0, 1});
                xRotation += xSpinAngle;

                if(left && !right) {
                    comp.transform(false, new double[] {1, 0, 0, speed, 
                            0, 1, 0,     0, 
                            0, 0, 1,     0, 
                            0, 0, 0,     1});
                } else if(right && !left) {
                    comp.transform(false, new double[] {1, 0, 0, -speed, 
                            0, 1, 0,      0, 
                            0, 0, 1,      0, 
                            0, 0, 0,      1});
                }

                if(forward && !backward) {
                    comp.transform(false, new double[] {1, 0, 0,      0, 
                            0, 1, 0,      0, 
                            0, 0, 1, -speed, 
                            0, 0, 0,      1});
                } else if(backward && !forward) {
                    comp.transform(false, new double[] {1, 0, 0,     0, 
                            0, 1, 0,     0, 
                            0, 0, 1, speed, 
                            0, 0, 0,     1});
                } 

                comp.transform(true, new double[] {Math.cos(-xRotation), 0, Math.sin(-xRotation), 0,
                        0, 1,                    0, 0, 
                        -Math.sin(-xRotation), 0, Math.cos(-xRotation), 0, 
                        0, 0,                    0, 1});

                if(fromServer != null) {
                    comp.updatePositions(fromServer);
                }

                toServer = -comp.getOriginX() + "a" + -comp.getOriginZ() + "a" + "0";

                comp.transform(true, new double[] {Math.cos(xRotation), 0, Math.sin(xRotation), 0,
                        0, 1,                    0, 0, 
                        -Math.sin(xRotation), 0, Math.cos(xRotation), 0, 
                        0, 0,                    0, 1});

                comp.transform(true, new double[] {1,                     0,                  0, 0, 
                        0,  Math.cos(yRotation), Math.sin(yRotation), 0, 
                        0, -Math.sin(yRotation), Math.cos(yRotation), 0, 
                        0,                     0,                  0, 1});

                double ySpinAngle = -mouseY / 400;
                if(yRotation + ySpinAngle < Math.PI / 2 && yRotation + ySpinAngle > -Math.PI / 2) {
                    comp.transform(true, new double[] {1,                     0,                    0, 0, 
                            0,  Math.cos(ySpinAngle), Math.sin(ySpinAngle), 0, 
                            0, -Math.sin(ySpinAngle), Math.cos(ySpinAngle), 0, 
                            0,                     0,                    0, 1});

                    yRotation += ySpinAngle;
                }

                comp.repaint();

                frame++;
                if(System.currentTimeMillis() - startTime >= 1000) {
                    startTime += 1000;
                    comp.updateFPS(frame);
                    frame = 0;
                }
            }
        }

        class KeyboardListener implements KeyListener {
            /**
             * Updates which keys are currently pressed.
             * 
             * @param  e  key pressed on the keyboard
             * @return    void
             */
            public void keyPressed(KeyEvent e)
            {
                int k = e.getKeyCode();
                if(k ==  KeyEvent.VK_ESCAPE) {
                    close = true;
                } else if(k == KeyEvent.VK_A) {
                    if(!left)
                        numberOfDirectionsMoving++;
                    left = true;
                } else if (k == KeyEvent.VK_D) {
                    if(!right)
                        numberOfDirectionsMoving++;
                    right = true;
                } else if (k == KeyEvent.VK_W) {
                    if(!forward)
                        numberOfDirectionsMoving++;
                    forward = true;
                } else if (k == KeyEvent.VK_S) {
                    if(!backward)
                        numberOfDirectionsMoving++;
                    backward = true;
                } 
            }

            /**
             * Updates when a key is released.
             * 
             * @param  e  key released from the keyboard
             * @return    void
             */
            public void keyReleased(KeyEvent e) {
                int k = e.getKeyCode();
                if(k == KeyEvent.VK_A) {
                    left = false;
                    numberOfDirectionsMoving--;
                } else if (k == KeyEvent.VK_D) {
                    right = false;
                    numberOfDirectionsMoving--;
                } else if (k == KeyEvent.VK_W) {
                    forward = false;
                    numberOfDirectionsMoving--;
                } else if (k == KeyEvent.VK_S) {
                    backward = false;
                    numberOfDirectionsMoving--;
                } 
            }

            /**
             * Updates when a key is typed.
             * 
             * @param  e  key typed on the keyboard
             * @return    void
             */
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
            }
        }

        class MousePressListener implements MouseListener
        {
            /**
             * Updates when the mouse button is pressed.
             * 
             * @param  event  mouse button press
             * @return        void
             */
            public void mousePressed(MouseEvent event)
            {

            }

            /**
             * Updates when the mouse button is released.
             * 
             * @param  event  mouse button is released
             * @return        void
             */
            public void mouseReleased(MouseEvent event) {
                comp.click();
            }

            public void mouseClicked(MouseEvent event) {}

            public void mouseEntered(MouseEvent event) {}

            public void mouseExited(MouseEvent event) {}
        }

        class ScrollListener implements MouseWheelListener
        {
            public void mouseWheelMoved(MouseWheelEvent e) {

            }
        }
        comp.setPreferredSize(new Dimension(width, height));
        comp.addKeyListener(new KeyboardListener());
        comp.addMouseListener(new MousePressListener());
        comp.addMouseWheelListener(new ScrollListener());
        comp.setBounds(0, 0, width, height);
        comp.setFocusable(true);
        comp.setVisible(true);

        final int DELAY = 1000 / 60;//60 frames per second
        Timer t = new Timer(DELAY, new TimeListener());
        t.start();

        panel.setLayout(null);

        panel.add(comp);
        this.add(panel);

        //frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        this.setVisible(true);

        setResizable(false);
        comp.requestFocus();

    }

    public void updatePositions(String fromServer) {
        this.fromServer = fromServer;
    }

    public String getPosition() {
        if(close)
            return "b";
        return toServer;
    }
}