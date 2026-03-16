package com.bingo.common.datapermission.config;


import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.bingo.common.datapermission.aspect.DataPermissionPointcutAdvisor;
import com.bingo.common.datapermission.interceptor.PlusDataPermissionInterceptor;
import com.bingo.common.mybatis.utils.MyBatisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@EnableTransactionManagement(proxyTargetClass = true)
public class DataPermissionConfig {

    public void setDataPermissionInterceptor(MybatisPlusInterceptor interceptor) {

        // 添加到 interceptor 中
        // 需要加在首个，主要是为了在分页插件前面。这个是 MyBatis Plus 的规定
        MyBatisUtils.addInterceptor(interceptor, dataPermissionInterceptor(), 0);
        log.info("数据权限拦截器已添加");
    }


    /**
     * 数据权限拦截器
     */
    public PlusDataPermissionInterceptor dataPermissionInterceptor() {
        return new PlusDataPermissionInterceptor();
    }

    /**
     * 数据权限切面处理器
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public DataPermissionPointcutAdvisor dataPermissionPointcutAdvisor() {
        return new DataPermissionPointcutAdvisor();
    }

}
