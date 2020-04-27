package com.ctgu.service;

import org.activiti.engine.history.HistoricProcessInstance;

public interface ImageService
{
	HistoricProcessInstance queryHistoricProcessInstance(String processInstanceId);

	byte[] getProcessImage(String processId) throws Exception;

	/**
	 * 根据流程实例id获取流程执行图
	 */
	byte[] getFlowImgByProcInstId(String procInstanceId) throws Exception;

}
