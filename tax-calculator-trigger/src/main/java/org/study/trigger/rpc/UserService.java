package org.study.trigger.rpc;

import org.study.api.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Profile;

@Slf4j
@Profile("!local")
@DubboService(version = "1.0.0", timeout = 450)
public class UserService implements IUserService {

    @Override
    public String queryUserInfo(String req) {
        return "查询用户信息";
    }

}
