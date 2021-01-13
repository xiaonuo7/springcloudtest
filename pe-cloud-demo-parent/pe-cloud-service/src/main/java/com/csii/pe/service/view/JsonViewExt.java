package com.csii.pe.service.view;

import com.csii.pe.channel.http.JsonUtils;
import com.csii.pe.channel.http.servlet.CsiiView;

import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ApplicationObjectSupport;

public class JsonViewExt extends ApplicationObjectSupport implements CsiiView {
	protected Log log = LogFactory.getLog(this.getClass());
	public static final String HEADER_PRAGMA = "Pragma";
	public static final String HEADER_EXPIRES = "Expires";
	public static final String HEADER_CACHE_CONTROL = "Cache-Control";
	protected boolean nocacheFlag = true;
	protected String contentType = "application/json";
	protected String jsonField = null;
	protected String encoding = "UTF-8";
	protected static final String JSON_ERROR = "jsonError";

	public void setNocacheFlag(boolean nocacheFlag) {
		this.nocacheFlag = nocacheFlag;
	}

	public void setJsonField(String cf) {
		this.jsonField = cf;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void render(String viewName, Object model, Locale locale, HttpServletRequest request,
			HttpServletResponse response) {
		this.preventCaching(response);
		response.setContentType(this.contentType);
		Map<String, Object> map = (Map) model;
		Object content;
		if (map.containsKey("_exceptionMessageList")) {
			content = map.get("_exceptionMessageList");
			Map<String, Object> wrap = new HashMap(3);
			wrap.put("jsonError", content);
			content = wrap;
		} else if (map.get("_RejCode") != null && !"000000".equals(map.get("_RejCode"))) {
			content = map;
		} else if (viewName != null && viewName.trim().length() > 0) {
			viewName = viewName.trim();
			if (viewName.startsWith("[") && viewName.endsWith("]")) {
				viewName = viewName.substring(1, viewName.length() - 1);
				String[] keys = viewName.split("\\|");
				Map result = new HashMap();
				String[] var10 = keys;
				int var11 = keys.length;

				for (int var12 = 0; var12 < var11; ++var12) {
					String key = var10[var12];
					if (!key.trim().isEmpty()) {
						String[] array = key.trim().split("\\$");
						key = array[0];
						if (array.length > 1) {
							Object value = map.get(key);
							if (value != null) {
								if (value instanceof List) {
									List list = (List) value;
									List nameList = new ArrayList();
									Iterator var18 = list.iterator();

									while (var18.hasNext()) {
										Object obj = var18.next();
										nameList.add(this.getMessage(array[1], obj, locale));
									}

									result.put(key + "_Name", nameList);
								} else if (value.getClass().isArray()) {
									String[] nameArray = new String[Array.getLength(value)];

									for (int i = 0; i < Array.getLength(value); ++i) {
										nameArray[i] = this.getMessage(array[1], Array.get(value, i), locale);
									}

									result.put(key + "_Name", nameArray);
								} else {
									result.put(key + "_Name", this.getMessage(array[1], map.get(key), locale));
								}
							}
						}

						result.put(key, map.get(key));
					}
				}

				content = result;
			} else {
				content = map.get(viewName);
			}
		} else if (this.jsonField != null) {
			Map<String,Object> result = new HashMap();
			result.put("bussData", map.get(this.jsonField));
			result.put("_RejCode", "000000");
			content = result;
		} else {
			Map<String,Object> result = new HashMap();
			result.put("bussData", model);
			result.put("_RejCode", "000000");
			content = model;
		}
		if (content != null) {
			try {
				if ("UTF-8".equalsIgnoreCase(this.encoding)) {
					String json = JsonUtils.encode(content);
					if (this.log.isTraceEnabled()) {
						this.log.trace("json: " + json);
					}

					response.getWriter().write(json);
				} else {
					byte[] json = JsonUtils.encode(content, this.encoding);
					if (this.log.isTraceEnabled()) {
						this.log.trace("json bytes: " + new String(json, this.encoding));
					}

					OutputStream out = response.getOutputStream();
					response.setContentLength(json.length);
					out.write(json);
					out.flush();
				}
			} catch (Exception var20) {
				this.log.error("render", var20);
			}

		}
	}

	private String getMessage(String name, Object value, Locale locale) {
		return value == null
				? ""
				: this.getApplicationContext().getMessage(name + "." + value, (Object[]) null, String.valueOf(value),
						locale);
	}

	protected final void preventCaching(HttpServletResponse response) {
		if (this.nocacheFlag) {
			response.setHeader("Pragma", "No-cache");
		}

		response.setDateHeader("Expires", 1L);
		if (this.nocacheFlag) {
			response.setHeader("Cache-Control", "no-cache");
		}

	}
}