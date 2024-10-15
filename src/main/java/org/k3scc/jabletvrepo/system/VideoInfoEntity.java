package org.k3scc.jabletvrepo.system;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@TableName("video_info")//@TableName中的值对应着表名
public class VideoInfoEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String status;
    private String code;
    private String title;
    private String videoUrl;
    private String image;
    private String imageUrl;
    private Date createTime;

}