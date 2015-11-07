import java.lang.Double;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JComponent;

/**
 * Basic GUI component GUITemplateComponent
 * 
 * @author Thanatcha Panpairoj
 * @version (a version number or a date)
 */
public class RendererComponent extends JComponent
{
    private static final Color BLUE = new Color(153, 204, 255), ORANGE = new Color(255, 204, 0), PURPLE = new Color(204, 51, 153), GREEN = new Color(153, 255, 51), WHITE = new Color(230, 230, 230), YELLOW = new Color(255, 255, 102);
    private int width, height, fps;
    private ArrayList<Shape> shapes;
    private ArrayList<Line> grid;
    private Face floor;
    private String usersOnline;
    private Point origin, corner1, corner2, corner3, corner4;
    private User user;

    public RendererComponent(int width, int height) {
        this.width = width;
        this.height = height;

        shapes = new ArrayList<Shape>();
        shapes.add(user = new User(BLUE, 0, 0, 0));

        grid = new ArrayList<Line>();
        for(int w = -100000; w <= 100000; w += 400) {
            grid.add(new Line(new Point(w, 600, -100000, 1), new Point(w, 600, 100000, 1)));
            grid.add(new Line(new Point(-100000, 600, w, 1), new Point(100000, 600, w, 1)));
        }

        corner1 = new Point(-100000.1, 600, -100000.2, 1);
        corner2 = new Point(100000.3, 600, -100000.4, 1);
        corner3 = new Point(100000.5, 600, 100000.6, 1);
        corner4 = new Point(-100000.7, 600, 100000.8, 1);
        floor = new Face(new Point(0, 600, 0, 1), new Color(0, 0, 20), new Line(corner1, corner2), new Line(corner2, corner3), new Line(corner3, corner4), new Line(corner1, corner4)); 
        origin = new Point(0, 0, 0, 1);
        usersOnline = "";
    }

    public void paintComponent(Graphics g) {
        if(!usersOnline.equals("")) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D)g;
            g2.setColor(new Color(43, 54, 72));
            g2.fillRect(0, 0, width, height);
            g2.translate(width / 2, height / 2);

            transform(true, new double[] {1, 0, 0,     0, 
                    0, 1, 0,     0, 
                    0, 0, 1, 600, 
                    0, 0, 0,     1});

            floor.draw(g2);

            g2.setColor(BLUE);
            for(Line l : grid) {
                l.draw(g2);
            }

            g2.drawString("WASD to move", -width / 2 + 5, - height / 2 + 17);
            g2.drawString("Mouse to turn", -width / 2 + 5, - height / 2 + 34);
            g2.drawString("ESC to exit", -width / 2 + 5, - height / 2 + 51);
            g2.drawString("Users Online: " + usersOnline, width / 2 - 100, - height / 2 + 17);
            g2.drawString("FPS: " + fps, width / 2 - 50, - height / 2 + 34);

            shapes.sort(new DistanceComparator());

            for(Shape s : shapes) {
                s.draw(g2);
            }

            transform(true, new double[] {1, 0, 0,     0, 
                    0, 1, 0,     0, 
                    0, 0, 1, -600, 
                    0, 0, 0,     1});
        }
    }

    public void transform(boolean transformUser, double[] transformationMatrix) {
        for(Line l : grid) {
            l.transform(transformationMatrix);
        }

        for(Shape s : shapes) {
            if(s != user || transformUser)
                s.transform(transformationMatrix);
        }

        origin.transform(transformationMatrix);
        corner1.transform(transformationMatrix);
        corner2.transform(transformationMatrix);
        corner3.transform(transformationMatrix);
        corner4.transform(transformationMatrix);
    }

    public void updatePositions(String fromServer) {
        int mark = fromServer.indexOf("a");
        usersOnline = fromServer.substring(0, mark);
        int userColorID = Integer.parseInt(fromServer.substring(mark + 1, mark + 2));
        if(userColorID < 3)
            user.setColor(BLUE);
        else if(userColorID < 6)
            user.setColor(ORANGE);
        else if(userColorID < 7)
            user.setColor(PURPLE);
        else if(userColorID < 8)
            user.setColor(GREEN);
        else if(userColorID < 9)
            user.setColor(WHITE);
        else
            user.setColor(YELLOW);
        fromServer = fromServer.substring(fromServer.indexOf("a") + 2);
        shapes = new ArrayList<Shape>();
        while(fromServer.length() > 0) {
            int comma1 = fromServer.indexOf("a");
            int comma2 = fromServer.indexOf("a", comma1 + 1);
            int comma3 = fromServer.indexOf("a", comma2 + 1);
            int colorID = Integer.parseInt(fromServer.substring(0, 1));
            if(colorID < 3)
                shapes.add(new User(BLUE, Integer.parseInt(fromServer.substring(1, comma1)) + getOriginX(), Integer.parseInt(fromServer.substring(comma1 + 1, comma2)) + getOriginZ(), Double.parseDouble(fromServer.substring(comma2 + 1, comma3))));
            else if(colorID < 6)
                shapes.add(new User(ORANGE, Integer.parseInt(fromServer.substring(1, comma1)) + getOriginX(), Integer.parseInt(fromServer.substring(comma1 + 1, comma2)) + getOriginZ(), Double.parseDouble(fromServer.substring(comma2 + 1, comma3))));
            else if(colorID < 7)
                shapes.add(new User(PURPLE, Integer.parseInt(fromServer.substring(1, comma1)) + getOriginX(), Integer.parseInt(fromServer.substring(comma1 + 1, comma2)) + getOriginZ(), Double.parseDouble(fromServer.substring(comma2 + 1, comma3))));
            else if(colorID < 8)
                shapes.add(new User(GREEN, Integer.parseInt(fromServer.substring(1, comma1)) + getOriginX(), Integer.parseInt(fromServer.substring(comma1 + 1, comma2)) + getOriginZ(), Double.parseDouble(fromServer.substring(comma2 + 1, comma3))));
            else if(colorID < 9)
                shapes.add(new User(WHITE, Integer.parseInt(fromServer.substring(1, comma1)) + getOriginX(), Integer.parseInt(fromServer.substring(comma1 + 1, comma2)) + getOriginZ(), Double.parseDouble(fromServer.substring(comma2 + 1, comma3))));
            else
                shapes.add(new User(YELLOW, Integer.parseInt(fromServer.substring(1, comma1)) + getOriginX(), Integer.parseInt(fromServer.substring(comma1 + 1, comma2)) + getOriginZ(), Double.parseDouble(fromServer.substring(comma2 + 1, comma3))));
            fromServer = fromServer.substring(comma3 + 1);
        }
        shapes.add(user);
    }

    public void click() {
        //
    }

    public void updateFPS(int fps) {
        this.fps = fps;
    }

    public int getOriginX() {
        return (int)origin.getX();
    }

    public int getOriginZ() {
        return (int)origin.getZ();
    }
}
