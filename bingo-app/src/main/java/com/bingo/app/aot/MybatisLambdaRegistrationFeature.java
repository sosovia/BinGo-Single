package com.bingo.app.aot;


//import com.proboot.system.api.RemoteUserService;
//import com.proboot.system.api.impl.RemoteClientServiceImpl;
//import com.proboot.system.api.impl.RemoteUserServiceImpl;
//import com.proboot.system.controller.SysClientController;
//import com.proboot.system.dal.mapper.SysMenuMapper;
//import com.proboot.system.service.SysClientServiceImpl;
//import com.proboot.system.service.SysMenuServiceImpl;
//import com.proboot.system.service.SysUserServiceImpl;
//import com.proboot.mybatisnative.service.SysClientServiceImpl;
//import com.proboot.mybatisnative.service.SysMenuServiceImpl;


import org.graalvm.nativeimage.hosted.Feature;


/**
 * lambda 表达式注入到graal中
 */
public class MybatisLambdaRegistrationFeature implements Feature {

    @Override
    public void duringSetup(DuringSetupAccess access) {
        // TODO 这里需要将lambda表达式所使用的成员类都注册上来,具体情况视项目情况而定,一般扫描@Controller和@Service的会多点.

//        RuntimeSerialization.registerLambdaCapturingClass(ClientController.class);
//        RuntimeSerialization.registerLambdaCapturingClass(SysClientMapper.class);


//        RuntimeSerialization.registerLambdaCapturingClass(SysClientServiceImpl.class);
//        RuntimeSerialization.registerLambdaCapturingClass(SysMenuServiceImpl.class);
//        RuntimeSerialization.registerLambdaCapturingClass(SysUserServiceImpl.class);
//        RuntimeSerialization.registerLambdaCapturingClass(SysMenuMapper.class);
//        RuntimeSerialization.registerLambdaCapturingClass(RemoteClientServiceImpl.class);
//        RuntimeSerialization.registerLambdaCapturingClass(RemoteUserServiceImpl.class);

//        RuntimeSerialization.registerLambdaCapturingClass(SysClientServiceImpl.class);
//        RuntimeSerialization.registerLambdaCapturingClass(SysMenuServiceImpl.class);

//        RuntimeSerialization.registerLambdaCapturingClass(RemoteClientServiceImpl.class);
//        RuntimeSerialization.registerLambdaCapturingClass(RemoteUserServiceImpl.class);
//        RuntimeSerialization.registerLambdaCapturingClass(SysClientServiceImpl.class);
//        RuntimeSerialization.registerLambdaCapturingClass(SysMenuServiceImpl.class);
//        RuntimeSerialization.registerLambdaCapturingClass(SysUserServiceImpl.class);

    }
}
