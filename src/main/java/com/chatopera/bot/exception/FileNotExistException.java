package com.chatopera.bot.exception;

public class FileNotExistException extends Exception {
    public FileNotExistException(final String msg) {
        super(msg);
    }
    public FileNotExistException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
