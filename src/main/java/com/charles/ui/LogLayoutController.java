package com.charles.ui;

import com.charles.misc.Log;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.awt.*;

/**
 * Created by Administrator on 2017/9/8.
 */
public class LogLayoutController {
    @FXML
    public TextArea txtLog;
    @FXML
    private Button btnClose;
    @FXML
    private Button btnClear;

    private Stage stage;

    @FXML
    private void initialize() {
        TextAreaLogHandler handler = new TextAreaLogHandler();
        handler.setTextArea(txtLog);
        Log.addHandler(handler);
    }

    @FXML
    private void handlerClear() {
        txtLog.setText("");
    }

    @FXML
    private void handleClose() {
        // STOPSHIP: 2017/9/8
        stage.hide();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
