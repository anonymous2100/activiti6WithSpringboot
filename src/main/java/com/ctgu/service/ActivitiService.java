package com.ctgu.service;

import java.util.List;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;

public interface ActivitiService
{

	/**
	 * 历史活动节点查询，开发之中这个用的最多,针对是某一个完整的过程
	 */
	List<HistoricActivityInstance> historyAcInstanceList(String processInstanceId);

	ProcessInstance startProcessInstanceByKey(String keyName);

	void deleteModel(String modelId);

	List<Model> getModelList();

	void completeTask(String processInstanceId);

}
