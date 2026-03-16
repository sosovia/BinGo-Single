package com.bingo.system.dict.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bingo.common.mybatis.mapper.BaseMapperX;
import com.bingo.system.dict.domain.entity.SysDictData;
import com.bingo.system.dict.domain.vo.SysDictDataVo;


import java.util.List;

/**
 * 字典表 数据层
 */
public interface SysDictDataMapper extends BaseMapperX<SysDictData> {

    /**
     * 根据字典类型查询字典数据列表
     *
     * @param dictType 字典类型
     * @return 符合条件的字典数据列表
     */
    default List<SysDictDataVo> selectDictDataByType(String dictType) {
        return selectVoList(
            new LambdaQueryWrapper<SysDictData>()
                .eq(SysDictData::getDictType, dictType)
                .orderByAsc(SysDictData::getDictSort), SysDictDataVo.class);
    }
}
