import net.eletech.util.Handler;
import net.eletech.util.Message;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class MemoFrame extends JFrame {
    private JPanel panelTool;
    private JButton btnNew;
    private JPanel panelRoot;
    private JTextArea txtMemo;
    private JButton btnMinimize;
    private JLabel labelTitle;
    private JPanel panelBottom;
    private JButton btnColor;
    private JScrollPane scrollPane;
    private JButton btnFix;
    private JTextField txtTitle;
    private static ArrayList<File> fileList = new ArrayList<>();
    public String fileName;
    private Rectangle rectFrame = null;
    private boolean iconized = false;
    private boolean fixed = false;
    private boolean modified = false;
    private String title;
    
    private Point mousePoint;
    private static final ArrayList<MemoFrame> memoList = new ArrayList<>();
    
    public MemoFrame(int fileId) {
        super("June Sticker");
        setIconImage(Main.image);
        setType(Window.Type.UTILITY);
        memoList.add(this);
        
        setAlwaysOnTop(false);  // 모든 창의 최상위에 오도록 함
        setSize(400,300);
        labelTitle.setText("(F2:제목입력)");
        labelTitle.setForeground(Color.decode("#666666"));
        txtTitle.setVisible(false);
        txtTitle.setBorder(null);
        scrollPane.setBorder(null);
        setContentPane(panelRoot);
        addMouseListener(dragListener);
        addMouseMotionListener(dragListener);
        setUndecorated(true); //윈도우창 없애기
        setLocationRelativeTo(null);
//        setColor(txtMemo.getBackground());
        setColor(randomColor());
        addKeyStroke(panelRoot);
        setVisible(true);
        rectFrame = getBounds();
        fileName = String.format("%s\\sticker%04d.txt", Main.filePath, fileId);
        
        btnNew.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMinimize.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnColor.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnFix.setCursor(new Cursor(Cursor.HAND_CURSOR));
        labelTitle.setCursor(Cursor.getDefaultCursor());
        
        btnNew.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JPopupMenu popMenu = new JPopupMenu();
                popMenu.removeAll();
                JMenuItem item = new JMenuItem("새 메모 (F3)");
                item.addActionListener(e1 -> addMemo());
                popMenu.add(item);
                
                item = new JMenuItem("메모 삭제 (F4)");
                item.addActionListener(e1 -> deleteFile());
                popMenu.add(item);
                
                item = new JMenuItem("프로그램 종료 (F12)");
                item.addActionListener(e1 -> closeMemo());
                popMenu.add(item);
                popMenu.show(btnNew, btnNew.getX() + 5, btnNew.getY() + 10);
            }
            
        });
        loadFile();
        
        btnColor.addActionListener(e -> changeColor());
        
        btnFix.addActionListener(e -> setFixed(!fixed));
        
        btnMinimize.addActionListener(e -> { saveFile(); iconize(); });
        
        txtMemo.getDocument().addDocumentListener(txtListener);

        labelTitle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() > 1) {
                    if (iconized) deIconize();
                    else iconize();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                mousePoint = e.getPoint();
            }
        });

        labelTitle.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point pt = getLocation();
                pt.x += e.getX() - mousePoint.x;
                pt.y += e.getY() - mousePoint.y;
                setLocation(pt);
                modified = true;
            }

            @Override
            public void mouseMoved(MouseEvent e) {
            }
        });
    }
    
    private final DocumentListener txtListener = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            modified = true;
        }
        
        @Override
        public void removeUpdate(DocumentEvent e) {
            modified = true;
        }
        
        @Override
        public void changedUpdate(DocumentEvent e) {
            modified = true;
        }
    };
    
    private void removeListener() {
        for (MouseListener listener : getMouseListeners())
            removeMouseListener(listener);
        for (MouseMotionListener listener : getMouseMotionListeners())
            removeMouseMotionListener(listener);
        
        for (ActionListener listener : btnNew.getActionListeners())
            btnNew.removeActionListener(listener);
        for (ActionListener listener : btnColor.getActionListeners())
            btnColor.removeActionListener(listener);
        for (ActionListener listener : btnFix.getActionListeners())
            btnFix.removeActionListener(listener);
        for (ActionListener listener : btnMinimize.getActionListeners())
            btnMinimize.removeActionListener(listener);
        
        for (KeyListener listener : panelRoot.getKeyListeners())
            panelRoot.removeKeyListener(listener);
        
        txtMemo.getDocument().removeDocumentListener(txtListener);
    }
    private DragListener dragListener = new DragListener(new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == DragListener.WHAT_SIZE) {
                if (!iconized) {
                    rectFrame = (Rectangle) msg.object;
                    modified = true;
                }
            } else if (msg.what == DragListener.WHAT_DRAG) {
                updateLocation(msg.arg1, msg.arg2);
            }
            modified = true;
        }
    });
    public static void addMemo() {
        Point point = MouseInfo.getPointerInfo().getLocation();
        MemoFrame memoFrame = new MemoFrame(Main.nextId++);
        memoFrame.setLocation(point.x, point.y);
        memoFrame.txtMemo.requestFocus();
    }
    
    public void updateLocation(int x, int y) {
        if (rectFrame == null) rectFrame = getBounds();
        if ((rectFrame.x != x) || (rectFrame.y != y)) {
            setLocation(x, y);
            rectFrame.x = x;
            rectFrame.y = y;
            modified = true;
        }
    }
    private void editTitle() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!labelTitle.getText().equals(txtTitle.getText())) {
                    modified = true;
                }
                labelTitle.setText(txtTitle.getText());
                txtTitle.setVisible(false);
                labelTitle.setVisible(true);
                txtMemo.requestFocus();
            }
        };
        
        if (iconized) return;
        txtTitle.setText(labelTitle.getText());
        txtTitle.setVisible(true);
        labelTitle.setVisible(false);
        txtTitle.requestFocus();
        txtTitle.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(runnable);
                txtTitle.removeActionListener(this);
            }
        });
        txtTitle.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                SwingUtilities.invokeLater(runnable);
                txtTitle.removeFocusListener(this);
            }
        });
    }
    
    private void setColor(Color color) {
        panelRoot.setBackground(color);
        txtMemo.setBackground(color);
        panelBottom.setBackground(color);
        panelTool.setBackground(color);
    }
    
    private Color randomColor() {
        int r = (int)(Math.random() * 32) + 224;
        int g = (int)(Math.random() * 32) + 224;
        int b = (int)(Math.random() * 32) + 224;
        return new Color(r, g, b);
    }
    
    private void loadFile() {
        File file = new File(fileName);
        if (!file.exists()) return;
        try {
            FileReader fr = new FileReader(file.getPath());
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            if (line == null) return;
            String[] sp = line.split("#");
            labelTitle.setText(sp[1]);
            iconized = sp[2].equals("1");
            fixed = sp[3].equals("1");
            rectFrame = new Rectangle(Integer.parseInt(sp[4]), Integer.parseInt(sp[5]), Integer.parseInt(sp[6]), Integer.parseInt(sp[7]));
            setBounds(rectFrame);
            Color color = new Color(Integer.parseInt(sp[8]), Integer.parseInt(sp[9]), Integer.parseInt(sp[10]));
            setColor(color);
            
            while ((line = br.readLine()) != null) {
                txtMemo.append(line + "\n");
            }
            br.close();
            fr.close();
            setFixed(fixed);
            if (iconized) iconize();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveFile() {
        Color color = txtMemo.getBackground();
        try {
            FileWriter fw = new FileWriter(fileName);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(String.format("#%s#%d#%d#%d#%d#%d#%d#%d#%d#%d",
                    labelTitle.getText(), (iconized) ? 1 : 0, (fixed) ? 1 : 0, rectFrame.x, rectFrame.y, rectFrame.width, rectFrame.height,
                    color.getRed(), color.getGreen(), color.getBlue()));
            bw.newLine();
            bw.write(txtMemo.getText());
            bw.flush();
            bw.close();
            fw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFile() {
        File file = new File(fileName);
        if (file.exists()) file.delete();
        if (JOptionPane.showConfirmDialog(MemoFrame.this,
                "삭제하시겠습니까?", "파일삭제",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            file.delete();
            dispose();
            memoList.remove(MemoFrame.this);
        }
        removeListener();
    }
    
    public static void closeMemo() {
        for (MemoFrame memo : memoList) {
            memo.saveFile();
            memo.dispose();
            memo.removeListener();
            SwingUtilities.invokeLater(() -> {
                memoList.remove(memo);
            });
        }
    }
    
    public void iconize() {
        String title = txtMemo.getText();
        labelTitle.setToolTipText("더블클릭하시면 창이 복원됩니다");
        dragListener.setResizing(false);
        rectFrame = getBounds();
        btnColor.setVisible(false);
        btnMinimize.setVisible(false);
        btnFix.setVisible(false);
        scrollPane.setVisible(false);
        panelBottom.setVisible(false);
        pack();
        setAlwaysOnTop(true);
        iconized = true;
        modified = true;
    }
    public void deIconize() {
        labelTitle.setToolTipText(null);
        dragListener.setResizing(true);
        btnColor.setVisible(true);
        btnMinimize.setVisible(true);
        btnFix.setVisible(true);
        scrollPane.setVisible(true);
        panelBottom.setVisible(true);
        setBounds(rectFrame);
        setAlwaysOnTop(fixed);
        iconized = false;
        modified = true;
    }
    public void setFixed(boolean fix) {
        fixed = fix;
        setAlwaysOnTop(fixed);
        try {
            btnFix.setIcon(new ImageIcon(ImageIO.read
                    (MemoFrame.class.getClassLoader().getResource((fixed) ? "iconFixed.png" : "iconUnfixed.png"))));
        }
        catch (IOException e) {

        }
    }
    
    private void changeColor() {
        Color color = JColorChooser.showDialog(btnColor,"",txtMemo.getBackground());
        if (color != null) {
            setColor(color);
        }
    }
    private void addKeyStroke(JPanel panel) {
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F2"), "F2");
        panel.getActionMap().put("F2",  new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editTitle();
            }
        });
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F3"), "F3");
        panel.getActionMap().put("F3", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addMemo();
            }
        });
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F4"), "F4");
        panel.getActionMap().put("F4", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteFile();
            }
        });
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F5"), "F5");
        panel.getActionMap().put("F5", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeColor();
            }
        });
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F6"), "F6");
        panel.getActionMap().put("F6", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setFixed(!fixed);
            }
        });
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F7"), "F7");
        panel.getActionMap().put("F7", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (iconized) deIconize();
                else iconize();
            }
        });
        panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F12"), "F12");
        panel.getActionMap().put("F12", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeMemo();
            }
        });
    }
    
    private void createUIComponents() {
        // TODO: place custom component creation code here
        btnNew = new JButton("iconMenu.png");
        btnColor = new JButton("iconPalette");
        btnMinimize = new JButton("iconMinimize");
        btnFix = new JButton("iconUnfixed");
    }
    public static class DragListener extends MouseAdapter {
        private static final int PADDING = 5;
        public static final int WHAT_SIZE = 1;
        public static final int WHAT_DRAG = 2;
        
        private static final int RESIZE_LEFT = 1;
        private static final int RESIZE_RIGHT = 2;
        private static final int RESIZE_TOP = 4;
        private static final int RESIZE_BOTTOM = 8;
        
        private final net.eletech.util.Handler handler;
        private int mouseX;
        private int mouseY;
        private boolean resizing = true;
        private int gripType = 0;
        
        private HashMap<Integer, Integer> cmap = new HashMap<>();
        
        public DragListener(net.eletech.util.Handler handler) {
            this.handler = handler;
            cmap.put(1, Cursor.W_RESIZE_CURSOR);
            cmap.put(2, Cursor.E_RESIZE_CURSOR);
            cmap.put(4, Cursor.N_RESIZE_CURSOR);
            cmap.put(5, Cursor.NW_RESIZE_CURSOR);
            cmap.put(6, Cursor.NE_RESIZE_CURSOR);
            cmap.put(8, Cursor.S_RESIZE_CURSOR);
            cmap.put(9, Cursor.SW_RESIZE_CURSOR);
            cmap.put(10, Cursor.SE_RESIZE_CURSOR);
        }
        
        public void setResizing(boolean resizingOn) {
            resizing = resizingOn;
        }
        
        @Override
        public void mouseMoved(MouseEvent e) {
            JFrame frame = (JFrame) e.getSource();
            int cursor = Cursor.DEFAULT_CURSOR;
            if (isSizeGrip(frame, e)) {
                cursor = cmap.get(gripType);
            }
            frame.setCursor(Cursor.getPredefinedCursor(cursor));
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            JFrame frame = (JFrame) e.getSource();
            mouseX = e.getX();
            mouseY = e.getY();
            isSizeGrip(frame, e);
        }
        
        @Override
        public void mouseDragged(MouseEvent e) {
            JFrame frame = (JFrame) e.getSource();
            if (resizing && (gripType != 0)) {
                Point point = MouseInfo.getPointerInfo().getLocation();
                Rectangle rect = frame.getBounds();
                if ((gripType & RESIZE_LEFT) != 0) {
                    rect.width += (rect.x - point.x);
                    rect.x = point.x;
                }
                if ((gripType & RESIZE_RIGHT) != 0) {
                    rect.width = (point.x - rect.x);
                }
                if ((gripType & RESIZE_TOP) != 0) {
                    rect.height += (rect.y - point.y);
                    rect.y = point.y;
                }
                if ((gripType & RESIZE_BOTTOM) != 0) {
                    rect.height = (point.y - rect.y);
                }
                frame.setBounds(rect);
                handler.obtainMessage(WHAT_SIZE, rect).sendToTarget();
            } else {    // move
                int x = frame.getLocation().x + e.getX() - mouseX;
                int y = frame.getLocation().y + e.getY() - mouseY;
                handler.obtainMessage(WHAT_DRAG, x, y).sendToTarget();
            }
        }
        
        private boolean isSizeGrip(JFrame frame, MouseEvent e) {
            if (!resizing) return false;
            gripType = 0;
            gripType |= (e.getX() <= PADDING) ? RESIZE_LEFT : 0;
            gripType |= (e.getX() >= frame.getWidth() - PADDING) ? RESIZE_RIGHT : 0;
            gripType |= (e.getY() <= PADDING / 2) ? RESIZE_TOP : 0;
            gripType |= (e.getY() >= frame.getHeight() - PADDING) ? RESIZE_BOTTOM : 0;
            return (gripType != 0);
        }
    }
    
}

