package com.charles.ui;

import com.charles.MainGui;
import com.charles.misc.Config;
import com.charles.network.IServer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.awt.*;
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
    private TextField cboCipher;
    @FXML
    private TextField txtPassword;
    @FXML
    private TextField txtLocalPort;
    @FXML
    private TextField cboProxyType;
    @FXML
    private Button btnStart;
    @FXML
    private Button btnStop;

    private Logger logger = Logger.getLogger(MainLayoutController.class.getName());
    private MainGui gui;
    private IServer server;
    private Stage logStage;
    private Config config;

    @FXML
    private void initialize() {
        //set cipher
        ObservableList<String> ciphers = FXCollections.observableArrayList();
        ciphers.addAll(CryptF)
        cboCipher.setItems();
    }

}
