package com.biokey.client.helpers;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;

public class LockerHelper implements Runnable {
    private boolean running = true;
    private JFrame[] lockFrames;

    public LockerHelper(JFrame[] lockFrames) {
        this.lockFrames = lockFrames;
    }

    public void start() {
        this.running = true;
        new Thread(this).start();
    }

    public void stop() {
        this.running = false;
    }

    public void run() {
        try {
            // TODO: Disable USB? MAC usage?
            Robot robot = new Robot();
            int i = 0;
            while (running) {
                sleep(10L);
                focus();
                releaseKeys(robot);
                if (++i % 10 == 0) {
                    kill("taskmgr.exe");
                    kill("explorer.exe"); // Kill explorer
                }
                releaseKeys(robot);
            }
        } catch (Exception e) {}
        finally {
            try {
                Runtime.getRuntime().exec("explorer.exe"); // Restart explorer
            } catch (Exception e) {}
        }
    }

    private void releaseKeys(Robot robot) {
        robot.keyRelease(17);
        robot.keyRelease(18);
        robot.keyRelease(127);
        robot.keyRelease(524);
        robot.keyRelease(9);

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
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {}
    }

    private void kill(String string) {
        try {
            Runtime.getRuntime().exec("taskkill /F /IM " + string).waitFor();
        } catch (Exception e) {}
    }

    private void focus() {
        for (JFrame lockFrame : this.lockFrames) {
            lockFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }
}
