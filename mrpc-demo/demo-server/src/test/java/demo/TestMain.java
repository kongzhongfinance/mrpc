package demo;

import com.kongzhong.mrpc.demo.exception.BizException;
import com.kongzhong.mrpc.exception.SerializeException;
import com.kongzhong.mrpc.serialize.jackson.JacksonSerialize;

/**
 * @author biezhi
 *         2017/5/21
 */
public class TestMain {

    public static void main(String[] args) throws SerializeException {
        String jsonError = JacksonSerialize.toJSONString(new BizException(1001, "fuck mrpc"));
        System.out.println(jsonError);

        System.out.println("-------------");
        BizException bizException = JacksonSerialize.parseObject(jsonError, BizException.class);
        System.out.println(bizException.getClass());
        bizException.printStackTrace();
    }
}
