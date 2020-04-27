package com.ctgu.vo.output;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HistoricActivityInstanceVO implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3604888789969862439L;

	@ApiModelProperty(value = "活动id")
	private String id;

	@ApiModelProperty(value = "活动流程id")
	private String activityId;

	@ApiModelProperty(value = "活动流程名称")
	private String activityName;

	@ApiModelProperty(value = "节点类型")
	private String activityType;

	@ApiModelProperty(value = "流程定义id")
	private String processDefinitionId;

	@ApiModelProperty(value = "执行实例id")
	private String executionId;

	@ApiModelProperty(value = "历史流程实例id")
	private String processInstanceId;

	@ApiModelProperty(value = "任务id")
	private String taskId;

	@ApiModelProperty(value = "调用外部流程的流程实例ID")
	private String calledProcessInstanceId;

	@ApiModelProperty(value = "任务委派人")
	private String assignee;

	@ApiModelProperty(value = "历史流程开始时间")
	private Date startTime;

	@ApiModelProperty(value = "历史流程结束时间")
	private Date endTime;

	@ApiModelProperty(value = "耗时，毫秒值")
	private Long durationInMillis;

	@ApiModelProperty(value = "删除节点原因")
	private String deleteReason;

	@ApiModelProperty(value = "tenantId")
	private String tenantId;

	@ApiModelProperty(value = "时间")
	private Date time;

	// -----------------------------------------------
	@ApiModelProperty(value = "描述，标识是否完成过任务，审批节点")
	private String description;
}
