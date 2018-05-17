/*******************************************************************************
 * Copyright (c) Mar 20, 2018 @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a>.
 * All rights reserved.
 *
 * Contributors:
 *     <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> - initial API and implementation
 ******************************************************************************/
package com.foreveross.common.shiro;

import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.iff.infra.util.Exceptions;
import org.iff.infra.util.FCS;
import org.iff.infra.util.Logger;

import com.foreveross.common.ConstantBean;

/**
 * 默认调用 loadFilterChainDefinitions 加载filterChainDefinitions配置，如果要动态更新filterChainDefinitions，先要setFilterChainDefinitions，然后再调用reCreateFilterChains。
 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> 
 * @since Mar 20, 2018
 */
public class ShiroChainDefinitionManager {

	// 注意/r/n前不能有空格
	private static final String CRLF = "\r\n";

	private String filterChainDefinitions;

	@Inject
	private ShiroFilterFactoryBean shiroFilter;

	/**
	 * loading filterChainDefinitions. default by set filterChainDefinitions or load from property shiro.filterChainDefinitions.
	 * @return
	 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> 
	 * @since Mar 20, 2018
	 */
	public String loadFilterChainDefinitions() {
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isEmpty(filterChainDefinitions)) {
			filterChainDefinitions = ConstantBean.getProperty("shiro.filterChainDefinitions", "");
			Logger.info(FCS.get("Loading shiro filterChainDefinitions from shiro.properties, values: {0}.",
					filterChainDefinitions));
		} else {
			Logger.info(FCS.get("Shiro filterChainDefinitions has set, values: {0}.", filterChainDefinitions));
		}
		String[] split = StringUtils.split(filterChainDefinitions, ';');
		for (String value : split) {
			value = StringUtils.trim(value);
			if (StringUtils.isNotEmpty(value)) {
				sb.append(value).append(CRLF);
			}
		}
		return sb.toString();
	}

	/**
	 * update shiro filterChainDefinitions.
	 * @author <a href="mailto:iffiff1@gmail.com">Tyler Chen</a> 
	 * @since Mar 20, 2018
	 */
	public synchronized void reCreateFilterChains() {
		AbstractShiroFilter shiroFilterTmp = null;
		try {
			shiroFilterTmp = (AbstractShiroFilter) shiroFilter.getObject();
		} catch (Exception e) {
			Exceptions.runtime("Get ShiroFilter from shiroFilterFactoryBean error!", e);
		}

		PathMatchingFilterChainResolver filterChainResolver = (PathMatchingFilterChainResolver) shiroFilterTmp
				.getFilterChainResolver();
		DefaultFilterChainManager manager = (DefaultFilterChainManager) filterChainResolver.getFilterChainManager();

		// 清空老的权限控制
		manager.getFilterChains().clear();

		if (shiroFilter.getFilterChainDefinitionMap() != null) {
			shiroFilter.getFilterChainDefinitionMap().clear();
		}
		shiroFilter.setFilterChainDefinitions(loadFilterChainDefinitions());
		// 重新构建生成
		Map<String, String> chains = shiroFilter.getFilterChainDefinitionMap();
		for (Map.Entry<String, String> entry : chains.entrySet()) {
			String url = entry.getKey();
			String chainDefinition = entry.getValue().trim().replace(" ", "");
			manager.createChain(url, chainDefinition);
		}

	}

	public String getFilterChainDefinitions() {
		return filterChainDefinitions;
	}

	public void setFilterChainDefinitions(String filterChainDefinitions) {
		this.filterChainDefinitions = filterChainDefinitions;
	}

	public ShiroFilterFactoryBean getShiroFilter() {
		return shiroFilter;
	}

	public void setShiroFilter(ShiroFilterFactoryBean shiroFilter) {
		this.shiroFilter = shiroFilter;
	}

}
