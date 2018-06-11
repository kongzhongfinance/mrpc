package com.kongzhong.mrpc.admin.bootstrap;

import com.alibaba.druid.pool.DruidDataSource;
import com.blade.Blade;
import com.blade.event.BeanProcessor;
import com.blade.ioc.annotation.Bean;
import com.blade.ioc.annotation.Inject;
import com.blade.kit.JsonKit;
import com.blade.kit.json.JacksonSupport;
import com.blade.mvc.view.template.JetbrickTemplateEngine;
import com.blade.validator.Validators;
import com.kongzhong.mrpc.admin.model.RpcServer;
import com.kongzhong.mrpc.admin.tasks.PingTask;
import io.github.biezhi.anima.Anima;

import java.util.stream.Collectors;

import static io.github.biezhi.anima.Anima.select;

/**
 * @author biezhi
 * @date 2018/6/6
 */
@Bean
public class Bootstrap implements BeanProcessor {

    @Inject
    private PingTask pingTask;

    private static String apply(RpcServer s) {return s.getHost() + ":" + s.getPort();}

    @Override
    public void processor(Blade blade) {
        Validators.useChinese();
        JsonKit.jsonSupprt(new JacksonSupport());

        JetbrickTemplateEngine templateEngine = new JetbrickTemplateEngine();

        blade.templateEngine(templateEngine);

        // JDBC
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(blade.environment().getOrNull("jdbc.url"));
        dataSource.setUsername(blade.environment().getOrNull("jdbc.username"));
        dataSource.setPassword(blade.environment().getOrNull("jdbc.password"));

        Anima.open(dataSource);

        pingTask.initUrl(select().from(RpcServer.class).map(Bootstrap::apply).collect(Collectors.toSet()));
    }


}
