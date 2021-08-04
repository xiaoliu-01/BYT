package com.register.byt.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.register.byt.cmn.mapper.DictMapper;
import com.register.model.entity.cmn.Dict;
import com.register.model.vo.cmn.DictEeVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LLXX
 * @create 2021-08-02 15:58
 */
//@Component
@Slf4j
public class DictListener extends AnalysisEventListener<DictEeVo> {
    //@Resource
    private DictMapper dictMapper;

    public DictListener(DictMapper dictMapper) {
        this.dictMapper = dictMapper;
    }

    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private List<Dict> dicts = new ArrayList<>();
    private static final Integer BATCH_COUNT = 5;

    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo, dict);
        log.info("解析到一条数据{}", dict);
        dicts.add(dict);
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (dicts.size() >= BATCH_COUNT) {
            saveData();
            // 保存完进行数据清空
            dicts.clear();
        }
    }

    // 批量保存数据字典到数据库
    public void saveData(){
        log.info("{}条数据，开始存储数据库！", dicts.size());
        dictMapper.insertBatchDict(dicts);  //批量插入
        log.info("存储数据库成功！");
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        // 这里也要保存数据，确保最后遗留的数据也存储到数据库
        saveData();
        log.info("所有数据解析完成！");
    }
}
