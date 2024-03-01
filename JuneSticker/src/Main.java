import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {
    private static ArrayList<File> fileList = new ArrayList<>();
    public static int nextId = 1;
    private static final int SERVER_PORT = 9999;
    public static String filePath = "";
    private static boolean linux = false;
    public static Image image = null;
    
    public static void main(String[] args) {
        String os = System.getProperty("os.name").toLowerCase();
        linux = os.contains("linux");
        if (checkServer()) {
            if (!linux) {
                JOptionPane.showMessageDialog(null,
                        "프로그램이 이미 실행 중입니다" + System.lineSeparator() + "시스템트레이의 아이콘 메뉴를 이용하세요",
                        "중복 실행", JOptionPane.INFORMATION_MESSAGE);
            }
            System.exit(1);
        }
        if (!linux) setOnTray();
//        if (!openAllNotes()) createNote();
        
        File file = Paths.get("data").toFile();
        
        if (!file.exists()) file.mkdir();
        filePath = file.getPath();
        
        int max = 0;
        int fileCount = 0;
        for (File f : file.listFiles()) {
            if (f.getName().startsWith("sticker")) {
                fileList.add(f);
                int n = Integer.parseInt(f.getName().substring(7, 11));
                if (n > max) max = n;
                new MemoFrame(n);
                fileCount++;
            }
        }
        nextId = max + 1;
        if (fileCount == 0) new MemoFrame(nextId);
        
        startServer();
    }
    
    private static void startServer() {
        try {
            ServerSocket server = new ServerSocket(SERVER_PORT);
            while (true) {
                Socket client = server.accept();
                client.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static boolean checkServer() {
        try {
            Socket socket = new Socket("127.0.0.1", SERVER_PORT);
            socket.close();
            return true;
        } catch (IOException ex) {
        }
        return false;
    }
    
    public static void setOnTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("System Tray not available");
            return;
        }
        
        final SystemTray tray = SystemTray.getSystemTray();

        try {
            URL url = Main.class.getClassLoader().getResource("trayiconMenu.png");
            if (url != null) {
                image = ImageIO.read(url);
            }
        } catch (IOException e) {
            return;
        }
        
        final TrayIcon trayIcon = new TrayIcon(image, "클립노트");
        trayIcon.setImageAutoSize(true);
        
        PopupMenu popup = new PopupMenu();
        
        MenuItem showItem = new MenuItem("Show All Notes");
        showItem.addActionListener(e -> showAllMemo());

        MenuItem exitItem = new MenuItem("Close and Exit");
        exitItem.addActionListener(e -> {MemoFrame.closeMemo(); System.exit(0);});
        
        popup.add(showItem);
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);
        
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            return;
        }
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Action");
            }
        });
    }
    
    public static void showAllMemo() {
        Frame[] frames = MemoFrame.getFrames();
        for (Frame frame : frames) {
            if (frame instanceof MemoFrame) {
                ((MemoFrame) frame).requestFocus();
            }
        }
    }
    
}