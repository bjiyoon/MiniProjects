import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.*;
import java.util.Iterator;

public class ViewFrame extends JFrame {
    private JPanel contentPane;
    private JPanel panelImg;
    private JPanel panelBottom;
    private JScrollPane ScrollPane;
    private JTextArea txtMemo;
    private JButton btnSubmit;
    private JButton btnAttach;
    private JButton btnDel;
    private static final String IMAGE_SUBMIT = "res/ic_submit.png";
    private static final String IMAGE_ATTACH = "res/ic_attach.png";
    private static final String IMAGE_DEL = "res/ic_del.png";
    private static final FileNameExtensionFilter FILTER = new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png");
    private static File currentDir = new File("C:\\JUNIVERSE_Dev\\JuneDiary\\src\\res");
    
    private MainFrame.DiaryData diaryData = new MainFrame.DiaryData();
    private boolean modified = false;
    
    public ViewFrame(JFrame parent, int id) {
        System.out.println("ViewFrame(): id=" + id);
        setContentPane(contentPane);
        setSize(500,800);
        setLocation(parent.getX() + parent.getWidth(), parent.getY());
        setResizable(false);
        panelSettings();
        setVisible(true);
        
        btnAttach.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                attachFile();
            }
        });
        btnDel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(ViewFrame.this,"게시물을 삭제하시겠습니까?","삭제",
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
                    deleteData(id);
                dispose();
            }
        });
        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (modified) uploadData(id);
                dispose();
                ((MainFrame)parent).loadData("", 0);
