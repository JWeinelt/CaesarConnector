package de.julianweinelt.caesar.connection;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.function.Consumer;

public class LiveConsoleHandler extends Handler {

    private volatile boolean live = false;
    private Consumer<String> callback;

    public void setLive(boolean live, Consumer<String> callback) {
        this.live = live;
        this.callback = callback;
    }

    @Override
    public void publish(LogRecord record) {
        if (!live || callback == null) return;

        String msg = getFormatter().format(record);
        callback.accept(msg);
    }

    @Override
    public void flush() {}
    @Override
    public void close() throws SecurityException {}
}
