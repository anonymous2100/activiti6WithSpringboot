package com.ctgu.service.impl;

import java.util.List;

import org.activiti.engine.DynamicBpmnService;
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;

import com.ctgu.service.ActivitiService;

public class ActivitiServiceImpl implements ActivitiService
{
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private HistoryService historyService;
	@Autowired
	private ManagementService managementService;
	@Autowired
	private IdentityService identityService;
	@Autowired
	private FormService formService;
	@Autowired
	private DynamicBpmnService dynamicBpmnService;

	/**
	 * 历史活动节点查询，开发之中这个用的最多,针对是某一个完整的过程
	 */
	@Override
	public List<HistoricActivityInstance> historyAcInstanceList(String processInstanceId)
	{
		List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery()    // 创建历史活动实例查询
				.processInstanceId(processInstanceId) // 指定流程实例id
				.orderByHistoricActivityInstanceEndTime()
				.asc()
				.list();
		return list;
	}

	@Override
	public ProcessInstance startProcessInstanceByKey(String keyName)
	{
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(keyName);
		return processInstance;
	}

	@Override
	public void deleteModel(String modelId)
	{
		repositoryService.deleteModel(modelId);
	}

	@Override
	public List<Model> getModelList()
	{
		List<Model> modelList = repositoryService.createModelQuery()
				.orderByCreateTime()
				.desc()
				.list();

		return modelList;
	}

	@Override
	public void completeTask(String processInstanceId)
	{
		Task task = taskService.createTaskQuery()
				.processInstanceId(processInstanceId)
				.singleResult();
		taskService.complete(task.getId());
	}

}
