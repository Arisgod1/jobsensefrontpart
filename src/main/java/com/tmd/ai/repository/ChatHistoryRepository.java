package com.tmd.ai.repository;

import java.util.List;

public interface ChatHistoryRepository {
    /*
    * 保存会话记录
    * @param type 业务类型 如:chat service pdf
    * @param chatId
    * */
    void save(String type,String chatId);


    /*
    * 获取会话ID列表
    * @param type 业务类型 如:chat service pdf
    *  @param chatId
    * */
    List<String> getChatIds(String type);
}
