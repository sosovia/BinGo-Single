package com.bingo.system.dict.controller;


import com.bingo.common.core.domain.R;
import com.bingo.common.core.domain.page.PageQuery;
import com.bingo.system.dict.domain.bo.SysDictDataBo;
import com.bingo.system.dict.domain.vo.SysDictDataVo;
import com.bingo.system.dict.mapper.SysDictDataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据字典信息
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/dict/data")
public class SysDictDataController {

    private final SysDictDataMapper sysDictDataMapper;

    /**
     * 分页查询字典数据列表。
     *
     * @param dictData  查询条件
     * @param pageQuery 分页参数
     * @return 字典数据分页结果
     */
//    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    public R<List<SysDictDataVo>> list(SysDictDataBo dictData, PageQuery pageQuery) {

        return R.ok(sysDictDataMapper.selectDictDataByType("1234"));
    }
}
