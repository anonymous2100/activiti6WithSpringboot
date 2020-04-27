package com.ctgu.vo.output;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class ImageVO implements Serializable
{
	private static final long serialVersionUID = -3711783440521695246L;

	private List<HistoricActivityInstanceVO> result;

	private String imageBase64;
}
