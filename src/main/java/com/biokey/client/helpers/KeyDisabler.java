import java.awt.event.KeyEvent;
import java.awt.Robot;
import javax.swing.*;

/*
 *  Source: https://stackoverflow.com/questions/6127709/remove-the-possibility-of-using-alt-f4-and-alt-tab-in-java-gui
 *  https://msdn.microsoft.com/en-us/library/windows/desktop/dd375731(v=vs.85).aspx
 */
public class KeyDisabler implements Runnable {
    private boolean working = true;
    private JFrame frame;
    public KeyDisabler (JFrame frame) {
        this.frame = frame;
    }
    public void run() {
        try {
            Robot robot = new Robot();
            while (working) {
                robot.keyRelease(KeyEvent.VK_ALT);
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_CONTROL);
                robot.keyRelease(KeyEvent.VK_CONTEXT_MENU);
                robot.keyRelease(KeyEvent.VK_WINDOWS);
                robot.keyRelease(KeyEvent.VK_ESCAPE);

                robot.keyRelease(KeyEvent.VK_F1);
                robot.keyRelease(KeyEvent.VK_F2);
                robot.keyRelease(KeyEvent.VK_F3);
                robot.keyRelease(KeyEvent.VK_F4);
                robot.keyRelease(KeyEvent.VK_F5);
                robot.keyRelease(KeyEvent.VK_F6);
                robot.keyRelease(KeyEvent.VK_F7);
                robot.keyRelease(KeyEvent.VK_F8);
                robot.keyRelease(KeyEvent.VK_F9);
                robot.keyRelease(KeyEvent.VK_F10);
                robot.keyRelease(KeyEvent.VK_F11);
                robot.keyRelease(KeyEvent.VK_F12);

                frame.requestFocus();
                try { Thread.sleep(10); } catch(Exception e) {}
            }
        } catch (Exception e) { e.printStackTrace(); System.exit(-1); }
    }
}