package com.yinyuan.radarsdk.service;


import com.yinyuan.radarsdk.pojo.PeopleCount;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kuangchang
 * @since 2021-08-12
 */
public interface PeopleCountService {
    void insertData(PeopleCount peopleCount);

    void insertData2List(PeopleCount peopleCount);

    Integer getResultFromList();
}
