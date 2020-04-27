package com.ctgu.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ctgu.common.ResultMsg;
import com.ctgu.service.ActivitiService;
import com.ctgu.service.ImageService;
import com.ctgu.vo.input.ModelVO;
import com.ctgu.vo.output.HistoricActivityInstanceVO;
import com.ctgu.vo.output.ImageVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @ClassName: ActivitiController
 * @Description:封装工作流基本业务操作<br>
 *                              1.模型管理 ：在线流程设计器创建模型、删除模型、部署流程、预览流程xml、导出xml、导出json <br>
 *                              2.流程管理 ：导入导出流程资源文件、查看流程图、根据流程实例反射出流程模型、激活挂起<br>
 *                              3.运行中流程：查看流程信息、当前任务节点、当前流程图、作废暂停流程、指派待办人<br>
 *                              4.历史的流程：查看流程信息、流程用时、流程状态、查看任务发起人信息 <br>
 *                              5.待办任务：查看本人个人任务以及本角色下的任务、办理、驳回、作废、指派一下代理人 <br>
 *                              6.已办任务 ：查看自己办理过的任务以及流程信息、流程图、流程状态(作废 驳回 正常完成)<br>
 * @author lh2
 * @date 2020年4月26日 下午10:04:25
 */
@Slf4j
@RestController
@RequestMapping("/activiti")
public class ActivitiController
{
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ActivitiService activitiService;
	@Autowired
	private ImageService imageService;
	@Autowired
	ObjectMapper objectMapper;

	// 1.模型管理 ：在线流程设计器创建模型、删除模型、部署流程、预览流程xml、导出xml、导出json
	// 模型创建
	@PostMapping("/model/create")
	public ResultMsg modelCreate(ModelVO modelVO) throws Exception
	{
		// 初始化一个空模型
		Model model = repositoryService.newModel();
		// 设置一些默认信息
		// String name = "newProcess";
		// String description = "";
		// int revision = 1;
		// String key = "process";
		String name = modelVO.getProcessName();
		String description = modelVO.getProcessDescription();
		int revision = modelVO.getProcessRevision();
		String key = modelVO.getProcessKey();

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
		// response.sendRedirect("/modeler.html?modelId=" + id);

		List<Model> modelList = activitiService.getModelList();

		return ResultMsg.success(modelList);
	}

	// 模型删除
	@GetMapping(value = "/model/delete-by/{modelId}")
	public ResultMsg modelDelete(@PathVariable("modelId") String modelId)
	{
		activitiService.deleteModel(modelId);

		return ResultMsg.success();
	}

	// 获取模型列表
	@GetMapping("/model/list")
	public ResultMsg modelList()
	{
		List<Model> modelList = activitiService.getModelList();

		return ResultMsg.success(modelList);
	}

