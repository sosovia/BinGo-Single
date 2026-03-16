package com.bingo.common.mybatis.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.func.Func1;
import cn.hutool.core.lang.func.LambdaUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bingo.common.core.domain.page.PageQuery;
import com.bingo.common.core.exception.ServiceException;
import com.bingo.common.core.utils.StringUtils;
import com.bingo.common.core.utils.sql.SqlUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.bingo.common.core.domain.page.PageQuery.DEFAULT_PAGE_NUM;
import static com.bingo.common.core.domain.page.PageQuery.DEFAULT_PAGE_SIZE;
import static com.bingo.common.mybatis.enums.DataBaseType.*;

/**
 * MyBatis 工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MyBatisUtils {


    /**
     * 构建分页对象
     */
    public static <T> Page<T> buildPageQuery(PageQuery pageQuery) {
        Integer pageNum = ObjectUtil.defaultIfNull(pageQuery.getPageNum(), DEFAULT_PAGE_NUM);
        Integer pageSize = ObjectUtil.defaultIfNull(pageQuery.getPageSize(), DEFAULT_PAGE_SIZE);
        if (pageNum <= 0) {
            pageNum = DEFAULT_PAGE_NUM;
        }
        Page<T> page = new Page<>(pageNum, pageSize);
        List<OrderItem> orderItems = buildOrderItem(pageQuery);
        if (CollUtil.isNotEmpty(orderItems)) {
            page.addOrder(orderItems);
        }
        return page;
    }

    /**
     * 构建排序
     * <p>
     * 支持的用法如下:
     * {isAsc:"asc",orderByColumn:"id"} order by id asc
     * {isAsc:"asc",orderByColumn:"id,createTime"} order by id asc,create_time asc
     * {isAsc:"desc",orderByColumn:"id,createTime"} order by id desc,create_time desc
     * {isAsc:"asc,desc",orderByColumn:"id,createTime"} order by id asc,create_time desc
     */
    private static List<OrderItem> buildOrderItem(PageQuery pageQuery) {
        if (StringUtils.isBlank(pageQuery.getOrderByColumn()) || StringUtils.isBlank(pageQuery.getIsAsc())) {
            return null;
        }
        String orderBy = SqlUtil.escapeOrderBySql(pageQuery.getOrderByColumn());
        orderBy = StringUtils.toUnderScoreCase(orderBy);

        // 兼容前端排序类型
        String isAsc = StringUtils.replaceEach(pageQuery.getIsAsc(), new String[]{"ascending", "descending"}, new String[]{"asc", "desc"});

        String[] orderByArr = orderBy.split(StringUtils.SEPARATOR);
        String[] isAscArr = isAsc.split(StringUtils.SEPARATOR);
        if (isAscArr.length != 1 && isAscArr.length != orderByArr.length) {
            throw new ServiceException("排序参数有误");
        }

        List<OrderItem> list = new ArrayList<>();
        // 每个字段各自排序
        for (int i = 0; i < orderByArr.length; i++) {
            String orderByStr = orderByArr[i];
            String isAscStr = isAscArr.length == 1 ? isAscArr[0] : isAscArr[i];
            if ("asc".equals(isAscStr)) {
                list.add(OrderItem.asc(orderByStr));
            } else if ("desc".equals(isAscStr)) {
                list.add(OrderItem.desc(orderByStr));
            } else {
                throw new ServiceException("排序参数有误");
            }
        }
        return list;
    }


    /**
     * 根据当前数据库类型，生成兼容的 FIND_IN_SET 语句片段
     * <p>
     * 用于判断指定值是否存在于逗号分隔的字符串列中，SQL写法根据不同数据库方言自动切换：
     * - Oracle 使用 instr 函数
     * - PostgreSQL 使用 strpos 函数
     * - SQL Server 使用 charindex 函数
     * - 其他默认使用 MySQL 的 find_in_set 函数
     *
     * @param var1 要查找的值（支持任意类型，内部会转换成字符串）
     * @param var2 存储逗号分隔值的数据库列名
     * @return 适用于当前数据库的 SQL 条件字符串，通常用于 where 或 apply 中拼接
     */
    public static String findInSet(Object var1, String var2) {
        String var = Convert.toStr(var1);
        return switch (JdbcUtils.getDbType()) {
            // instr(',0,100,101,' , ',100,') <> 0
            case ORACLE -> "instr(','||%s||',' , ',%s,') <> 0".formatted(var2, var);
            // (select strpos(',0,100,101,' , ',100,')) <> 0
            case POSTGRE_SQL -> "(select strpos(','||%s||',' , ',%s,')) <> 0".formatted(var2, var);
            // charindex(',100,' , ',0,100,101,') <> 0
            case SQL_SERVER -> "charindex(',%s,' , ','+%s+',') <> 0".formatted(var, var2);
            // find_in_set(100 , '0,100,101')
            default -> "find_in_set('%s' , %s) <> 0".formatted(var, var2);
        };
    }


    /**
     * 将拦截器添加到链中
     * 由于 MybatisPlusInterceptor 不支持添加拦截器，所以只能全量设置
     *
     * @param interceptor 链
     * @param inner       拦截器
     * @param index       位置
     */
    public static void addInterceptor(MybatisPlusInterceptor interceptor, InnerInterceptor inner, int index) {
        List<InnerInterceptor> inners = new ArrayList<>(interceptor.getInterceptors());
        inners.add(index, inner);
        interceptor.setInterceptors(inners);
    }


    /**
     * 将驼峰命名转换为下划线命名
     * <p>
     * 使用场景：
     * 1. <a href="https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/1357/files">fix:修复"商品统计聚合函数的别名与排序字段不符"导致的 SQL 异常</a>
     *
     * @param func 字段名函数(驼峰命名)
     * @return 字段名(下划线命名)
     */
    public static <T> String toUnderlineCase(Func1<T, ?> func) {
        String fieldName = LambdaUtil.getFieldName(func);
        return StrUtil.toUnderlineCase(fieldName);
    }
}
