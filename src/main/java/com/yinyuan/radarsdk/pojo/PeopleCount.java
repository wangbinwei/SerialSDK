package com.yinyuan.radarsdk.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author kuangchang
 * @since 2021-08-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PeopleCount implements Serializable {

    private static final long serialVersionUID = 1L;


    private Integer id;


    private Integer peopleNum;


    private Date startTime;


    private Date endTime;


    private Integer countId;


    private Integer trueNum;


    private Date createTime;


    private String sn;

    public PeopleCount(Integer peopleNum, Date startTime, Date endTime, Integer countId, Integer trueNum, String sn) {
        this.peopleNum = peopleNum;
        this.startTime = startTime;
        this.endTime = endTime;
        this.countId = countId;
        this.trueNum = trueNum;
        this.sn = sn;
    }

    public PeopleCount(int size, Date startTime, String radarSn, LocalDateTime time) {
        this.peopleNum = size;
        this.startTime = startTime;
        this.sn = radarSn;
        this.createTime = Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }


    public PeopleCount(Integer peopleNum) {
        this.peopleNum = peopleNum;
    }
}
