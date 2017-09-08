package com.charles;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * Created by Administrator on 2017/7/14.
 */
public class MainGui extends Application {
    private static Logger logger = Logger.getLogger(MainGui.class.getName());
    //javafx中 舞台初始化为primaryStage
    //javafx中 场景初始化为rootScene
    private Stage primaryStage;
    private Scene rootScene;

    private TrayIcon trayIcon;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Platform.setImplicitExit(false);
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Server Configuration");

        try {
            FXMLLoader mainLayoutLoader = new FXMLLoader(MainGui.class.getResource("/resources/ui/MainLayout.fxml"));
            mainLayoutLoader.setResources(ResourceBundle.getBundle("resources.bundle.ui", Constant.LOCALE));
            Pane rootLayout = mainLayoutLoader.load();

            rootScene = new Scene(rootLayout);
            primaryStage.setScene(rootScene);
            primaryStage.setResizable(false);

            addToTray();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addToTray() {
        //initial awt
        java.awt.Toolkit.getDefaultToolkit();

        //make sure system tray is supported
        if (!java.awt.SystemTray.isSupported()) {
            logger.warning("系统不支持tray");
        }

        final java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
        try {
            java.awt.Image image = ImageIO.read(MainGui.class.getResource("/resources/img/icon.png"));
            trayIcon = new TrayIcon(image);
            trayIcon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            primaryStage.show();
                        }
                    });
                }
            });

            java.awt.MenuItem openItem = new java.awt.MenuItem("Configuration");
            openItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            show();

                        }
                    });
                }
            });

            java.awt.MenuItem exitItem = new java.awt.MenuItem("exit");
            exitItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Platform.exit();
                    tray.remove(trayIcon);
                }
            });

            PopupMenu popup = new PopupMenu();
            popup.add(openItem);
            popup.addSeparator();
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);
            trayIcon.setToolTip("Not Connected!");
            tray.add(trayIcon);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public void show() {
        primaryStage.show();
    }

    public void hide() {
        primaryStage.hide();
    }

    public void setTooltip(String message) {
        if (trayIcon != null) {
            trayIcon.setToolTip(message);
        }
    }

    public void showNotification(String message) {
        trayIcon.displayMessage("shadowSocks-test", message, TrayIcon.MessageType.INFO);
    }



}
