/*
 * Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.shell.standard;

import static org.springframework.util.StringUtils.collectionToDelimitedString;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.shell.ConfigurableCommandRegistry;
import org.springframework.shell.MethodTarget;
import org.springframework.shell.MethodTargetRegistrar;
import org.springframework.shell.Utils;
import org.springframework.util.ReflectionUtils;

/**
 * The standard implementation of {@link MethodTargetRegistrar} for new shell applications,
 * resolves methods annotated with {@link ShellMethod} on {@link ShellComponent} beans.
 *
 * @author Eric Bottard
 * @author Florent Biville
 * @author Camilo Gonzalez
 */
public class StandardMethodTargetRegistrar implements MethodTargetRegistrar {

	@Autowired
	private ApplicationContext applicationContext;

	private Map<String, MethodTarget> commands = new HashMap<>();
	
	@Override
	public void register(ConfigurableCommandRegistry registry) {
		Map<String, Object> commandBeans = applicationContext.getBeansWithAnnotation(ShellComponent.class);
		for (Object bean : commandBeans.values()) {
			Class<?> clazz = bean.getClass();
			ReflectionUtils.doWithMethods(clazz, method -> {
				ShellMethod shellMapping = method.getAnnotation(ShellMethod.class);
				String[] keys = shellMapping.key();
				if (keys.length == 0) {
					keys = new String[] {Utils.unCamelify(method.getName())};
				}
				for (String key : keys) {
					MethodTarget target = new MethodTarget(method, bean, shellMapping.value());
					registry.register(key, target);
					commands.put(key, target);
				}
			}, method -> method.getAnnotation(ShellMethod.class) != null);
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " contributing "
			+ collectionToDelimitedString(commands.keySet(), ", ", "[", "]");
	}
}
