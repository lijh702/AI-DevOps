package com.ljh.domain.agent.model.valobj.prompt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 里程碑
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MilestoneVO {

    private Type type;          // 事件类型（枚举）
    private String content;     // 事件内容（文本）
    private long timestamp;     // 发生时间（时间戳）

    /**
     *
     枚举值	            含义	    触发场景	                示例内容
     TASK_CHANGE	    任务变更	用户改变主意、调整方向	    "不对，应该用PostgreSQL而不是MySQL"
     TASK_COMPLETE	    任务完成	用户确认任务结束	        "部署完成了，可以访问了"
     USER_CORRECTION	用户纠正	用户明确制止AI的行为	    "不要删除这个文件！"
     ERROR	            工具错误	命令执行失败、系统报错	    "Permission denied: /var/log/app.log"
     DECISION	        关键决策	用户做出重要选择	        "我决定使用微服务架构"
     FILE_SWITCH	    文件切换	用户在编辑不同文件	        "切换到 application.yml"
     */
    public enum Type {
        TASK_CHANGE,
        TASK_COMPLETE,
        USER_CORRECTION,
        ERROR,
        DECISION,
        FILE_SWITCH
    }
}
