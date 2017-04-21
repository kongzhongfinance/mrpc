package com.kongzhong.mrpc.context;

import com.kongzhong.mrpc.transport.TransferSelector;

/**
 * @author biezhi
 *         2017/4/21
 */
public class RpcContext {

    private TransferSelector transferSelector;

    public RpcContext(TransferSelector transferSelector) {
        this.transferSelector = transferSelector;
    }

    public TransferSelector getTransferSelector() {
        return transferSelector;
    }

    public void setTransferSelector(TransferSelector transferSelector) {
        this.transferSelector = transferSelector;
    }
}
