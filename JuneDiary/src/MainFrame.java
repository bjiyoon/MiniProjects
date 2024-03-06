import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class MainFrame extends JFrame {
    private JPanel panelRoot;
    private JPanel panelFloor;
    private JPanel panelTitle;
    private JPanel panelContent;
    private JPanel panelPage;
    private JLabel labelTitle;
    private JLabel labelPage;
    private JButton btnPrev;
    private JButton btnNext;
    private JButton btnWrite;
    private JButton btnLogin;
    private JButton btnSearch;
    private JTextField txtFieldSearch;
    public static final ArrayList<DiaryData> ddList = new ArrayList<>();
    private static final String IMAGE_SEARCH = "res/ic_search.png";
    private static final String IMAGE_LOGIN = "res/ic_login.png";
    private static final String IMAGE_WRITE = "res/ic_write.png";
    private static final String IMAGE_PREV = "res/ic_prev.png";
    private static final String IMAGE_NEXT = "res/ic_next.png";
    public static String imagePath = "C:\\JUNIVERSE_Dev\\JuneDiary\\src\\res\\";

    private static int cols = 3;
    private static int rows = 3;
    private static int width = 200;
    private static int height = 200;
    private static int gap = 1;
    
    private String diaryName = "";
    public static int totalItems = 0;
    public static int totalPages = 0;
    private int page = 0;
    
    public MainFrame() {
        setUIFont();
//        new LoginForm(MainFrame.this, "");
        setContentPane(panelRoot);
        setLocationRelativeTo(null);
        setLocation(900,300);
        setSize(900,900);
        panelContent.setPreferredSize(new Dimension((width + gap) * cols, (height + gap) * rows));
        
        setResizable(false);
        panelSettings();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        panelContent.setLayout(new GridLayout(3, 3));
        labelTitle.setFont(new Font("JetBrains MONO", Font.PLAIN, 30));
        labelTitle.setText(diaryName);
        txtFieldSearch.setBorder(BorderFactory.createMatteBorder(0,0,1,0, Color.BLACK));
        
        labelPage.setText("" + (page + 1));
        
        labelTitle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                loadData("", page);
            }
        });
        
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadData(txtFieldSearch.getText(), 0);
            }
        });
        
        btnPrev.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (page > 0) {
                    page--;
                    labelPage.setText("" + (page + 1));
                    loadData(txtFieldSearch.getText(), page);
                    repaint();
                }
            }
        });
        
        btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (page < totalPages - 1) {
                    page++;
                    labelPage.setText("" + (page + 1));
                    loadData(txtFieldSearch.getText(), page);
                    repaint();
                }
            }
        });

        btnWrite.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewFrame(MainFrame.this, 0);
            }
        });
        
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (login()) loadData("", page);
            }
        });
        
        panelContent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int col = e.getX() / (width + gap);
                int row = e.getY() / (height + gap);
                int index = row * cols + col;
                new ViewFrame(MainFrame.this, ddList.get(index).id);
            }
        });
        
        setVisible(true);
        SwingUtilities.invokeLater(() -> {
            width = panelContent.getPreferredSize().width / cols;
            height = panelContent.getPreferredSize().height/ cols;
        });
        if (login()) loadData("", page);
    }
    
    public static void setUIFont() {
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");
        javax.swing.plaf.FontUIResource f = new javax.swing.plaf.FontUIResource("Dialog", Font.PLAIN, 12);
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }
    public static BufferedImage thumbImage(BufferedImage img) {
        BufferedImage thumbImage = new BufferedImage(200, 200, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D canvas = thumbImage.createGraphics();
        if (img != null) {
            canvas.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            canvas.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            canvas.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            canvas.drawImage(img, 0, 0, 200, 200, null);
        }
        else {
            canvas.setColor(Color.white);
            canvas.fillRect(0, 0, 200, 200);
            canvas.setColor(Color.decode("#EEEEEE"));
            canvas.drawLine(0, 0, 200, 200);
            canvas.drawLine(200, 0, 0, 200);
        }
        canvas.dispose();
        return thumbImage;
    }
   
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        System.out.println("paint()");
        drawPhotos();
    }
    
    private void drawPhotos() {
        Graphics2D g2d = (Graphics2D) panelContent.getGraphics();
//        g2d.setColor(panelContent.getBackground());
//        g2d.fillRect(0, 0, panelContent.getWidth(), panelContent.getHeight());
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        System.out.println(cols + " " + width + " " + height + " " + gap);
        for (int i = 0; i < ddList.size(); i++) {
            int x = (i % cols) * (width + gap);
            int y = (i / cols) * (height + gap);
            g2d.setClip(new RoundRectangle2D.Double(x, y, width, height, width/10, height/10));
            g2d.drawImage(ddList.get(i).photo, x, y, width, height,null);
        }
        g2d.dispose();
    }
    
    public void panelSettings() {
        panelRoot.setBackground(Color.decode("#FFFFFF"));
        panelFloor.setBackground(Color.decode("#FFFFFF"));
        panelTitle.setBackground(Color.decode("#FFFFFF"));
        panelTitle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panelContent.setBackground(Color.decode("#FFFFFF"));
        panelPage.setBackground(Color.decode("#FFFFFF"));
    }
    
    private void createUIComponents() {
        // TODO: place custom component creation code here
        btnSearch = new CButton(IMAGE_SEARCH);
        btnLogin = new CButton(IMAGE_LOGIN);
//        btnLogout = new CButton(IMAGE_LOGOUT);
        btnWrite = new CButton(IMAGE_WRITE);
        btnPrev = new CButton(IMAGE_PREV);
        btnNext = new CButton(IMAGE_NEXT);
        
    }

    public void loadData(String hash, int page) {
        System.out.println("loadData()");
        Connection sqlConn = null;
        ddList.clear();
        try {
            Class.forName(Main.SQL_DRIVER);
            String url = Main.SQL_URL;
            System.out.println(url + " " + Main.sqlDbase + " " + Main.sqlPass);
            sqlConn = DriverManager.getConnection(url, Main.sqlDbase, Main.sqlPass);
            PreparedStatement pst;
            pst = sqlConn.prepareStatement("SELECT COUNT(*) AS cnt FROM " + Main.sqlTable + " WHERE hash like '%" + hash + "%'");
            System.out.println(pst);
            ResultSet rs = pst.executeQuery();
            rs.next();
            totalItems = rs.getInt("cnt");

            // next를 누르면 limit 9, 9;
            pst = sqlConn.prepareStatement("SELECT * FROM " + Main.sqlTable + " WHERE hash like '%" + hash + "%' ORDER BY id DESC LIMIT " + (9 * page) + ", 9");
            rs = pst.executeQuery();
            ResultSetMetaData stData = rs.getMetaData();
            while (rs.next()) {
                DiaryData dd = new DiaryData();
                dd.id = rs.getInt("id");
                dd.date = rs.getString("date");
                dd.text = rs.getString("text");
                dd.hash = rs.getString("hash");
                Blob blob = rs.getBlob("photo");
                dd.photo = (blob != null) ? ImageIO.read(blob.getBinaryStream()) : null; 
                ddList.add(dd);
            }
            totalPages = (totalItems + 8) / 9;
            System.out.println(ddList.size());
        }
        catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
        if (sqlConn != null) {
            try {
                sqlConn.close();
            }
            catch (Exception e) {
            }
        }
        System.out.println("load finished: " + ddList.size() + " " + totalItems + " " + totalPages);
        repaint();
    }
    
    private boolean login() {
        Main.tableReady = false;
        LoginForm dlg = new LoginForm(MainFrame.this, "DB 로그인");
        dlg.sqlDbase = Main.sqlDbase;
        dlg.sqlTitle = diaryName;
        dlg.sqlPass = Main.sqlPass;
        if (dlg.doModal()) {
            Main.sqlDbase = dlg.sqlDbase;
            diaryName = dlg.sqlTitle;
            Main.sqlPass = dlg.sqlPass;
            labelTitle.setText(dlg.sqlTitle);
            System.out.println(Main.sqlDbase + " " + Main.sqlPass);
            Main.setupUser();
            return true;
        }
        return false;
    }
    
    public static class DiaryData {
        public int id = 0;
        public String date = "";
        public String text = "";
        public String hash = "";
        public BufferedImage photo = null;
    }
}
