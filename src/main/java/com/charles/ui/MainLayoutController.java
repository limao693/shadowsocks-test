package com.charles.ui;

import com.charles.Constant;
import com.charles.MainGui;
import com.charles.misc.Config;
import com.charles.misc.UTF8Control;
import com.charles.misc.Util;
import com.charles.network.IServer;
import com.charles.network.NioLocalServer;
import com.charles.network.proxy.IProxy;
import com.charles.network.proxy.ProxyFactory;
import com.charles.ss.CryptFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Created by Administrator on 2017/7/14.
 */
public class MainLayoutController {
    @FXML
    private TextField txtServerIP;
    @FXML
    private TextField txtServerPort;
    @FXML
    private ComboBox cboCipher;
    @FXML
    private TextField txtPassword;
    @FXML
    private TextField txtLocalPort;
    @FXML
    private ComboBox cboProxyType;
    @FXML
    private Button btnStart;
    @FXML
    private Button btnStop;
    @FXML
    private Button btnClose;

    private Logger logger = Logger.getLogger(MainLayoutController.class.getName());
    private MainGui gui;
    private IServer server;
    private Stage logStage;
    private Config config;

    @FXML
    private void initialize() {
        //set cipher option
        ObservableList<String> ciphers = FXCollections.observableArrayList();
        ciphers.addAll(CryptFactory.getSupportedCiphers());
        cboCipher.setItems(ciphers);

        //set proxy option
        ObservableList<IProxy.TYPE> proxy = FXCollections.observableArrayList();
        proxy.addAll(ProxyFactory.getSupportedProxyTypes());
        cboProxyType.setItems(proxy);

        //prepare configuration
        config = new Config();
        config.loadFromJson(Util.getFileContent(Constant.CONF_FILE));
        txtServerIP.setText(config.getRemoteIpAddress());
        txtServerPort.setText(String.valueOf(config.getRemotePort()));
        txtLocalPort.setText(String.valueOf(config.getLocalPort()));
        txtPassword.setText(config.getPassword());
        cboCipher.setValue(config.getMethod());
        cboProxyType.setValue(config.get_proxyType());

        //prepare log windows
        Stage stage = new Stage();
        try{
            FXMLLoader logLayoutLoader = new FXMLLoader(MainGui.class.getResource("/resources/ui/LogLayout.fxml"));
            //TODO 解释
            logLayoutLoader.setResources(ResourceBundle.getBundle("resources.bundle.ui", Constant.LOCALE, new UTF8Control()));
            Pane logLayout = logLayoutLoader.load();
            Scene logScene = new Scene(logLayout);
            stage.setTitle("Log");
            stage.setScene(logScene);
            stage.setResizable(false);
            stage.getIcons().add(new Image(MainGui.class.getResource("/resources/imag/icon.png").toString()));

            LogLayoutController controller = logLayoutLoader.getController();
            controller.setStage(stage);
            logStage = stage;
        }catch (IOException e) {
            logger.warning("Unable to load ICON: " + e.toString());
        }
        btnStop.setEnabled(false);
    }

    @FXML
    private void handdleStart() {
        boolean isValidated = false;
        do {
            if (!txtServerIP.getText().matches("[0-9]{1,4}.[0-9]{1,4}.[0-9]{1,4}.[0-9]{1,4}")) {
                showAlert(Constant.PROG_NAME, "Invalid IP address", Alert.AlertType.ERROR);
                break;
            }
            String ip = txtServerIP.getText();
            if (!txtServerPort.getText().matches("[0-9]+")) {
                showAlert(Constant.PROG_NAME, "Invalid Port", Alert.AlertType.ERROR);
                break;
            }
            int port = Integer.parseInt(txtServerPort.getText());

            String method = (String) cboCipher.getValue();
            if (txtPassword.getText().length() == 0) {
                showAlert(Constant.PROG_NAME, "Please specified password", Alert.AlertType.ERROR);
                break;
            }
            String password = txtPassword.getText();
            IProxy.TYPE type = (IProxy.TYPE) cboProxyType.getValue();
            if (!txtLocalPort.getText().matches("[0-9]+")) {
                showAlert(Constant.PROG_NAME, "Invalid Port", Alert.AlertType.ERROR);
                break;
            }
            int localPort = Integer.parseInt(txtLocalPort.getText());

            //create config
            config.setRemoteIpAddress(ip);
            config.setRemotePort(port);
            config.setLocalIpAddress("127.0.0.1");
            config.setLocalPort(localPort);
            config.setMethod(method);
            config.setPassword(password);
            config.setProxyType(type);
            Util.saveFile(Constant.CONF_FILE, config.saveToJson());

            isValidated = true;
        } while (false);

        if (!isValidated) {
            return;
        }

        //start
        try {
            server = new NioLocalServer(config);
            Thread thread = new Thread(server);
            //TODO what
            thread.setDaemon(true);
            thread.start();
            String message = String.format("(Connexted) Server %s:%d", config.getRemoteIpAddress(), config.getRemotePort());
            gui.setTooltip(message);
            gui.showNotification(message);
        } catch (IOException | InvalidAlgorithmParameterException e) {
            logger.warning("Unable to start server:" + e.toString());
        }
        btnStop.setEnabled(true);
        btnStart.setEnabled(false);
    }

    @FXML
    private void handleStop() {
        if (server != null) {
            server.close();
            String message = String.format("(Disconnected) Server %s:%d", config.getRemoteIpAddress(), config.getRemotePort());
            gui.showNotification(message);
            gui.setTooltip("Not Connected");
        }

        btnStop.setEnabled(false);
        btnStart.setEnabled(true);
    }

    public void setMainGui(MainGui gui) {
        this.gui = gui;
    }

    public void closeServer() {
        handleStop();
    }

    private void showAlert(String title, String messages, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(type.name());
        alert.setResizable(false);
        alert.setContentText(messages);
        alert.showAndWait();
    }

}
