package demo;

import com.kongzhong.mrpc.model.RpcRequest;
import com.kongzhong.mrpc.serialize.KyroSerialize;
import com.kongzhong.mrpc.utils.StringUtils;

/**
 * @author biezhi
 *         2017/4/26
 */
public class TestSerailize {

    public void aaa() {

    }

    public static void main(String[] args) throws NoSuchMethodException {
        RpcRequest request = new RpcRequest();
        request.setRequestId(StringUtils.getUUID());
//        request.setMethod(TestSerailize.class.getMethod("aaa"));

        KyroSerialize kyroSerialize = new KyroSerialize();
        try {
            byte[] data = kyroSerialize.serialize(request);
            RpcRequest b = kyroSerialize.deserialize(data, RpcRequest.class);
            System.out.println(b.getRequestId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
