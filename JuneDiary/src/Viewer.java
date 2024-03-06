import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.*;

public class Viewer extends JFrame {
    private JPanel panelView;
    private JPanel pn_Top;
    private JPanel pn_Image;
    private JPanel pn_Bottom;
    private JScrollPane scrollPane;
    private JTextArea txtBody;
    private static final String IMAGE_CANCEL = "res/ic_cancel.png";
    
    public Viewer(JFrame parent) {
        MainFrame.setUIFont();
        setContentPane(panelView);
        setSize(500,800);
        setLocation(parent.getX() + parent.getWidth(), parent.getY());
        setResizable(false);
        panelSettings();
        setVisible(true);
    }
    private void viewData() {
        loadData();
    }
    
    private void loadData() {
        Connection sqlConn = null;
        try {
            Class.forName(Main.SQL_DRIVER);
            String url = Main.SQL_URL;
            sqlConn = DriverManager.getConnection(url, Main.sqlUser, Main.sqlPass);
            PreparedStatement pst;
            pst = sqlConn.prepareStatement("SELECT * FROM board ORDER BY id DESC LIMIT 0, 1");
            ResultSet rs = pst.executeQuery();
            rs.next();
            while (rs.next()) {
                MainFrame.DiaryData dd = new MainFrame.DiaryData();
                dd.id = rs.getInt("id");
                dd.date = rs.getString("date");
                dd.text = rs.getString("text");
                dd.hash = rs.getString("hash");
                Blob blob = rs.getBlob("photo");
                dd.photo = (blob != null) ? ImageIO.read(blob.getBinaryStream()) : null;
                MainFrame.ddList.add(dd);
            }
            
        }
        catch (SQLException | ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }
    public void panelSettings() {
        panelView.setBackground(new Color(0, 0, 0, 0));
        pn_Top.setBackground(new Color(0, 0, 0, 0));
        pn_Image.setBackground(new Color(0, 0, 0, 0));
        pn_Bottom.setBackground(new Color(0, 0, 0, 0));
    }
    
    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
