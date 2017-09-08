package com.charles.ui;

import javafx.application.Platform;

import java.awt.*;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * Created by Administrator on 2017/9/8.
 */
public class TextAreaLogHandler extends StreamHandler{
    TextArea textArea = null;

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void publish(LogRecord record) {
        final LogRecord lg = record;
        super.publish(record);
        flush();

        if (textArea != null) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    //limit at 64k
                    if (textArea.getText().length() > 65535) {
                        textArea.setText("");
                    }
                    textArea.append(getFormatter().format(lg));
                }
            });
        }
    }
}
