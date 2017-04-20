package com.kongzhong.mrpc.model;

import java.io.Serializable;

public class Header implements Serializable {

    private short magic;
    private byte version;
    private String messageId;
    private int contentLength;

    public Header(short magic, byte version, String messageId, int contentLength) {
        this.magic = magic;
        this.version = version;
        this.messageId = messageId;
        this.contentLength = contentLength;
    }

    public short getMagic() {
        return magic;
    }

    public void setMagic(short magic) {
        this.magic = magic;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public String toString() {
        return "Header{" +
                "magic=" + magic +
                ", version=" + version +
                ", messageId='" + messageId + '\'' +
                ", contentLength=" + contentLength +
                '}';
    }
}
