import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class CButton extends JButton {
    private static final int STATE_NORMAL = 0;
    private static final int STATE_OVERED = 1;
    private static final int STATE_PRESSED = 2;
    private static final int STATE_DISABLED = 3;
    
    private final BufferedImage[] image = {null, null, null, null};
    private boolean mouseEntered = false;
    private boolean mousePressed = false;
    private boolean disabled = false;
    
    public CButton(String src) {
        this(new String[]{src, null, null, null});
    }
    
    public CButton(String[] src) {
        super();
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setBackground(new Color(0, 0, 0, 0));
        setSource(src);
        disabled = !isEnabled();
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                mousePressed = true;
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                mousePressed = false;
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                mouseEntered = true;
                setCursor(new Cursor(disabled ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                mouseEntered = false;
            }
        });
        
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                mouseEntered = false;
                mousePressed = false;
                invalidate();
            }
        });
    }
    
    @Override
    public void setEnabled(boolean b) {
        disabled = !b;
        super.setEnabled(b);
        invalidate();
    }
    
    public void removeListeners() {
        for (ActionListener l : getActionListeners())
            removeActionListener(l);
        for (MouseListener l : getMouseListeners())
            removeMouseListener(l);
        for (FocusListener l : getFocusListeners())
            removeFocusListener(l);
    }
    
    public void setSource(String[] src) {
        int width = 0, height = 0;
        for (int i = 0; (i < image.length) && (i < src.length); i++) {
            image[i] = loadImage(src[i]);
            if (image[i] != null) {
                if (image[i].getWidth() > width) width = image[i].getWidth();
                if (image[i].getHeight() > height) height = image[i].getHeight();
            }
        }
        setPreferredSize(new Dimension(width, height));
        invalidate();
    }
    
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (disabled && (image[STATE_DISABLED] != null)) {
            drawImage(g2d, image[STATE_DISABLED]);
            return;
        }
        if (mousePressed && (image[STATE_PRESSED] != null)) {
            drawImage(g2d, image[STATE_PRESSED]);
            return;
        }
        if (mouseEntered && (image[STATE_OVERED] != null)) {
            drawImage(g2d, image[STATE_OVERED]);
            return;
        }
        drawImage(g2d, image[STATE_NORMAL]);
    }
    
    private BufferedImage loadImage(String src) {
        if (src == null) return null;
        BufferedImage image = null;
        try {
            URL url = getClass().getClassLoader().getResource(src);
            if (url != null) {
                image = ImageIO.read(url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
    
    private void drawImage(Graphics2D g, BufferedImage image) {
        if (image == null) return;
        int x = (getWidth() - image.getWidth()) / 2;
        int y = (getHeight() - image.getHeight()) / 2;
        g.drawImage(image, x, y, null);
    }
    
}
