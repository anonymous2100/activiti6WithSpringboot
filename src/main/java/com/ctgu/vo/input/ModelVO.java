package com.ctgu.vo.input;

import java.io.Serializable;

import lombok.Data;

/**
 * @ClassName: ModelVO
 * @Description:
 * @author lh2
 * @date 2020年4月27日 下午12:34:13
 */
@Data
public class ModelVO implements Serializable
{
	/** serialVersionUID */
	private static final long serialVersionUID = -4375171555752576042L;
	private String processName;
	private String processDescription;
	private int processRevision;
	private String processKey;
}
