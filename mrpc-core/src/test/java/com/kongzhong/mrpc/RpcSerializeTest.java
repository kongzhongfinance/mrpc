package com.kongzhong.mrpc;

import com.kongzhong.mrpc.model.Bar;
import com.kongzhong.mrpc.model.Foo;
import com.kongzhong.mrpc.serialize.KyroSerialize;
import com.kongzhong.mrpc.serialize.ProtostuffSerialize;
import com.kongzhong.mrpc.serialize.RpcSerialize;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * kyro序列化测试
 *
 * @author biezhi
 *         28/06/2017
 */
public class RpcSerializeTest {

    private RpcSerialize rpcSerialize;


    @Test
    public void testKyroSerialize() throws Exception {
        rpcSerialize = new KyroSerialize();

        Foo foo = createFooInstance();

        byte[] data = rpcSerialize.serialize(foo);

        Foo x = rpcSerialize.deserialize(data, Foo.class);
        System.out.println(x);
        assertThat(foo, is(x));
    }

    @Test
    public void testProtostuffSerialize() throws Exception {
        rpcSerialize = new ProtostuffSerialize();

        Foo foo = createFooInstance();

        byte[] data = rpcSerialize.serialize(foo);

        Foo x = rpcSerialize.deserialize(data, Foo.class);
        System.out.println(x);
        assertThat(foo, is(x));
    }

    private Foo createFooInstance() {
        Foo foo;
        foo = new Foo();
        foo.setBar(Bar.builder()
                .id(42)
                .name("Bar")
                .build());
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "One");
        map.put(42, "Forty Two");
        foo.setMap(map);
        return foo;
    }

}
