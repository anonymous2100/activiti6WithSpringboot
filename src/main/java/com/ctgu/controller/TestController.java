package com.ctgu.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @ClassName: TestController
 * @Description: 测试用的
 * @author lh2
 * @date 2020年4月27日 上午11:36:40
 */
@Controller
@RequestMapping("")
public class TestController
{
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	ProcessEngine processEngine;
	@Autowired
	ObjectMapper objectMapper;

	@GetMapping("editor")
	public String test()
	{
		return "/modeler";
	}

	/**
	 * 新建一个空模型
	 */
	@RequestMapping("/create")
	public void newModel(HttpServletResponse response) throws IOException
	{
		RepositoryService repositoryService = processEngine.getRepositoryService();
		// 初始化一个空模型
		Model model = repositoryService.newModel();

		// 设置一些默认信息
		String name = "new-process";
		String description = "";
		int revision = 1;
		String key = "process";

		ObjectNode modelNode = objectMapper.createObjectNode();
		modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
		modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
		modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

		model.setName(name);
		model.setKey(key);
		model.setMetaInfo(modelNode.toString());

		repositoryService.saveModel(model);
		String id = model.getId();

		// 完善ModelEditorSource
		ObjectNode editorNode = objectMapper.createObjectNode();
		editorNode.put("id", "canvas");
		editorNode.put("resourceId", "canvas");
		ObjectNode stencilSetNode = objectMapper.createObjectNode();
		stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
		editorNode.set("stencilset", stencilSetNode);
		repositoryService.addModelEditorSource(id, editorNode.toString()
				.getBytes("utf-8"));
		response.sendRedirect("/modeler.html?modelId=" + id);
	}

	/**
	 * 获取所有模型
	 * 
	 * @return
	 */
	@RequestMapping("/modelist")
	public String modelList(org.springframework.ui.Model model)
	{
		List<Model> models = repositoryService.createModelQuery()
				.orderByCreateTime()
				.desc()
				.list();
		model.addAttribute("models", models);
		return "list";
	}

	/**
	 * 删除model，如果model已经发布（deploy）则不能删除
	 *
	 * @param modelId
	 * @return
	 */
	@RequestMapping(value = "/deleteModel/{modelId}")
	public String deleteModel(@PathVariable("modelId") String modelId)
	{
		repositoryService.deleteModel(modelId);
		return "redirect:/modelist";
	}

	@ResponseBody
	@GetMapping(value = "/deleteDeploy")
	public void deleteDeploy(@RequestParam(name = "id") String id)
	{
		repositoryService.deleteDeployment(id);
	}

	@ResponseBody
	@GetMapping(value = "/flowList")
	public List<Model> listFlow()
	{
		List<Model> flowList = repositoryService.createModelQuery()
				.list();
		return flowList;
	}

	@ResponseBody
	@GetMapping(value = "/flowDelete")
	public void flowDelete(@RequestParam(name = "id") String id)
	{
		System.out.println("id = " + id);
		repositoryService.deleteModel(id);
	}

	/**
	 * 发布模型为流程定义
	 */
	@RequestMapping("/deploy")
	@ResponseBody
	public Object deploy(String modelId) throws Exception
	{
		// 获取模型
		RepositoryService repositoryService = processEngine.getRepositoryService();
		Model modelData = repositoryService.getModel(modelId);
		byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

		if (bytes == null)
		{
			return "模型数据为空，请先设计流程并成功保存，再进行发布。";
		}

		JsonNode modelNode = new ObjectMapper().readTree(bytes);

		BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
		if (model.getProcesses()
				.size() == 0)
		{
			return "数据模型不符要求，请至少设计一条主线流程。";
		}
		byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);

		// 发布流程
		String processName = modelData.getName() + ".bpmn20.xml";
		Deployment deployment = repositoryService.createDeployment()
				.name(modelData.getName())
				.addString(processName, new String(bpmnBytes, "UTF-8"))
				.deploy();
		modelData.setDeploymentId(deployment.getId());
		repositoryService.saveModel(modelData);

		return "SUCCESS";
	}

	/**
	 * 启动流程
	 */
	@RequestMapping("/start")
	@ResponseBody
	public Object startProcess(String keyName)
	{
		ProcessInstance process = processEngine.getRuntimeService()
				.startProcessInstanceByKey(keyName);

		return process.getId() + " : " + process.getProcessDefinitionId();
	}

	/**
	 * 提交任务
	 */
	@RequestMapping("/run")
	@ResponseBody
	public Object run(String processInstanceId)
	{
		Task task = processEngine.getTaskService()
				.createTaskQuery()
				.processInstanceId(processInstanceId)
				.singleResult();
		processEngine.getTaskService()
				.complete(task.getId());
		return "SUCCESS";
	}

}
