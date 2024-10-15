package org.k3scc.jabletvrepo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.k3scc.jabletvrepo.system")
public class JableTvRepoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JableTvRepoApplication.class, args);
    }

}
