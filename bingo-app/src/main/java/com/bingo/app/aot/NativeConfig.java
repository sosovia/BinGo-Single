package com.bingo.app.aot;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReflectUtil;

import com.bingo.common.core.utils.SpringUtils;
import com.bingo.common.json.utils.JsonUtils;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.stream.Stream;

@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(NativeConfig.AppNativeConfig.class)
public class NativeConfig {

    static class AppNativeConfig implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

            Stream.of(
                    "mapper/**/*Mapper.xml"
            ).forEach(hints.resources()::registerPattern);

            // hutool
            hints.reflection().registerType(Snowflake.class, MemberCategory.values());
            hints.reflection().registerType(BeanUtil.class, MemberCategory.values());
            hints.reflection().registerType(ObjectUtil.class, MemberCategory.values());
            hints.reflection().registerType(ReflectUtil.class, MemberCategory.values());

            hints.reflection().registerType(SpringUtils.class, MemberCategory.values());
            hints.reflection().registerType(JsonUtils.class, MemberCategory.values());

//            hints.reflection().registerType(LoginBody.class, MemberCategory.values());
//            hints.reflection().registerType(PasswordLoginBody.class, MemberCategory.values());
//            hints.reflection().registerType(LoginUserDTO.class, MemberCategory.values());
//            hints.reflection().registerType(LoginUser.class, MemberCategory.values());
        }
    }
}