	// 模型部署，发布模型为流程定义
	@GetMapping("/model/deploy")
	public ResultMsg modelDeploy(String modelId) throws Exception
	{
		// 获取模型
		Model modelData = repositoryService.getModel(modelId);
		byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

		if (bytes == null)
		{
			return ResultMsg.fail("模型数据为空，请先设计流程并成功保存，再进行发布");
		}
		JsonNode modelNode = new ObjectMapper().readTree(bytes);
		BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
		if (model.getProcesses()
				.size() == 0)
		{
			return ResultMsg.fail("数据模型不符要求，请至少设计一条主线流程");
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

		return ResultMsg.success();
	}

	// 模型xml预览
	@GetMapping("/model/view-xml")
	public ResultMsg modelViewXml()
	{

		return ResultMsg.success();
	}

	// 模型导出为xml
	@GetMapping("/model/export-xml")
	public ResultMsg modelExportXml()
	{
		return ResultMsg.success();
	}

	// 模型导出为json
	@GetMapping("/model/export-json")
	public ResultMsg modelExportJson()
	{
		return ResultMsg.success();
	}

	// 2.流程管理 ：导入导出流程资源文件、查看流程图、根据流程实例反射出流程模型、激活挂起
	// 流程导入
	@GetMapping("/process/import")
	public ResultMsg processImport()
	{
		return ResultMsg.success();
	}

	// 流程导出
	@GetMapping("/process/outport")
	public ResultMsg processOutport()
	{
		return ResultMsg.success();
	}

	// 流程图查看
	@GetMapping("/process/image-view")
	public ResultMsg processImageView()
	{
		return ResultMsg.success();
	}

	// 流程定义转为流程模型
	@GetMapping("/process/define-to-model")
	public ResultMsg processDefToModel()
	{
		return ResultMsg.success();
	}

	// 流程模型转流程定义
	@GetMapping("/process/model-to-define")
	public ResultMsg processModelToDefine()
	{
		return ResultMsg.success();
	}

	// 流程激活
	@GetMapping("/process/activate")
	public ResultMsg processActivate()
	{
		return ResultMsg.success();
	}

	// 流程挂起
	@GetMapping("/process/suspend")
	public ResultMsg processSuspend()
	{
		return ResultMsg.success();
	}

	// 流程启动
	@GetMapping("/process/start-by-key/{key}")
	public ResultMsg processStart(@PathVariable("key") String key)
	{
		ProcessInstance processInstance = activitiService.startProcessInstanceByKey(key);
		log.info("流程已启动" + processInstance.getId() + " : " + processInstance.getProcessDefinitionId());

		return ResultMsg.success(processInstance);
	}

	// 3.运行中流程：查看流程信息、当前任务节点、当前流程图、作废暂停流程、指派待办人
	// 查看流程信息
	@GetMapping("/running-process/view-info")
	public ResultMsg runningProcessViewInfo()
	{
		return ResultMsg.success();
	}

	// 当前任务节点
	@GetMapping("/running-process/view-current-task")
	public ResultMsg runningProcessViewCurrentTask()
	{
		return ResultMsg.success();
	}

	// 当前流程图
	@GetMapping(value = "/running-process/view-image")
	public ResultMsg runningProcessViewImage()
	{

		return ResultMsg.success();
	}

	/**
	 * 查看流程定义图片(后端开发调试使用)
	 * 
	 * @param processInstanceId
	 * @param response
	 */
	@ApiOperation(value = "查看图片")
	@GetMapping(value = "/view-process-img-by/{processInstanceId}", produces = MediaType.IMAGE_JPEG_VALUE)
	public byte[] viewProcessImage(@PathVariable("processInstanceId") String processInstanceId,
			HttpServletResponse response)
	{
		// 设置页面不缓存
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);
		try
		{
			byte[] processImage = imageService.getFlowImgByProcInstId(processInstanceId);
			return processImage;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 查看流程定义图片Base64(前端展示使用)
	 *
	 * @param processInstanceId
	 * @param response
	 * @throws Exception
	 */
	@ApiOperation(value = "前端展示使用")
	@RequestMapping(value = "/viewProcessImgBy/{processInstanceId}", method = RequestMethod.GET)
	@ResponseBody
	public ResultMsg viewProcessImg(@PathVariable("processInstanceId") String processInstanceId,
			HttpServletResponse response) throws Exception
	{
		byte[] processImage = imageService.getFlowImgByProcInstId(processInstanceId);
		// 图片：对字节数组Base64编码
		Encoder encoder = Base64.getEncoder();
		String base64 = encoder.encodeToString(processImage);
		// 活动列表
		List<HistoricActivityInstance> list = activitiService.historyAcInstanceList(processInstanceId);
		List<HistoricActivityInstanceVO> result = new ArrayList<HistoricActivityInstanceVO>();
		for (HistoricActivityInstance hisac : list)
		{
			log.info("活动id:" + hisac.getId());
			log.info("任务id:" + hisac.getTaskId());
			log.info("办理人:" + hisac.getAssignee());
			log.info("活动流程id：" + hisac.getActivityId());
			log.info("活动流程名称：" + hisac.getActivityName());
			log.info("活动流程实例id：" + hisac.getProcessInstanceId());
			log.info("活动流程开始时间：" + hisac.getStartTime());
			log.info("活动流程结束时间：" + hisac.getEndTime());
			log.info("=================");
			HistoricActivityInstanceVO historicActivityInstanceVO = new HistoricActivityInstanceVO();
			historicActivityInstanceVO.setId(hisac.getId());
			historicActivityInstanceVO.setTaskId(hisac.getTaskId());
			historicActivityInstanceVO.setAssignee(hisac.getAssignee());
			historicActivityInstanceVO.setActivityId(hisac.getActivityId());
			historicActivityInstanceVO.setActivityName(hisac.getActivityName());
			historicActivityInstanceVO.setProcessInstanceId(hisac.getProcessInstanceId());
			historicActivityInstanceVO.setStartTime(hisac.getStartTime());
			historicActivityInstanceVO.setEndTime(hisac.getEndTime());
			result.add(historicActivityInstanceVO);
		}
		ImageVO imageVO = new ImageVO();
		imageVO.setImageBase64(base64);
		imageVO.setResult(result);

		return ResultMsg.success(imageVO);
	}

	// 作废暂停流程
	@GetMapping("/running-process/cancel")
	public ResultMsg runningProcessCancel()
	{
		return ResultMsg.success();
	}

	// 指派待办人
	@GetMapping("/running-process/set-assignee")
	public ResultMsg runningProcessSetAssignee()
	{
		return ResultMsg.success();
	}

	// 4.历史的流程：查看流程信息、流程用时、流程状态、查看任务发起人信息
	// 查看流程信息
	@GetMapping("/history-process/view-info")
	public ResultMsg historyProcessViewInfo()
	{
		return ResultMsg.success();
	}

	// 流程用时
	@GetMapping("/history-process/view-time-spent")
	public ResultMsg historyProcessViewTimeSpent()
	{
		return ResultMsg.success();
	}

	// 流程状态
	@GetMapping("/history-process/view-status")
	public ResultMsg historyProcessViewStatus()
	{
		return ResultMsg.success();
	}

	// 查看任务发起人信息
	@GetMapping("/history-process/view-sponsor")
	public ResultMsg historyProcessViewSponsor()
	{
		return ResultMsg.success();
	}

	// 5.待办任务 ：查看本人个人任务以及本角色下的任务、办理、驳回、作废、指派一下代理人
	// 查看本人个人任务以及本角色下的任务
	@GetMapping("/task/view-info")
	public ResultMsg taskViewInfo()
	{
		return ResultMsg.success();
	}

	// 办理
	@GetMapping("/task/Complete")
	public ResultMsg taskHandler(String processInstanceId)
	{
		activitiService.completeTask(processInstanceId);

		return ResultMsg.success();
	}

	// 驳回
	@GetMapping("/task/turn-down")
	public ResultMsg taskTurnDown()
	{
		return ResultMsg.success();
	}

	// 作废
	@GetMapping("/task/cancel")
	public ResultMsg taskCancel()
	{
		return ResultMsg.success();
	}

	// 指派一下代理人
	@GetMapping("/task/set-next-assignee")
	public ResultMsg taskSetNextAssignee()
	{
		return ResultMsg.success();
	}

	// 6.已办任务 ：查看自己办理过的任务以及流程信息、流程图、流程状态(作废 驳回 正常完成)
	// 查看自己办理过的任务以及流程信息
	@GetMapping("/history-task/view-info")
	public ResultMsg historyTaskViewInfo()
	{
		return ResultMsg.success();
	}

	// 查看自己办理过的任务流程图
	@GetMapping("/history-task/view-image")
	public ResultMsg historyTaskViewImage()
	{
		return ResultMsg.success();
	}

	// 查看自己办理过的流程状态(作废 驳回 正常完成)
	@GetMapping("/history-task/view-status")
	public ResultMsg historyTaskViewStatus()
	{
		return ResultMsg.success();
	}

}