//                Main.frame.loadData("", 0);
            }
        });

        txtMemo.getDocument().addDocumentListener(new DocumentListener() {
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
        });

        if (id > 0) downloadData(id);
    }
    
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (diaryData.photo != null) {
            int width = panelImg.getWidth();
            int height = width * diaryData.photo.getHeight() / diaryData.photo.getWidth();
            
            Graphics2D g2d = (Graphics2D) panelImg.getGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(diaryData.photo, 0, 0, width, height, null);
        }
    }
    
    public void attachFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(FILTER);
        fileChooser.setCurrentDirectory(currentDir);
        int returnVal = fileChooser.showOpenDialog(ViewFrame.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            currentDir = fileChooser.getCurrentDirectory();
            File selectedFile = fileChooser.getSelectedFile();
            String fn = selectedFile.getName().toLowerCase();
            if (fn.endsWith(".png") || fn.endsWith(".jpg") || fn.endsWith(".jpeg")) {
                try {
                    diaryData.photo = ImageIO.read(selectedFile);
                }
                catch (IOException e) {
                }
                int width = 384;
                int height = width * diaryData.photo.getHeight() / diaryData.photo.getWidth();
                panelImg.setPreferredSize(new Dimension(width, height));
                panelImg.setSize(width, height);
                revalidate();
                repaint();
                modified = true;
            } else {
                JOptionPane.showMessageDialog(ViewFrame.this,"jpg, jpeg, png 파일만 가능합니다.");
            }
        }
    }
    public static ByteArrayOutputStream writeImage(BufferedImage image, String type, float ratio, float quality) {
        if ((image == null) || (ratio <= 0f)) return null;
        
        if (ratio > 1f) ratio = 1f;
        if (quality < 0f) quality = 0f;
        if (quality > 1f) quality = 1f;
        
        BufferedImage src = image;
        if (ratio < 1f) {
            int width = (int)(image.getWidth() * ratio);
            int height = (int)(image.getHeight() * ratio);
            src = new BufferedImage(width, height, image.getType());
            Graphics2D g2d = src.createGraphics();
            g2d.drawImage(image, 0, 0, width, height, null);
            g2d.dispose();
        }
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(type);
        if (!writers.hasNext()) {
            throw new IllegalStateException("No writers found");
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            writer.write(null, new IIOImage(src, null, null), param);
            ios.flush();
            ios.close();
        } catch (IOException e) {
        }
        writer.dispose();
        return baos;
    }
    
    //---------------------------------------------------------------------------------------------------------
    public static long writeImageSize(BufferedImage image, String type, float ratio, float quality) {
        if ((image == null) || (ratio <= 0f)) return 0;
        ByteArrayOutputStream baos = writeImage(image, type, ratio, quality);
        if (baos == null) return 0;
        long size = baos.size();
        try { baos.close(); } catch (IOException e) { }
        return size;
    }
    
    private void downloadData(int id) {
        Connection sqlConn = null;
        try {
            Class.forName(Main.SQL_DRIVER);
            String url = Main.SQL_URL;
            sqlConn = DriverManager.getConnection(url, Main.sqlUser, Main.sqlPass);
            PreparedStatement pst;
            pst = sqlConn.prepareStatement("SELECT * FROM " + Main.sqlTable + " WHERE id =" + id);
            ResultSet rs = pst.executeQuery();
            rs.next();
            diaryData.id = rs.getInt("id");
            diaryData.date = rs.getString("date");
            diaryData.text = rs.getString("text");
            diaryData.hash = rs.getString("hash");
            Blob blob = rs.getBlob("photo");
            diaryData.photo = (blob != null) ? ImageIO.read(blob.getBinaryStream()) : null;
            txtMemo.setText(diaryData.text);
            
            if (diaryData.photo != null) {
                int width = 384;
                int height = 384 * diaryData.photo.getHeight() / diaryData.photo.getWidth();
                panelImg.setPreferredSize(new Dimension(width, height));
                panelImg.setSize(width, height);
                revalidate();
                repaint();
                modified = false;
            }
        }
        catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
    public void uploadData(int id) {
        //id 체크를 해서 0이면 insert , 아니면 update
        long count = 0;
        Connection sqlConn = null;
        try {
            Class.forName(Main.SQL_DRIVER);
            sqlConn = DriverManager.getConnection(Main.SQL_URL, Main.sqlUser, Main.sqlPass);
            String query;
            if (id == 0) {
                query = "INSERT INTO " + Main.sqlTable + " (date, text, hash, photo) VALUES (NOW(),?,?,?)";
            }
            else {
                query = "UPDATE " + Main.sqlTable + " SET date=NOW(), text=?, hash=?, photo=? WHERE id=" + id;
            }
            System.out.println(query);
            PreparedStatement pst = sqlConn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
//            pst.setString(1, page.date);
            pst.setString(1, txtMemo.getText());    // date를 제외한 나머지 전달값들 나열
            String hash = getHash(txtMemo.getText());
            pst.setString(2, hash);
            if (diaryData.photo == null) {
                pst.setNull(3, Types.BLOB);
            } else {
                ByteArrayOutputStream baos = writeImage(diaryData.photo, "jpg", 0.5f, 0.8f);
                ImageIO.write(diaryData.photo, "jpg", baos);
                pst.setBlob(3, new ByteArrayInputStream(baos.toByteArray()));
            }
            System.out.println(pst);
            count = pst.executeLargeUpdate();
            ResultSet rs = pst.getGeneratedKeys();
        } catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
    public void deleteData(int id) {
        long count = 0;
        Connection sqlConn = null;
        try {
            Class.forName(Main.SQL_DRIVER);
            sqlConn = DriverManager.getConnection(Main.SQL_URL, Main.sqlUser, Main.sqlPass);
            String query = "DELETE FROM " + Main.sqlTable + " WHERE id=" + id;
            PreparedStatement pst = sqlConn.prepareStatement(query);
            count = pst.executeLargeUpdate();
        }
        catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (sqlConn != null) {
            try {
                sqlConn.close();
            }
            catch (Exception e) {
            }
        }
        System.out.println("deleteData()");
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        btnSubmit = new CButton(IMAGE_SUBMIT);
        btnAttach = new CButton(IMAGE_ATTACH);
        btnDel = new CButton(IMAGE_DEL);
    }
    
    public void panelSettings() {
        contentPane.setBackground(Color.decode("#FFFFFF"));
        panelImg.setBackground(Color.decode("#FFFFFF"));
        panelBottom.setBackground(Color.decode("#FFFFFF"));
    }
    
    public String getHash(String src) {
        String hash = "";
        while (!src.isEmpty()) {
            int iTag = src.indexOf('#');    // #을 찾아서 iTag에 저장
            if (iTag < 0) break;
            src = src.substring(iTag + 1);  // #부터 다음문장 = src
            int iSpc = src.indexOf(' ');    // 공백 이전까지의 문자 = iSpc
            if (!hash.isEmpty()) hash += " ";
            if (iSpc > 0) {
                hash += src.substring(0, iSpc);
                src = src.substring(iSpc);
            }
            else {
                hash += src;
                break;
            }
        }
        return hash;
    }
}
