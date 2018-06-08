package com.kongzhong.mrpc.admin.bootstrap;

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
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(blade.environment().getOrNull("jdbc.url"));
        config.setUsername(blade.environment().getOrNull("jdbc.username"));
        config.setPassword(blade.environment().getOrNull("jdbc.password"));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
//        config.setAutoCommit(false);

        HikariDataSource dataSource = new HikariDataSource(config);

        Anima.open(dataSource);

        pingTask.initUrl(select().from(RpcServer.class).map(Bootstrap::apply).collect(Collectors.toSet()));
    }


}
