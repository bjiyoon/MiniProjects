import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginForm extends JDialog {
    private JPanel contentPane;
    private JTextField txtId;
    private JTextField txtTitle;
    private JPasswordField txtPass;
    private JButton btnOk;
    private JButton btnCancel;
    
    private String IMAGE_OK = "res/ic_submit.png";
    private String IMAGE_CANCEL = "res/ic_cancel.png";
    
    public String sqlDbase = "";
    public String sqlTitle = "";
    public String sqlPass = "";
    
    private boolean approved = false;
    
    
    public LoginForm(JFrame parent, String title) {
        super(parent, "회원가입 / 로그인");
        setContentPane(contentPane);
//        setSize(350,400);
        setModal(true);
        contentPane.setBackground(Color.white);
        setLocationRelativeTo(parent);
        pack();
        setResizable(false);
        getRootPane().setDefaultButton(btnOk);
        
        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        btnCancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
        
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }
    
    public boolean doModal() {
        txtId.setText(sqlDbase);
        txtPass.setText(sqlPass);
        txtTitle.setText(sqlTitle);
        
        Dimension size = getSize();
        Point point = getLocation();
        setLocation(point.x - size.width / 2, point.y - size.height / 2);
        setVisible(true);
        while (isVisible()) {
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
            }
        }
        return approved;
    }
    
    private void onOK() {
        sqlDbase = txtId.getText();
        sqlPass = new String(txtPass.getPassword());
        sqlTitle = txtTitle.getText();
        System.out.println(sqlDbase + " " +sqlPass);
        approved = true;
        setVisible(false);
        dispose();
        removeListeners();
    }
    
    private void onCancel() {
        setVisible(false);
        dispose();
        removeListeners();
    }
    
    private void removeListeners() {
        for (ActionListener l : btnOk.getActionListeners())
            btnOk.removeActionListener(l);
        for (ActionListener l : btnCancel.getActionListeners())
            btnCancel.removeActionListener(l);
    }
    
    private void createUIComponents() {
        // TODO: place custom component creation code here
        btnOk = new CButton(IMAGE_OK);
        btnCancel = new CButton(IMAGE_CANCEL);
    }
}
